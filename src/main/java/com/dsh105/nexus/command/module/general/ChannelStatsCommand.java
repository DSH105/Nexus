package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import org.pircbotx.Channel;

@Command(command = "chanstats", needsChannel = true, help = "Gives stats on the current channel.",
        extendedHelp = {"{b}{p}{c}{/b} - Gives the number of users connected plus some useful metrics on voiced/opped users."})
public class ChannelStatsCommand extends CommandModule {
    @Override
    public boolean onCommand(CommandPerformEvent event) {
        Channel chan = event.getChannel();
        String chanName = chan.getName();
        int numUsers = chan.getUsers().size();
        int numOpped = chan.getOps().size();
        int numVoiced = chan.getVoices().size();

        int opPercentage = Math.round(((float) numOpped / numUsers) * 100);
        int voicePercentage = Math.round(((float) numVoiced / numUsers) * 100);

        event.respondWithPing(String.format("Channel %s has %d users. There are %d ops (%d%%) and %d voiced users (%d%%).", chanName, numUsers, numOpped, opPercentage, numVoiced, voicePercentage));
        return true;
    }
}
