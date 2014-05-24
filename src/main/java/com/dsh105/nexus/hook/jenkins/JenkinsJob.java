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
import com.dsh105.nexus.exception.jenkins.JenkinsJobException;
import com.dsh105.nexus.exception.jenkins.JenkinsJobNotFoundException;
import com.dsh105.nexus.util.JsonUtil;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;

public class JenkinsJob {

    private String jobName;
    private JenkinsJobEntry jobEntry;
    private JenkinsJobHealth health;
    private JenkinsJobBuild latestBuild;

    public JenkinsJob(String jobName, JenkinsJobEntry jobEntry) {
        this.jobName = jobName;
        this.jobEntry = jobEntry;
        this.health = getHealth();
        this.latestBuild = getLatestBuild();
    }

    public String getJobName() {
        return jobName;
    }

    public JenkinsJobEntry getJobEntry() {
        return jobEntry;
    }

    public JenkinsJobBuild getLatestBuild() {
        if (this.latestBuild == null) {
            try {
                latestBuild = JsonUtil.read(Unirest.get(Jenkins.getJenkins().JENKINS_URL + "job/" + jobName + "/api/json"), "lastBuild", JenkinsJobBuild.class);
            } catch (UnirestException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    throw new JenkinsJobNotFoundException("Failed to locate Jenkins API!", e);
                }
                throw new JenkinsJobException("Failed to connect to Jenkins API!", e);
            }
        }
        return latestBuild;
    }

    public JenkinsJobHealth getHealth() {
        if (this.health == null) {
            try {
                return JsonUtil.read(Unirest.get(Jenkins.getJenkins().JENKINS_URL + "job/" + jobName + "/api/json"), "healthReport", JenkinsJobHealth[].class)[0];
            } catch (UnirestException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    throw new JenkinsJobNotFoundException("Failed to locate Jenkins API!", e);
                }
                throw new JenkinsJobException("Failed to connect to Jenkins API!", e);
            }
        }
        return health;
    }
}