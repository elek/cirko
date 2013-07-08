package net.anzix.cirko.buildr;

import net.anzix.cirko.Execution;
import net.anzix.cirko.Project;

import java.nio.file.Path;
import java.util.Set;

/**
 * Executors to build a specific project.
 */
public interface Buildr {

    /**
     * True if the project build could be build by this definition.
     */
    public boolean isMatch(Path path);

    /**
     * Build the project.
     */
    public void build(Project project, Execution ex);

    /**
     * Return a set of path which could be persisted between builds (artifacts/test results...)
     */
    public Set<Path> pathToStore();
}
