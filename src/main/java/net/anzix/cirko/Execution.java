package net.anzix.cirko;

import java.util.Date;

/**
 * Metadata for one build execution.
 */
public class Execution {

    private String branch;

    private Date date;

    private int buildNo;

    private boolean success;

    private long time;

    private String vcsRevision;

    public void start() {
        date = new Date();
    }

    public void stop() {
        time = new Date().getTime() - date.getTime();
    }

    public String getBranch() {
        return branch;
    }

    public Date getDate() {
        return date;
    }

    public int getBuildNo() {
        return buildNo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setBuildNo(int buildNo) {
        this.buildNo = buildNo;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getVcsRevision() {
        return vcsRevision;
    }

    public void setVcsRevision(String vcsRevision) {
        this.vcsRevision = vcsRevision;
    }
}
