package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.InvalidInputException;
import com.dsh105.nexus.util.StringUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public abstract class AbstractActionCommand extends CommandModule {

    private String verb;

    /**
     * Sets the verb to use for the action command.
     * @param verb The singular form of the verb. E.g. slap.
     */
    public void setVerb(String verb) {
        Validate.notNull(verb);
        this.verb = verb;
    }

    public String getActionText(String[] args) {
        Validate.notNull(args);
        if (args.length < 1) {
            throw new InvalidInputException("Must specify a target user to perform the action on.");
        }
        String sentenceList = StringUtil.buildSentenceList(args[0].split(","));
        String additional = " ";
        if (args.length > 1) {
            System.out.print("> 1");
            additional += StringUtil.join(Arrays.copyOfRange(args, 1, args.length), " ");
        }
        return verb + "s " + sentenceList + additional;
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        throw new NotImplementedException("Command not yet written");
    }

}
