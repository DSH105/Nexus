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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

@Exclude
@Command(command = "dynamic",
        needsChannel = false,
        help = "Dynamic command",
        extendedHelp = "Dynamic command")
public class DynamicCommand extends CommandModule {

    private String command;
    private String response;
    private boolean needsChannel = false;
    private String help;
    private String[] extendedHelp;
    private String[] aliases = new String[0];
    private boolean action = false;

    public DynamicCommand(String command, String response, boolean needsChannel, String help, String[] extendedHelp, String[] aliases) {
        this(command, response, needsChannel, help, extendedHelp, aliases, false);
    }

    public DynamicCommand(String command, String response, boolean needsChannel, String help, String[] extendedHelp, String[] aliases, boolean action) {
        this.command = command;
        this.response = response;
        this.needsChannel = needsChannel;
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.aliases = aliases;
        this.action = action;

        this.prepare();
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (this.action) {
            Nexus.getInstance().sendIRC().action(event.getSender().getNick(), appendReplacements(event));
        } else {
            event.respond(appendReplacements(event));
        }
        return true;
    }

    public String appendReplacements(CommandPerformEvent event) {
        String response = this.response
                .replace("%s", event.getSender().getNick())
                .replace("%c", event.getChannel().getName());

        for (int i = 0; i < event.getArgs().length; i++) {
            response.replace("%a" + i, event.getArgs()[i]);
        }

        return event.getManager().format(null, response);
    }

    public String getResponse() {
        return response;
    }

    private void prepare() {
        final Command existingAnnotation = this.info();
        Command annotation = new Command() {

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
                return new CommandGroup[] {CommandGroup.DYNAMIC};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return existingAnnotation.annotationType();
            }
        };

        // hacky stuff, don't try this at home kids! - TODO: use CaptainBern's awesome library

        try {
            Field field = Class.class.getDeclaredField("annotations");
            field.setAccessible(true);
            Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(this.getClass());
            annotations.put(Command.class, annotation);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Oh noes!
            throw new DynamicCommandRegistrationFailedException(e);
        }
    }
}