package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.InvalidInputException;
import com.dsh105.nexus.util.StringUtil;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public abstract class AbstractActionCommand extends CommandModule {

    private boolean override;
    private String verb;
    private String defaultReceiver;

    /**
     * Sets the verb to use for the action command.
     * @param verb The singular form of the verb. E.g. slap.
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
     * @param verb The singular form of the verb. E.g. slap.
     */
    public void setVerb(String verb) {
        this.setVerb(verb, "everybody");
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
        return verb + (isOverriden() ? " " : "s ") + sentenceList + additional;
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
