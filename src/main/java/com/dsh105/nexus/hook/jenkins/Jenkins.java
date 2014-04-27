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
import com.dsh105.nexus.util.JsonUtil;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Jenkins {

    private HashSet<JenkinsJob> jobs = new HashSet<>();
    private HashMap<String, JenkinsJob.JobEntry> jobEntries = new HashMap<>();
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
            try {
                new URL(jenkinsUrl + "job/" + jobName + "/build?token=" + token).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JenkinsJob.JobEntry getJobEntry(String jobName) {
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
            JenkinsJob.JobEntry[] jobs = new JenkinsJob.JobEntry[0];
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(Nexus.getInstance().getJenkins().jenkinsUrl + "/api/json").openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setUseCaches(false);
                jobs = Nexus.JSON.read(con, "jobs", JenkinsJob.JobEntry[].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (jobs.length > 0) {
                this.jobEntries.clear();
                this.jobs.clear();
                for (JenkinsJob.JobEntry entry : jobs) {
                    this.jobEntries.put(entry.getName(), entry);
                    this.jobs.add(new JenkinsJob(entry.getName(), entry));
                }
            }
        }
        return new HashSet<>(this.jobs);
    }


}