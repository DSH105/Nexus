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

package com.dsh105.nexus.command.module;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.JenkinsJobNotFoundException;
import com.dsh105.nexus.hook.jenkins.JenkinsJob;
import com.dsh105.nexus.hook.jenkins.Result;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Colors;

@Command(command = "ci", needsChannel = false, help = "Fetch information on a Jenkins job.",
        extendedHelp = {
                "The CI allows you to connect to a set (unchangeable) Jenkins server to retrieve build information for certain jobs.",
                "{b}{p}{c} <job_name>{/b} - fetches information on a certain jenkins job and presents to the user"})
public class CICommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            event.respondWithPing("Usage: {0}", event.getCommandPrefix() + this.getCommand() + " <job_name>");
            return true;
        }
        String jobName = event.getArgs()[0];
        JenkinsJob job;
        try {
            job = Nexus.getInstance().getJenkins().getJob(jobName);
        } catch (JenkinsJobNotFoundException e) {
            event.respond(Colors.RED + "The {0} job could not be found on " + Nexus.getInstance().getConfig().getJenkinsUrl() + "! :(", jobName);
            return true;
        }
        if (job != null) {
            Result result = job.getJobEntry().getResult();
            event.respond(Colors.BOLD + "Jenkins" + Colors.NORMAL + " - " + Colors.BLUE + Colors.BOLD + job.getJobName() + Colors.NORMAL + " - " + job.getJobEntry().getUrl());
            event.respond(job.getHealth().getDescription() + " ({0}%)", String.valueOf(job.getHealth().getScore()));
            event.respond("Latest build: #{0} - {1} ({2})", String.valueOf(job.getLatestBuild().getNumber()), result.format(StringUtil.capitalise(result.toString().replace("_", " "))), job.getLatestBuild().getUrl());
            return true;
        }
        return false;
    }
}