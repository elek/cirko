package net.anzix.cirko;

import java.nio.file.Path;

/**
 * Configuration for the whole CI system.
 */
public class Env {

    private Path repoDir;

    private Path metaDir;

    private boolean rebuild;

    public Env(Path repoDir, Path metaDir) {
        this.repoDir = repoDir;
        this.metaDir = metaDir;
    }

    public Path getRepoDir() {
        return repoDir;
    }

    public Path getMetaDir() {
        return metaDir;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }
}
