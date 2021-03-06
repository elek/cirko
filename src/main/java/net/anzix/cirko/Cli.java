package net.anzix.cirko;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import com.google.gson.GsonBuilder;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point and argument parsing.
 */
public class Cli {

    @Argument(metaVar = "repos_dir", usage = "directory with checked out repositories", required = true)
    String work;

    @Argument(metaVar = "meta_dir", usage = "Directory for the build reports and metadata", required = true, index = 1)
    String meta;

    @Option(name = "-p", usage = "filter for only this project")
    String project;

    @Option(name = "-q", usage = "Set log level to ERROR")
    boolean quite;

    @Option(name = "-r", usage = "Rebuild all project even if not changed")
    boolean rebuild;

    private static Logger LOG = LoggerFactory.getLogger(Cli.class);

    public static void main(String args[]) throws IOException, GitAPIException {
        Cli cli = new Cli();
        CmdLineParser parser = new CmdLineParser(cli);
        try {
            parser.parseArgument(args);
            cli.run();
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

    }

    private void run() {
        if (quite) {
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            logger.setLevel(Level.ERROR);
        }
        Path workDir = Paths.get(work);
        Path metaDir = Paths.get(meta);
        Env e = new Env(workDir, metaDir);
        e.setRebuild(rebuild);
        e.setProjectFilter(project);
        Build b = new Build(e);
        try {
            Map<String, ExecutionReport> result = new HashMap<String, ExecutionReport>();
            for (Path projectDir : Files.newDirectoryStream(workDir)) {
                Project prj = Project.create(projectDir, e);


                Appender app = createProjectLogger(prj);
                LOG.debug("Building project " + projectDir.getFileName());
                result.put(prj.getName(), b.execute(prj));
                removeProjectLogger(app);

            }
            try (FileWriter writer = new FileWriter(metaDir.resolve("status.json").toFile())) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(result));
            }
        } catch (IOException ex) {
            throw new BuildException("Can't iterate over the workDir " + workDir, ex);
        }
    }

    private void removeProjectLogger(Appender app) {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        app.stop();
        logger.detachAppender(app);
    }

    private Appender createProjectLogger(Project prj) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("timestamp");
        // set the file name
        fileAppender.setFile(prj.getMetadataDir().resolve("build.log").toFile().getAbsolutePath());

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d %r %level - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(fileAppender);
        return fileAppender;


    }

}
