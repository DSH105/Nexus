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

package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.StringUtil;

@Command(command = "fix",
        needsChannel = false,
        groups = CommandGroup.ACTION,
        help = "Make someone fix something.",
        extendedHelp = {
                "{b}{p}{c} <user> [to_fix]{/b} - Make someone fix it!"
        })
public class FixCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String toFix = "it";
        String user = event.getArgs().length >= 1 ? " " + event.getArgs()[0] : "";
        if (event.getArgs().length > 1) {
            toFix = StringUtil.combineSplit(1, event.getArgs(), " ");
        }

        event.respond("Fix " + toFix + user + "!");
        return true;
    }
}
