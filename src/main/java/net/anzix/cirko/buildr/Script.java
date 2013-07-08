package net.anzix.cirko.buildr;

import net.anzix.cirko.Env;
import net.anzix.cirko.Execution;
import net.anzix.cirko.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Generic script builder. Builds build.sh from the project dir.
 */
public class Script implements Buildr {

    private static Logger LOG = LoggerFactory.getLogger(Script.class);

    private Env env;


    public Script(Env env) {
        this.env = env;

    }

    @Override
    public boolean isMatch(Path path) {
        return Files.exists(path.resolve("build.sh"));
    }

    @Override
    public void build(Project project, Execution ex) {
        try {
            Path brachMetaDir = project.getMetadataDir().resolve(ex.getBranch());
            ProcessBuilder p = createProcessBuilder();


            p.directory(project.getRoot().toFile().getAbsoluteFile());
            p.redirectErrorStream(true);
            p.redirectOutput(brachMetaDir.resolve("last-output.txt").toFile());
            loadEnv(p.environment());


            ex.start();
            Process ps = p.start();
            ex.setSuccess(ps.waitFor() == 0);
            ex.stop();


        } catch (Exception e) {
            LOG.debug(String.format("Can't build the %s on %s ", project.getName(), ex.getBranch()), e);
            ex.setSuccess(false);
        }
    }

    protected ProcessBuilder createProcessBuilder() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return new ProcessBuilder("bash.exe", "build.sh");
        } else {
            return new ProcessBuilder("/bin/bash", "build.sh");
        }
    }

    @Override
    public Set<Path> pathToStore() {
        return new HashSet<>();
    }

    public void loadEnv(Map<String, String> envVars) {
        Path envFile = env.getMetaDir().resolve("env.properties");
        if (Files.exists(envFile)) {
            Properties sp = new Properties();
            try (InputStream stream = new FileInputStream(envFile.toFile())) {
                sp.load(stream);
                for (Object key : sp.keySet()) {
                    envVars.put(key.toString(), sp.get(key).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (System.getenv("PATH") != null) {
            envVars.put("PATH", System.getenv("PATH"));
        }
        if (System.getenv("JAVA_HOME") != null) {
            envVars.put("JAVA_HOME", System.getenv("JAVA_HOME"));
        }
    }
}
