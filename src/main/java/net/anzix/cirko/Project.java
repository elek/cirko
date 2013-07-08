package net.anzix.cirko;

import java.nio.file.Path;
import java.util.Map;

/**
 * Project to build with checkout and metadata files.
 */
public class Project {

    private String name;

    /**
     * Last executions per branch.
     */
    private Map<String, Execution> lastExecutions;

    Path root;

    Path metaData;

    public Project(Path p) {
        this.root = p;
        this.name = p.getFileName().toString();
    }

    public Path getCheckoutDir() {
        return root;
    }

    public Path getMetadataDir() {
        return metaData;
    }

    public static Project create(Path p, Env env) {
        Project project = new Project(p);
        project.metaData = env.getMetadataDir().resolve(project.name);
        return project;

    }

    public String getName() {
        return name;
    }

    public Path getRoot() {
        return root;
    }
}
