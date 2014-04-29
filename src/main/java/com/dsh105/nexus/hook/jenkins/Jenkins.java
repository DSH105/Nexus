/*
 * This file is part of Nexus.
 *
 * Nexus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nexus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nexus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.nexus.hook.jenkins;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.exception.JenkinsJobException;
import com.dsh105.nexus.exception.JenkinsJobNotFoundException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Jenkins {

    private HashSet<JenkinsJob> jobs = new HashSet<>();
    private HashMap<String, JenkinsJobEntry> jobEntries = new HashMap<>();
    protected String jenkinsUrl;
    protected String jenkinsToken;

    public Jenkins() {
        this.jenkinsToken = Nexus.getInstance().getConfig().getJenkinsUrl();
        this.jenkinsUrl = Nexus.getInstance().getConfig().getJenkinsToken();
    }

    public void requestBuild(String jobName) {
        String jenkinsUrl = Nexus.getInstance().getConfig().getJenkinsUrl();
        String token = Nexus.getInstance().getConfig().getJenkinsToken();
        if (!jenkinsUrl.isEmpty() && !token.isEmpty()) {
            Unirest.get(jenkinsUrl + "job/" + jobName + "/build?token=");
        }
    }

    public JenkinsJobEntry getJobEntry(String jobName) {
        JenkinsJob job = this.getJob(jobName);
        return job == null ? null : job.getJobEntry();
    }

    public JenkinsJob getJob(String jobName) {
        for (JenkinsJob job : getJobs()) {
            if (job.getJobName().equals(jobName)) {
                return job;
            }
        }
        return null;
    }

    public Set<JenkinsJob> getJobs() {
        return getJobs(false);
    }

    public Set<JenkinsJob> getJobs(boolean reconnect) {
        if (reconnect || this.jobEntries.isEmpty()) {
            JenkinsJobEntry[] jobs;
            try {
                jobs = Nexus.JSON.read(Unirest.get(jenkinsUrl + "/api/json"), "jobs", JenkinsJobEntry[].class);
            } catch (UnirestException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    throw new JenkinsJobNotFoundException("Failed to locate Jenkins API!", e);
                }
                throw new JenkinsJobException("Failed to connect to Jenkins API!", e);
            }
            if (jobs.length > 0) {
                this.jobEntries.clear();
                this.jobs.clear();
                for (JenkinsJobEntry entry : jobs) {
                    this.jobEntries.put(entry.getName(), entry);
                    this.jobs.add(new JenkinsJob(entry.getName(), entry));
                }
            }
        }
        return new HashSet<>(this.jobs);
    }


}