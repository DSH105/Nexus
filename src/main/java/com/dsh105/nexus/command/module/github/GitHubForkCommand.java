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

@Command(command = "fork", aliases = {"githubfork", "ghf"}, needsChannel = false, help = "Fork a GitHub repository. Requires a GitHub API key (see {b}{p}ghkey{/b})", extendedHelp = "This command is simply an alias of {b}{p}repo <name> fork{/b}")
public class GitHubForkCommand extends CommandModule {
    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 1 || event.getArgs().length == 2) {
            String fullName = event.getArgs().length == 1 ? event.getArgs()[0] : event.getArgs()[0] + " " + event.getArgs()[1];
            Nexus.getInstance().getCommandManager().onCommand(event.getChannel(), event.getSender(), "repo " + fullName + " fork");
            return true;
        }
        return false;
    }
}