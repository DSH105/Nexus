/**
 * This file is part of Nexus. In case stuff doesn't work,
 * Just be patient while we try to fix it: http://puu.sh/8N7Dy.gif
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
import com.dsh105.nexus.command2.core.Command;
import com.dsh105.nexus.command2.core.CommandAlias;
import com.dsh105.nexus.command2.core.CommandPermissions;
import com.dsh105.nexus.command2.core.NestedCommand;
import com.dsh105.nexus.command2.exceptions.*;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class CommandRegistrationService {

    protected CommandManager commandManager;

    protected PermissionHandler permissionHandler;

    protected Map<Method, Map<String, Method>> commands = new HashMap<Method, Map<String, Method>>();

    protected Map<Method, Object> instances = new HashMap<Method, Object>();

    protected Map<String, String> descriptions = new HashMap<String, String>();

    protected Map<String, String> helpMessages = new HashMap<String, String>();

    private Instantiator objectInstantiator;

    public CommandRegistrationService(CommandManager commandManager, PermissionHandler permissionHandler) {
        this.commandManager = commandManager;
        this.permissionHandler = permissionHandler;
    }

    public void register(Class<?> clazz) {
        registerMethods(clazz, null);
    }

    public List<Command> registerAndReturn(Class<?> clazz) {
        return registerMethods(clazz, null);
    }

    protected List<Command> registerMethods(Class<?> clazz, Method parent) {
        if (getInstantiator() != null) {
            return registerMethods(clazz, parent, getInstantiator().instantiate(clazz));
        } else {
            return registerMethods(clazz, parent, null);
        }
    }

    protected List<Command> registerMethods(Class<?> clazz, Method parent, Object instance) {
        Map<String, Method> map;
        List<Command> registeredCommands = new ArrayList<Command>();

        if (commands.containsKey(parent)) {
            map = commands.get(parent);
        } else {
            map = new HashMap<String, Method>();
            commands.put(parent, map);
        }

        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(Command.class))
                continue;

            Command cmd = method.getAnnotation(Command.class);

            map.put(cmd.name(), method);
            for (String alias : cmd.aliases()) {
                map.put(alias, method);
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                if (instance == null) {
                    continue;
                }
                instances.put(method, instance);
            }

            if (parent == null) {
                final String commandName = cmd.name();
                final String desc = cmd.description();

                final String usage = cmd.usage();
                if (usage.length() == 0) {
                    descriptions.put(commandName, desc);
                } else {
                    descriptions.put(commandName, usage + " - " + desc);
                }

                String help = cmd.help();
                if (help.length() == 0) {
                    help = desc;
                }
                final String commandPrefix = Nexus.getInstance().getConfig().getCommandPrefix();
                final CharSequence arguments = getArguments(cmd);
                for (String alias : cmd.aliases()) {
                    final String helpMessage = commandPrefix + alias + " " + arguments + "\n\n" + help;
                    final String key = alias.replaceAll(commandPrefix, "");
                    String previous = helpMessages.put(key, helpMessage);

                    if (previous != null && !previous.replaceAll("^" + commandPrefix + "[^ ]+ ", "").equals(helpMessage.replaceAll("^" + commandPrefix + "[^ ]+ ", ""))) {
                        helpMessages.put(key, previous + "\n\n" + helpMessage);
                    }
                }
            }
            registeredCommands.add(cmd);

            if (method.isAnnotationPresent(NestedCommand.class)) {
                NestedCommand nestedCmd = method.getAnnotation(NestedCommand.class);

                for (Class<?> nestedCls : nestedCmd.value()) {
                    registerMethods(nestedCls, method);
                }
            }
        }

        if (clazz.getSuperclass() != null) {
            registerMethods(clazz.getSuperclass(), parent, instance);
        }

        return registeredCommands;
    }

    private boolean hasPermission(User user, Method method) {
        CommandPermissions permissions = method.getAnnotation(CommandPermissions.class);

        if (permissions == null) {
            return true;
        }

        for (String permission : permissions.value()) {
            return this.permissionHandler.checkPermission(user, permission);
        }

        return false;
    }

    protected String getUsage(String[] args, int level, Command cmd) {
        final StringBuilder command = new StringBuilder();

        command.append(Nexus.getInstance().getConfig().getCommandPrefix());

        for (int i = 0; i <= level; ++i) {
            command.append(args[i]);
            command.append(' ');
        }
        command.append(getArguments(cmd));

        final String help = cmd.help();
        if (help.length() > 0) {
            command.append("\n\n");
            command.append(help);
        }

        return command.toString();
    }

    protected CharSequence getArguments(Command cmd) {
        final String flags = cmd.flags();

        final StringBuilder command2 = new StringBuilder();
        if (flags.length() > 0) {
            String flagString = flags.replaceAll(".:", "");
            if (flagString.length() > 0) {
                command2.append("[-");
                for (int i = 0; i < flagString.length(); ++i) {
                    command2.append(flagString.charAt(i));
                }
                command2.append("] ");
            }
        }

        command2.append(cmd.usage());

        return command2;
    }

    protected String getNestedUsage(String[] args, int level, Method method, User user) throws CommandException {
        StringBuilder command = new StringBuilder();

        command.append(Nexus.getInstance().getConfig().getCommandPrefix());

        for (int i = 0; i <= level; ++i) {
            command.append(args[i] + " ");
        }

        Map<String, Method> map = commands.get(method);
        boolean found = false;

        command.append("<");

        Set<String> allowedCommands = new HashSet<String>();

        for (Map.Entry<String, Method> entry : map.entrySet()) {
            Method childMethod = entry.getValue();
            found = true;

            if (hasPermission(user, childMethod)) {
                Command childCmd = childMethod.getAnnotation(Command.class);

                allowedCommands.add(childCmd.aliases()[0]);
            }
        }

        if (allowedCommands.size() > 0) {
            command.append(StringUtil.join(allowedCommands, "|"));
        } else {
            if (!found) {
                command.append("?");
            } else {
                throw new CommandPermissionsException();
            }
        }

        command.append(">");

        return command.toString();
    }

    public Map<String, String> getCommands() {
        return this.descriptions;
    }

    public Map<String, String> getHelpMessages() {
        return this.helpMessages;
    }

    public Map<Method, Map<String, Method>> getMethods() {
        return this.commands;
    }

    public boolean hasCommand(String command) {
        return commands.get(null).containsKey(command.toLowerCase());
    }

    public void execute(String cmd, String[] args, User user, Channel channel) throws CommandException {
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = cmd;

        executeMethod(null, newArgs, user, channel, 0);
    }

    public void execute(String[] args, User user, Channel channel) throws CommandException {
        executeMethod(null, args, user, channel, 0);
    }

    public void executeMethod(Method parent, String[] args, User user, Channel channel, int level) throws CommandException {
        String cmdName = args[level];

        Map<String, Method> map = commands.get(parent);
        Method method = map.get(cmdName.toLowerCase());

        if (method == null) {
            if (parent == null) { // Root
                throw new UnhandledCommandException();
            } else {
                throw new MissingNestedCommandException("Unknown command: " + cmdName, getNestedUsage(args, level - 1, parent, user));
            }
        }

        checkPermission(user, method);

        int argsCount = args.length - 1 - level;

        boolean executeNested = method.isAnnotationPresent(NestedCommand.class) && (argsCount > 0 || !method.getAnnotation(NestedCommand.class).executeBody());

        if (executeNested) {
            if (argsCount == 0) {
                throw new MissingNestedCommandException("Sub-command required.",
                        getNestedUsage(args, level, method, user));
            } else {
                executeMethod(method, args, user, channel, level + 1);
            }
        } else if (method.isAnnotationPresent(CommandAlias.class)) {
            CommandAlias aCmd = method.getAnnotation(CommandAlias.class);
            executeMethod(parent, aCmd.value(), user, channel, level);
        } else {
            Command cmd = method.getAnnotation(Command.class);

            String[] newArgs = new String[args.length - level];
            System.arraycopy(args, level, newArgs, 0, args.length - level);

            final Set<Character> valueFlags = new HashSet<Character>();

            char[] flags = cmd.flags().toCharArray();
            Set<Character> newFlags = new HashSet<Character>();
            for (int i = 0; i < flags.length; ++i) {
                if (flags.length > i + 1 && flags[i + 1] == ':') {
                    valueFlags.add(flags[i]);
                    ++i;
                }
                newFlags.add(flags[i]);
            }

            CommandContext context = new CommandContext(newArgs, valueFlags);

            if (context.argsLength() < cmd.minArgs()) {
                throw new CommandUsageException("Too few arguments.", getUsage(args, level, cmd));
            }

            if (cmd.maxArgs() != -1 && context.argsLength() > cmd.maxArgs()) {
                throw new CommandUsageException("Too many arguments.", getUsage(args, level, cmd));
            }

            if (!cmd.anyFlags()) {
                for (char flag : context.getFlags()) {
                    if (!newFlags.contains(flag)) {
                        throw new CommandUsageException("Unknown flag: " + flag, getUsage(args, level, cmd));
                    }
                }
            }

            Object instance = instances.get(method);

            invokeMethod(method, instance, context, user, channel);
        }
    }

    protected void checkPermission(User user, Method method) throws CommandException {
        if (!hasPermission(user, method)) {
            throw new CommandPermissionsException();
        }
    }

    public void invokeMethod(Method method, Object instance, CommandContext commandContext, User user, Channel channel) throws CommandException {
        try {
            method.invoke(instance, commandContext, user, channel);
        } catch (IllegalArgumentException e) {
            Nexus.LOGGER.warning("Failed to execute command!");
            e.printStackTrace();
        } catch (IllegalAccessException e) {   // http://www.troll.me/images/obama-laughing/oh-juan-you-silly-goose.jpg
            Nexus.LOGGER.warning("Failed to execute command!");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException) {
                throw (CommandException) e.getCause();
            }

            throw new WrappedCommandException(e.getCause());
        }
    }

    public Instantiator getInstantiator() {
        return this.objectInstantiator;
    }

    public void setInstantiator(Instantiator objectInstantiator) {
        this.objectInstantiator = objectInstantiator;
    }
}
