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

package com.dsh105.nexus.command.module.github;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.StringUtil;

@Command(command = "merge", aliases = {"githubmerge", "ghmerge", "ghm"}, needsChannel = false, help = "Merge a pull request", extendedHelp = "This command is simply an alias of {p}{b}repo <name> issue <number>{/b}")
public class GitHubMergePullRequestCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 2 || event.getArgs().length == 3) {
            String fullName = event.getArgs().length == 2 ? event.getArgs()[0] : event.getArgs()[0] + " " + event.getArgs()[1];
            String issueNumber = event.getArgs().length == 2 ? event.getArgs()[1] : event.getArgs()[2];
            if (!StringUtil.isInt(issueNumber)) {
                event.errorWithPing("{0} needs to be a number.", issueNumber);
                return true;
            }
            return Nexus.getInstance().getCommandManager().onCommand(event.getChannel(), event.getSender(), "repo " + fullName + " issue " + issueNumber + " merge");
        }
        return false;
    }
}