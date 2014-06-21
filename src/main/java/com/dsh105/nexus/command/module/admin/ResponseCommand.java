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

package com.dsh105.nexus.command.module.admin;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.response.ResponseTrigger;
import com.dsh105.nexus.util.StringUtil;

import java.util.ArrayList;

@Command(command = "response",
        needsChannel = false,
        groups = CommandGroup.ADMIN,
        help = "Manage response triggers",
        extendedHelp = {
                "{b}{p}{c} create <trigger> <chance> <response>{/b} - Create a response trigger with a trigger chance.",
                "{b}{p}{c} add <trigger> <response>{/b} - Add a response to an existing trigger.",
                "{b}{p}{c} <trigger>{/b} - View information on existing response triggers"
        })
public class ResponseCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            return false;
        }

        if (event.getArgs().length == 1) {
            ResponseTrigger trigger = Nexus.getInstance().getResponseManager().getTriggerInstance(event.getArgs()[0]);
            if (trigger == null) {
                event.errorWithPing("Trigger ({0}) doesn't exist! Use {1} to create one.", event.getArgs()[0], event.getCommandPrefix() + event.getCommand() + " create <trigger> <chance> <response>");
                return true;
            }
            ArrayList<String> responses = Nexus.getInstance().getResponseManager().getResponsesFor(trigger);
            event.respondWithPing("Trigger ({0}) has {1} response" + (responses.size() == 1 ? "" : "s") + " and fires at a chance of {2}", trigger.getTrigger(), responses.size() + "", trigger.getChance() + "%");
            return true;
        }

        if (event.getArgs()[0].equalsIgnoreCase("create")) {
            String trigger = event.getArgs()[1].toLowerCase();
            int chance = StringUtil.toInteger(event.getArgs()[2]);
            String response = StringUtil.combineSplit(3, event.getArgs(), " ");
            Nexus.getInstance().getResponseManager().addResponses(new ResponseTrigger(chance, trigger), response);
            event.respondWithPing("Response trigger ({0}) created with a chance of {1}.", trigger, chance + "%");
            return true;
        } else if (event.getArgs()[0].equalsIgnoreCase("add")) {
            String trigger = event.getArgs()[1].toLowerCase();
            String response = StringUtil.combineSplit(2, event.getArgs(), " ");
            ResponseTrigger triggerInstance = Nexus.getInstance().getResponseManager().getTriggerInstance(trigger);
            if (triggerInstance == null) {
                event.errorWithPing("Trigger ({0}) doesn't exist! Use {1} to create one.", trigger, event.getCommandPrefix() + event.getCommand() + " create <trigger> <chance> <response>");
                return true;
            }
            Nexus.getInstance().getResponseManager().addResponses(triggerInstance, response);
            event.respondWithPing("Response added to trigger ({0}).", trigger);
            return true;
        }
        return false;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}