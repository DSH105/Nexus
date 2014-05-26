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

package com.dsh105.nexus.command.module.dynamic;

import com.dsh105.nexus.command.Exclude;
import org.apache.commons.lang3.Validate;

@Exclude
public class DynamicCommandFactory {

    private String command;
    private String response;
    private boolean needsChannel = false;
    private boolean action = false;
    private String help;
    private String[] extendedHelp;
    private String[] aliases = new String[0];

    public DynamicCommandFactory withCommand(String command) {
        this.command = command;
        return this;
    }

    public DynamicCommandFactory withResponse(String response) {
        this.response = response;
        return this;
    }

    public DynamicCommandFactory withActionResponse(String response) {
        this.response = response;
        this.action = true;
        return this;
    }

    public DynamicCommandFactory withChannelRequirement(boolean needsChannel) {
        this.needsChannel = needsChannel;
        return this;
    }

    public DynamicCommandFactory withHelp(String help) {
        this.help = help;
        return this;
    }

    public DynamicCommandFactory withExtendedHelp(String... extendedHelp) {
        this.extendedHelp = extendedHelp;
        return this;
    }

    public DynamicCommandFactory withAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    public DynamicCommand prepare() {
        Validate.notNull(command, "Command for DynamicCommand must not be null");
        Validate.notNull(response, "Response for DynamicCommand must not be null");
        Validate.notNull(help, "Help for DynamicCommand must not be null");
        Validate.notNull(extendedHelp, "Extended help for DynamicCommand must not be null");
        Validate.notNull(aliases, "Aliases for DynamicCommand must not be null");

        return new DynamicCommand(command, response, needsChannel, help, extendedHelp, aliases, action);
    }
}