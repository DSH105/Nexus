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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class JenkinsJob {

    public class JobEntry {
        private String name;
        private String url;
        private String color;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public Result getResult() {
            return Result.getByIdent(color);
        }
    }

    private class JobHealth {
        private String description;
        private int score;

        public String getDescription() {
            return description;
        }

        public int getScore() {
            return score;
        }
    }

    private class Build {
        private int number;
        private String url;

        public int getNumber() {
            return number;
        }

        public String getUrl() {
            return url;
        }
    }

    private String jobName;
    private JobEntry jobEntry;
    private JobHealth health;
    private Build latestBuild;

    public JenkinsJob(String jobName, JobEntry jobEntry) {
        this.jobName = jobName;
        this.jobEntry = jobEntry;
        this.health = getHealth();
        this.latestBuild = getLatestBuild();
    }

    public String getJobName() {
        return jobName;
    }

    public JobEntry getJobEntry() {
        return jobEntry;
    }

    public Build getLatestBuild() {
        if (this.latestBuild == null) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(Nexus.getInstance().getJenkins().jenkinsUrl + "job/" + jobName + "/api/json").openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setUseCaches(false);
                this.latestBuild = Nexus.JSON.read(con, "lastBuild", Build.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return latestBuild;
    }

    public JobHealth getHealth() {
        if (this.health == null) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(Nexus.getInstance().getJenkins().jenkinsUrl + "job/" + jobName + "/api/json").openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setUseCaches(false);
                this.health = Nexus.JSON.read(con, "healthReport", JobHealth.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return health;
    }
}