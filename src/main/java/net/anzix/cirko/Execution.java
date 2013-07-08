package net.anzix.cirko;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    /**
     * Url to the vcsRevision on a web based browser.
     */
    private String vcsUrl;

    /**
     * Download urls (name,url) relative to the branch meta dir.
     */
    Map<String, String> downloadables = new HashMap<>();

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

    public Map<String, String> getDownloadables() {
        return downloadables;
    }

    public void setDownloadables(Map<String, String> downloadables) {
        this.downloadables = downloadables;
    }
}
