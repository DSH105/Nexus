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

package com.dsh105.nexus.command2;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command2.exceptions.*;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;

public class CommandManager {

    protected CommandRegistrationService registrationService;
    private String NO_PERMISSION = Colors.RED + "Error: You don't have permission";
    private String ERROR_OCCURRED = Colors.RED + "An unknown error occurred. See the console for more info.";
    private String NUMBER_EXCEPTION = Colors.RED + "Number expected, string received instead.";
    private PermissionHandler permissionHandler;

    public CommandManager() {
        this.registrationService = new CommandRegistrationService(this, null);
    }

    public void setPermissionHandler(PermissionHandler permissionHandler) {
        if (this.permissionHandler != null) {
            throw new RuntimeException("PermissionHandler already set!");
        }
        this.permissionHandler = permissionHandler;
    }

    public void register(Class<?> commandClazz) {
        if (registrationService != null) {
            this.registrationService.register(commandClazz);
        }
    }

    public void onCommand(Channel channel, User sender, String content) {
        String commandPrefix = null;
        for (String prefix : Nexus.getInstance().getConfig().getCommandPrefixes()) {
            if (content.startsWith(prefix)) {
                commandPrefix = prefix;
            }
        }
        String[] split = Colors.removeFormattingAndColors(content).substring(commandPrefix == null ? commandPrefix.length() : 0).replaceAll("\\s+", " ").split(" ");
        onCommand(channel, sender, split[0].toLowerCase(), StringUtil.splitArgs(1, split, " "));
    }

    public void onCommand(User sender, String command, String... args) {
        onCommand(null, sender, command, args);
    }


    public void onCommand(Channel channel, User sender, String command, String... args) {
        try {
            this.registrationService.execute(command, args, sender, channel);
        } catch (CommandPermissionsException e) {
            sender.send().message(this.NO_PERMISSION);
        } catch (MissingNestedCommandException e) {
            sender.send().message(Colors.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.send().message(Colors.RED + e.getMessage());
            sender.send().message(Colors.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.send().message(this.NUMBER_EXCEPTION);
            } else {
                sender.send().message(this.ERROR_OCCURRED);
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.send().message(Colors.RED + e.getMessage());
        }
    }
}
