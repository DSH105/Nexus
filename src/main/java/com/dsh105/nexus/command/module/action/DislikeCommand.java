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

@Command(command = "dislike",
        needsChannel = false,
        groups = CommandGroup.ACTION,
        help = "Dislike a user",
        extendedHelp = {
                "{b}{p}{c} <user>{/b} - dislike someone!"
        })
public class DislikeCommand extends AbstractActionCommand {
    public DislikeCommand() {
        this.setVerb("dislike");
    }
}
