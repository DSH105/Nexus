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

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.command.Exclude;
import com.dsh105.nexus.command.module.CommandGroup;
import com.dsh105.nexus.exception.general.DynamicCommandRegistrationFailedException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Exclude
@Command(command = "dynamic",
        needsChannel = false,
        help = "Dynamic command",
        extendedHelp = "Dynamic command")
public class DynamicCommand extends CommandModule {

    private Command customInfo;

    private String command;
    private String response;
    private boolean needsChannel = false;
    private String help;
    private String[] extendedHelp;
    private String[] aliases = new String[0];
    private boolean action = false;
    private boolean commandResponse = false;

    public DynamicCommand(String command, String response, boolean needsChannel, String help, String[] extendedHelp, String[] aliases) {
        this(command, response, needsChannel, help, extendedHelp, aliases, false, false);
    }

    public DynamicCommand(String command, String response, boolean needsChannel, String help, String[] extendedHelp, String[] aliases, boolean action, boolean commandResponse) {
        this.command = command;
        this.response = response;
        this.needsChannel = needsChannel;
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.aliases = aliases;
        this.action = action;
        this.commandResponse = commandResponse;

        this.prepare();
    }

    public static void loadCommands() {
        File commandsFolder = new File("commands");
        if (!commandsFolder.exists()) {
            commandsFolder.mkdirs();
        }
        for (File file : commandsFolder.listFiles()) {
            int extIndex = file.getName().lastIndexOf(".");
            String extension = "";
            if (extIndex > 0) {
                extension = file.getName().substring(extIndex + 1);
            }
            if (extension.equalsIgnoreCase("YML")) {
                try {
                    FileInputStream input = new FileInputStream(file);
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = (Map<String, Object>) yaml.load(input);
                    if (data != null && !data.isEmpty()) {
                        try {
                            ArrayList<String> extendedHelp = (ArrayList<String>) data.get("extendedHelp");
                            ArrayList<String> aliases = (ArrayList<String>) data.get("aliases");
                            Nexus.getInstance().getCommandManager().register(new DynamicCommand((String) data.get("command"), (String) data.get("response"), (Boolean) data.get("needsChannel"), (String) data.get("help"), extendedHelp.toArray(new String[extendedHelp.size()]), aliases.toArray(new String[aliases.size()]), (Boolean) data.get("action"), (Boolean) data.get("commandResponse")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e) {
                    Nexus.LOGGER.severe("Failed to load dynamic command: " + file.getName().substring(0) + ".");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (this.action) {
            Nexus.getInstance().sendIRC().action(event.getSender().getNick(), appendReplacements(event));
        } else if (this.commandResponse) {
            event.getManager().onCommand(event.getChannel(), event.getSender(), response);
        } else {
            event.respond(appendReplacements(event));
        }
        return true;
    }

    @Override
    public Command info() {
        return customInfo;
    }

    public String appendReplacements(CommandPerformEvent event) {
        String response = this.response
                .replace("%s", event.getSender().getNick())
                .replace("%c", event.isInPrivateMessage() ? "PM" : event.getChannel().getName());

        for (int i = 0; i < event.getArgs().length; i++) {
            response.replace("%a" + i, event.getArgs()[i]);
        }

        return event.getManager().format(null, response);
    }

    public String getResponse() {
        return response;
    }

    public void save() {
        PrintWriter writer = null;
        try {
            File commandsFolder = new File("commands");
            if (!commandsFolder.exists()) {
                commandsFolder.mkdirs();
            }

            File saveFile = new File(commandsFolder, command + ".yml");
            if (saveFile.exists()) {
                saveFile.delete();
            }
            saveFile.createNewFile();

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("command", command);
            valueMap.put("response", response);
            valueMap.put("needsChannel", needsChannel);
            valueMap.put("help", help);
            valueMap.put("extendedHelp", extendedHelp);
            valueMap.put("aliases", aliases);
            valueMap.put("action", action);
            valueMap.put("commandResponse", commandResponse);

            writer = new PrintWriter(saveFile);
            Yaml yaml = new Yaml();
            writer.write(yaml.dump(valueMap));
        } catch (IOException e) {
            Nexus.LOGGER.severe("Failed to save dynamic command: " + command + ".");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void remove() {
        File saveFile = new File("commands" + File.separator + command + ".yml");
        if (saveFile.exists()) {
            saveFile.delete();
        }
        Nexus.getInstance().getCommandManager().unregister(this);
    }

    private void prepare() {
        final Command existingAnnotation = this.info();
        customInfo = new Command() {

            @Override
            public boolean needsChannel() {
                return needsChannel;
            }

            @Override
            public String command() {
                return command;
            }

            @Override
            public String[] aliases() {
                return aliases;
            }

            @Override
            public String[] extendedHelp() {
                return extendedHelp;
            }

            @Override
            public String help() {
                return help;
            }

            @Override
            public CommandGroup[] groups() {
                return new CommandGroup[]{CommandGroup.DYNAMIC};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return existingAnnotation.annotationType();
            }
        };
    }
}