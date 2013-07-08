package net.anzix.cirko;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.anzix.cirko.buildr.Buildr;
import net.anzix.cirko.buildr.Gradle;
import net.anzix.cirko.buildr.Maven;
import net.anzix.cirko.buildr.Script;
import net.anzix.cirko.util.TreeCopier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Command to build specific/all branches of a project.
 */
public class Build {

    private static Logger LOG = LoggerFactory.getLogger(Build.class);

    Env env;
    List<Buildr> builders = new ArrayList<>();

    public Build(Env e) {
        this.env = e;
        builders.add(new Script(e));
        builders.add(new Gradle(e));
        builders.add(new Maven(e));
    }


    public ExecutionReport execute(Project project) {
        ExecutionReport report = new ExecutionReport();
        Path metaDir = env.getMetadataDir();


        Path checkout = project.getCheckoutDir();
        if (!Files.exists(checkout)) {
            throw new BuildException(String.format("Checkout dir %s doesn't exist for the project %s", checkout, project.getName()));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {

            Properties commonCiProperties = readCiProperties(project);

            FileRepository repo = new FileRepository(project.getCheckoutDir().resolve(".git").toFile());
            Git git = new Git(repo);

            for (String branch : commonCiProperties.getProperty("branches", "master").split(",")) {

                branch = branch.trim();
                LOG.debug("Building branch " + branch);

                Path branchMetaDir = getBranchMetaDir(project, branch);

                Properties ciProperties = loadProperties(project.getCheckoutDir().resolve("ci.properties"), commonCiProperties);

                Path lastExPath = branchMetaDir.resolve("last.json");

                //prase the last Execution if exists.
                Execution lastExecution = null;
                if (Files.exists(lastExPath)) {
                    try (FileReader r = new FileReader(lastExPath.toFile())) {
                        lastExecution = gson.fromJson(r, Execution.class);
                    }
                }

                if (!env.isActive(project, branch)) {
                    LOG.debug("Skipping build as a command line filter is used: " + env.getProjectFilter());
                    report.addReport(branch, lastExecution);
                    continue;
                }

                try {

                    Execution execution = new Execution();
                    execution.setBranch(branch);
                    if (lastExecution != null) {
                        execution.setBuildNo(lastExecution.getBuildNo() + 1);
                    }

                    git.fetch().setRemote("origin").call();
                    git.checkout().setName("origin/" + branch).call();
                    git.reset().setMode(ResetCommand.ResetType.HARD).setRef("origin/" + branch).call();
                    String rev = repo.resolve("HEAD").getName();
                    if (!env.isRebuild() && lastExecution != null && rev.equals(lastExecution.getVcsRevision())) {
                        LOG.debug("Skipping build as this revision has alredy been build.");
                        report.addReport(branch, lastExecution);
                        continue;
                    } else {
                        report.addReport(branch, execution);
                    }
                    execution.setVcsRevision(rev);

                    Buildr currentBuilder = null;
                    for (Buildr buildr : builders) {
                        if (buildr.isMatch(project.getCheckoutDir())) {
                            currentBuilder = buildr;
                            break;
                        }
                    }

                    if (currentBuilder == null) {
                        execution.setSuccess(false);
                        LOG.error(String.format("Can't find builder for project/branch %s/%s", project.getName(), branch));
                    } else {
                        currentBuilder.build(project, execution);
                        saveArtifacts(project, execution, currentBuilder);
                    }
                    findDownloadables(branchMetaDir, ciProperties, execution);
                    try (FileWriter writer = new FileWriter(lastExPath.toFile())) {
                        writer.write(gson.toJson(execution));
                    }
                    if (execution.isSuccess()) {
                        LOG.debug("Branch has been built successfully");
                    } else {
                        LOG.error(String.format("Failed to build branch %s of project %s", branch, project.getName()));
                    }

                } catch (Exception ex) {
                    LOG.error(String.format("Can't execute build script for project %s and branch %s", project.getName(), branch), ex);
                }
            }
        } catch (IOException e) {
            throw new BuildException("Can't checkout and build the project " + project.getName(), e);
        }
        return report;
    }

    /**
     * Find downloadable artifacts in the destination directory based on regexp pattern.
     */
    private void findDownloadables(final Path branchMetaDir, Properties ciProperties, final Execution ex) {
        if (ciProperties.containsKey("downloads")) {
            for (final String download : ((String) ciProperties.get("downloads")).trim().split(",")) {
                final String normalPattern = download.trim().replace('\\', '/');
                try {
                    Files.walkFileTree(branchMetaDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            String normalFileName = branchMetaDir.relativize(file).toString().replace('\\', '/');
                            if (normalFileName.matches(normalPattern)) {
                                ex.getDownloadables().put(file.getFileName().toString(), normalFileName);
                            }
                            return super.visitFile(file, attrs);
                        }
                    });
                } catch (Exception e) {
                    LOG.error("Can't find the downloadables artifacts: " + ciProperties.get("downloads"), e);
                }
            }
        }
    }

    /**
     * Copy specific dirs and files from the build dir to the build result metadir.
     */
    private void saveArtifacts(Project project, Execution execution, Buildr currentBuilder) {
        Path branchMeta = project.getMetadataDir().resolve(execution.getBranch());
        for (Path toSave : currentBuilder.pathToStore()) {
            Path from = project.getCheckoutDir().resolve(toSave);
            if (Files.exists(from)) {
                Path to = branchMeta.resolve(toSave);
                try {
                    LOG.debug(String.format("Copying artifacts from %s to %s", from, to));
                    Files.walkFileTree(from, new TreeCopier(from, to, true));
                } catch (IOException e) {
                    LOG.error(String.format("Can't copy artifacts from %s to %s", from, to), e);
                }
            }

        }
    }

    private Path getBranchMetaDir(Project project, String branch) {
        Path meta = project.getMetadataDir().resolve(branch);
        if (!Files.exists(meta)) {
            try {
                Files.createDirectories(meta);
            } catch (IOException e) {
                throw new BuildException(String.format("Can't create metadata directory %s for the project %s", meta, project), e);
            }
        }
        return meta;
    }

    private Properties loadProperties(Path path) throws IOException {
        return loadProperties(path, null);
    }

    /**
     * Loading properties file from a path (with an optional default set).
     */
    private Properties loadProperties(Path path, Properties deflt) throws IOException {
        Properties props;
        if (deflt != null) {
            props = new Properties(deflt);
        } else {
            props = new Properties();
        }
        if (Files.exists(path)) {
            try (InputStream is = new FileInputStream(path.toFile())) {
                props.load(is);
            }
        }
        return props;
    }

    private Properties readCiProperties(Project project) throws IOException {
        return loadProperties(project.getMetadataDir().resolve("ci.properties"));
    }


    public Env getEnv() {
        return env;
    }

    public void setEnv(Env env) {
        this.env = env;
    }
}
