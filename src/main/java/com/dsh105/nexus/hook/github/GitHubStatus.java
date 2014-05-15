package com.dsh105.nexus.hook.github;

public class GitHubStatus {
    private String status;
    private String body;
    private String created_on;

    public String getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public String getCreated_on() {
        return created_on;
    }
}
