package net.anzix.cirko;

import java.nio.file.Path;

/**
 * Configuration for the whole CI system.
 */
public class Env {

    private Path repoDir;

    private Path metaDir;

    private boolean rebuild;

    private String projectFilter;

    public Env(Path repoDir, Path metaDir) {
        this.repoDir = repoDir;
        this.metaDir = metaDir;
    }

    public Path getRepoDir() {
        return repoDir;
    }

    public Path getMetadataDir() {
        return metaDir;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }

    public String getProjectFilter() {
        return projectFilter;
    }

    public void setProjectFilter(String projectFilter) {
        this.projectFilter = projectFilter;
    }

    public boolean isActive(Project p, String branch) {
        return projectFilter == null || projectFilter.equals(p.getName());
    }
}
