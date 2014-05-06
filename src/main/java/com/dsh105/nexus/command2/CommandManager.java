package com.dsh105.nexus.command2;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command2.exceptions.CommandException;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.User;

public class CommandManager {

    protected CommandRegistrationService registrationService;

    public CommandManager() {
         this.registrationService = new CommandRegistrationService(this, null);
    }

    public void register(Class<?> commandClazz) {
        if(registrationService != null) {
            this.registrationService.register(commandClazz);
        }
    }

    public void onCommand(Channel channel, User sender, String content) {
        String[] split = content.substring(content.contains("\\") ? Nexus.getInstance().getConfig().getCommandPrefix().length() : 0).replaceAll("\\s+", " ").split(" ");
        onCommand(channel, sender, split[0].toLowerCase(), StringUtil.splitArgs(1, split, " "));
    }

    public void onCommand(User sender, String command, String... args) {
        onCommand(null, sender, command, args);
    }


    public void onCommand(Channel channel, User sender, String command, String... args) {
        try {
            this.registrationService.execute(command, args, sender, channel);
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }
}
