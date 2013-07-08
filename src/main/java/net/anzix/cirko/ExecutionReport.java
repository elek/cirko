package net.anzix.cirko;

import java.util.HashMap;
import java.util.Map;

/**
 * Build state of a specific project.
 */
public class ExecutionReport {
    /**
     * Execution result per branch.
     */
    private Map<String, Execution> reports = new HashMap<>();

    public void addReport(String branch, Execution execution) {
        reports.put(branch, execution);
    }

    public Map<String, Execution> getReports() {
        return reports;
    }
}
