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

package com.dsh105.nexus.response;

import com.dsh105.nexus.Nexus;
import org.pircbotx.Channel;
import org.pircbotx.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ResponseManager {

    private HashMap<ResponseTrigger, ArrayList<String>> responses = new HashMap<>();
    private Random r = new Random();

    public ResponseManager() {
        this.load();
    }

    public void load() {
        try {
            File responsesFolder = new File("responses");
            if (!responsesFolder.exists()) {
                responsesFolder.mkdirs();
            }
            for (File f : responsesFolder.listFiles()) {
                if (f.getName().startsWith("responses-")) {
                    BufferedReader reader = null;
                    try {
                        String s = f.getName().split("-")[1];
                        ArrayList<String> responses = new ArrayList<>();
                        reader = new BufferedReader(new FileReader(f));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responses.add(line);
                        }
                        reader.close();

                        this.responses.put(new ResponseTrigger(Nexus.getInstance().getConfig().get("responses." + s + ".chance", 5), s), responses);
                    } catch (IOException e) {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            Nexus.LOGGER.severe("Could not load responses!");
            e.printStackTrace();
        }
    }

    public boolean trigger(Channel channel, User user, String message) {
        for (ResponseTrigger trigger : responses.keySet()) {
            if (message.toLowerCase().contains(trigger.getTrigger())) {
                return this.onMention(trigger, channel, user, message, responses.get(trigger));
            }
        }
        return false;
    }

    public String getRandomResponse(ArrayList<String> responses) {
        return responses.get(r.nextInt(responses.size()));
    }

    public boolean onMention(ResponseTrigger trigger, Channel channel, User user, String message, ArrayList<String> possibleResponses) {
        if (r.nextInt(100) < trigger.getChance()) {
            Nexus.getInstance().sendMessage(channel, getRandomResponse(possibleResponses));
            return true;
        }
        return false;
    }
}