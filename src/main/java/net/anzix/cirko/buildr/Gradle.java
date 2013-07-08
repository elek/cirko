package net.anzix.cirko.buildr;

import net.anzix.cirko.Env;

import java.nio.file.Files;
import java.nio.file.Path;

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
            return new ProcessBuilder("gradle.bat");
        } else {
            return new ProcessBuilder("gradle");
        }
    }
}
