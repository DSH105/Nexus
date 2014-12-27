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
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ResponseManager {

    private HashMap<ResponseTrigger, ArrayList<String>> responses = new HashMap<>();
    private Random r = new Random();

    public ArrayList<String> getResponsesFor(ResponseTrigger trigger) {
        return getResponsesFor(trigger.getTrigger());
    }

    public ArrayList<String> getResponsesFor(String trigger) {
        for (Map.Entry<ResponseTrigger, ArrayList<String>> entry : responses.entrySet()) {
            if (entry.getKey().getTrigger().equals(trigger)) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return null;
    }

    public ResponseTrigger getTriggerInstance(String trigger) {
        for (Map.Entry<ResponseTrigger, ArrayList<String>> entry : responses.entrySet()) {
            if (entry.getKey().getTrigger().equals(trigger)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void addResponses(ResponseTrigger trigger, String... responses) {
        ArrayList<String> existing = this.responses.get(trigger);
        if (existing == null) {
            existing = new ArrayList<>();
        }
        for (String response : responses) {
            existing.add(response);
        }
        this.responses.put(trigger, existing);
    }

    public void load() {
        FileInputStream input = null;
        try {
            File responsesFolder = new File("responses");
            if (!responsesFolder.exists()) {
                responsesFolder.mkdirs();
            }
            for (File f : responsesFolder.listFiles()) {
                int extIndex = f.getName().lastIndexOf(".");
                String extension = "";
                if (extIndex > 0) {
                    extension = f.getName().substring(extIndex + 1);
                }
                if (extension.equalsIgnoreCase("YML")) {
                    String triggerWord = f.getName().substring(0, extIndex);

                    input = new FileInputStream(f);
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = (Map<String, Object>) yaml.load(input);
                    if (data != null && !data.isEmpty()) {
                        try {
                            ArrayList<String> responses = (ArrayList<String>) data.get("responses");
                            int chance = (Integer) data.get("chance");
                            this.responses.put(new ResponseTrigger(chance, triggerWord), responses);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            Nexus.LOGGER.severe("Could not load responses!");
            e.printStackTrace();
        } finally {

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Nexus.LOGGER.severe("Failed to close output stream whilst loading reminders");
                    e.printStackTrace();
                }
            }
        }
    }

    public void save() {
        for (Map.Entry<ResponseTrigger, ArrayList<String>> entry : responses.entrySet()) {
            ResponseTrigger trigger = entry.getKey();
            ArrayList<String> responses = entry.getValue();

            File rootFolder = new File("responses");
            if (!rootFolder.exists()) {
                rootFolder.mkdirs();
            }
            PrintWriter writer = null;
            try {
                File file = new File(rootFolder, trigger.getTrigger() + ".yml");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();

                HashMap<String, Object> valueMap = new HashMap<>();
                valueMap.put("chance", trigger.getChance());
                valueMap.put("responses", responses);

                writer = new PrintWriter(new FileOutputStream(file));
                Yaml yaml = new Yaml();
                writer.write(yaml.dump(valueMap));
                writer.close();
            } catch (IOException e) {
                Nexus.LOGGER.severe("Could not save reminders!");
                e.printStackTrace();
                if (writer != null) {
                    writer.close();
                }
            }
        }
        this.responses.clear();
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
            String randomResponse = getRandomResponse(possibleResponses);
            if (channel == null) {
                Nexus.getInstance().sendIRC().message(user.getNick(), ResponseFormatter.appendReplacements(randomResponse, user, channel));
            } else {
                Nexus.getInstance().sendIRC().message(channel.getName(), ResponseFormatter.appendReplacements(randomResponse, user, channel));
            }
            return true;
        }
        return false;
    }
}