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

@Command(command = "issue", aliases = {"githubissue", "ghi"}, needsChannel = false, help = "Retrieve issue information for a GitHub repository", extendedHelp = "This command is simply an alias of {p}{b}repo <name> issue <number>{/b}")
public class GitHubIssueCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        int issueId = -1;
        String fullName = "";
        String postArgs = "";
        for (int i = 0; i < event.getArgs().length; i++) {
            if (StringUtil.isInt(event.getArgs()[i]) && i >= 1) {
                issueId = Integer.parseInt(event.getArgs()[i]);
                for (int j = 0; j < i; j++) {
                    fullName += (fullName != null && !fullName.isEmpty() ? " " : "") + event.getArgs()[j];
                }
                if (event.getArgs().length >= i) {
                    postArgs = StringUtil.combineSplit(i + 1, event.getArgs(), " ");
                }
                break;
            }
        }
        return Nexus.getInstance().getCommandManager().onCommand(event.getChannel(), event.getSender(), "repo " + fullName + " issue " + issueId + postArgs);
    }
}