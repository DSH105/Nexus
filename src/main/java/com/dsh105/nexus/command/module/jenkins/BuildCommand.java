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

package com.dsh105.nexus.command.module.jenkins;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.jenkins.JenkinsException;
import com.dsh105.nexus.exception.jenkins.JenkinsJobNotFoundException;
import com.dsh105.nexus.hook.jenkins.Jenkins;
import com.dsh105.nexus.hook.jenkins.JenkinsJob;

@Command(command = "build",
        needsChannel = false,
        helpGroups = "admin",
        help = "Start building a Jenkins job on the stored CI. Only Nexus admins may use this command",
        extendedHelp = {
                "Begins the build process for a job on the configured Jenkins server",
                "{b}{p}{c} <job_name>{/b} - attempts to begin the build process for this job.",
                "{b}{p}{c} <job_name> token <build_token>{/b} - sets the build token for this job. Build tokens are used to access the Jenkins API and request builds",
                "Only certain jobs will work, as Nexus can only access certain job keys",
                "Only Nexus admins may use this command."
        })
public class BuildCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            event.respondWithPing("Usage: {0}", event.getCommandPrefix() + this.getCommand() + " <job_name>");
            return true;
        }

        String jobName = event.getArgs()[0];
        if (event.getArgs().length == 3) {
            if (event.getArgs()[1].equalsIgnoreCase("token")) {
                String token = event.getArgs()[2];
                JenkinsJob job;
                try {
                    job = Jenkins.getJenkins().getJob(jobName);
                } catch (JenkinsJobNotFoundException e) {
                    event.errorWithPing("{0} job could not be found on " + Nexus.getInstance().getConfig().getJenkinsUrl(), jobName);
                    return true;
                }
                if (job != null) {
                    Nexus.getInstance().getConfig().set("jenkins-token-" + job.getJobName(), token);
                    Nexus.getInstance().getConfig().save();
                    event.respondWithPing("Jenkins job token set for {0}.", job.getJobName());
                }
                return true;
            }
        }
        event.respondWithPing("Requesting build initiation of Jenkins job ({0})...", jobName);
        try {
            Jenkins.getJenkins().requestBuild(jobName);
            return true;
        } catch (JenkinsException e) {
            event.errorWithPing("{0} job could not be found on " + Nexus.getInstance().getConfig().getJenkinsUrl(), jobName);
            return true;
        }
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}