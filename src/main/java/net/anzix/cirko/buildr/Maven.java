package net.anzix.cirko.buildr;

import net.anzix.cirko.Env;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Maven extends Script {

    public Maven(Env env) {
        super(env);
    }

    @Override
    public boolean isMatch(Path path) {
        return Files.exists(path.resolve("pom.xml"));
    }

    @Override
    protected ProcessBuilder createProcessBuilder() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return new ProcessBuilder("mvn.bat", "clean", "install");
        } else {
            return new ProcessBuilder("mvn", "clean", "install");
        }
    }

    @Override
    public Set<Path> pathToStore() {
        Set<Path> result = new HashSet<>();
        result.add(Paths.get("target"));
        return result;
    }
}
