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

package com.dsh105.nexus.command.module;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.TimeUtil;
import org.pircbotx.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

@Command(command = "remind", help = "Schedule a reminder", extendedHelp = "")
public class RemindCommand extends CommandModule {

    private ArrayList<Reminder> reminders = new ArrayList<>();

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length >= 2) {
            long timePeriod = TimeUtil.parse(event.getArgs()[0]);
            if (timePeriod <= 0) {
                event.respondWithPing("Invalid time period entered: {0}. Examples: {1} (1 day), {2} (2 hours), {3} (5 minutes), {4} (20 seconds)", event.getArgs()[0], "1d", "2h", "5m", "20s");
                return true;
            }
            String reminder = StringUtil.combineSplit(1, event.getArgs(), " ");
            new Timer(true).schedule(new Reminder(event.getChannel(), event.getSender().getNick(), reminder), timePeriod * 1000);
            event.respondWithPing("Reminder scheduled for {0}", event.getArgs()[0]);
            return true;
        }
        return false;
    }

    public ArrayList<Reminder> getReminders() {
        return new ArrayList<>(reminders);
    }

    public void clearReminders() {
        Iterator<Reminder> i = reminders.iterator();
        while (i.hasNext()) {
            Reminder r = i.next();
            r.cancel();
            i.remove();
        }
    }

    public class Reminder extends TimerTask {

        private Channel channel;
        private String userToRemind;
        private String reminder;

        public Reminder(Channel channel, String userToRemind, String reminder) {
            this.channel = channel;
            this.userToRemind = userToRemind;
            this.reminder = reminder;
        }

        @Override
        public void run() {
            if (channel != null) {
                Nexus.getInstance().sendAction(channel, "reminds " + userToRemind + " to " + reminder);
            }
        }
    }
}