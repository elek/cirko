package net.anzix.cirko.buildr;

import net.anzix.cirko.Env;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Gradle extends Script {

    public Gradle(Env env) {
        super(env);
    }

    @Override
    public boolean isMatch(Path path) {
        return Files.exists(path.resolve("build.gradle"));
    }

    @Override
    protected ProcessBuilder createProcessBuilder() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return new ProcessBuilder("gradle.bat", "build");
        } else {
            return new ProcessBuilder("gradle", "build");
        }
    }

    @Override
    public Set<Path> pathToStore() {
        Set<Path> result = new HashSet<>();
        result.add(Paths.get("build"));
        return result;
    }
}
