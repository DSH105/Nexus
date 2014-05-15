package com.dsh105.nexus.command2;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command2.exceptions.*;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;

public class CommandManager {

    private String NO_PERMISSION = Colors.RED + "Error: You don't have permission";
    private String ERROR_OCCURRED = Colors.RED + "An unknown error occurred. See the console for more info.";
    private String NUMBER_EXCEPTION = Colors.RED + "Number expected, string received instead.";

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
        } catch (CommandPermissionsException e) {
            sender.sendMessage(this.NO_PERMISSION);
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(Colors.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(Colors.RED + e.getMessage());
            sender.sendMessage(Colors.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(this.NUMBER_EXCEPTION);
            } else {
                sender.sendMessage(this.ERROR_OCCURRED);
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(Colors.RED + e.getMessage());
        }
    }
}
