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

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.command.Exclude;
import com.dsh105.nexus.exception.general.InvalidInputException;
import com.dsh105.nexus.util.StringUtil;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

@Exclude
public abstract class AbstractActionCommand extends CommandModule {

    private boolean override;
    private String verb;
    private String defaultReceiver;
    private String prefix = "";

    /**
     * Sets the verb to use for the action command.
     *
     * @param verb            The singular form of the verb. E.g. slap.
     * @param defaultReceiver The default receiver of the action e.g. everybody
     */
    public void setVerb(String verb, String defaultReceiver) {
        Validate.notNull(verb);
        Validate.notNull(defaultReceiver);
        this.verb = verb;
        this.defaultReceiver = defaultReceiver;
    }

    /**
     * Sets the verb to use for the action command.
     *
     * @param verb The singular form of the verb. E.g. slap.
     */
    public void setVerb(String verb) {
        this.setVerb(verb, "everybody");
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        Validate.notNull(prefix);
        this.prefix = " " + prefix;
    }

    public boolean isOverriden() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public String getActionText(String... args) {
        Validate.notNull(args);
        if (args.length < 1) {
            throw new InvalidInputException("Must specify a target user to perform the action on.");
        }
        String sentenceList = StringUtil.buildSentenceList(args[0].split(","));
        String additional = " ";
        if (args.length > 1) {
            additional += StringUtil.join(Arrays.copyOfRange(args, 1, args.length), " ");
        }
        return verb + (override ? " " : "s ") + sentenceList + (args.length > 1 ? prefix : "") + additional;
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String actionText = event.getArgs().length == 0 ? getActionText(defaultReceiver) : getActionText(event.getArgs());
        if (event.isInPrivateMessage()) {
            Nexus.getInstance().sendIRC().action(event.getSender().getNick(), actionText);
        } else {
            Nexus.getInstance().sendIRC().action(event.getChannel().getName(), actionText);
        }
        return true;
    }

}
