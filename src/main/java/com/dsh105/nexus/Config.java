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

package com.dsh105.nexus;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {

    private LinkedHashMap<String, Object> options = new LinkedHashMap<>();
    private ArrayList<String> channels = new ArrayList<>();
    private String fileName;

    private void saveDefaults() {
        channels.add(Nexus.ADMIN_CHANNEL);
        this.options.put("server", "irc.esper.net");
        this.options.put("port", 5555);
        this.options.put("channels", channels);
        this.options.put("command-prefix", ";");
        this.options.put("nick", "Nexus");
        this.options.put("server-password", "");
        this.options.put("account-password", "");
        this.save();
    }

    public String getFileName() {
        return fileName;
    }

    public void save() {
        this.set("channels", channels);
        try {
            File file = new File(this.getFileName());
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            Yaml yaml = new Yaml();
            writer.write(yaml.dump(this.options));
        } catch (IOException e) {
            Nexus.LOGGER.severe("Failed to save configuration file: " + fileName);
        }
    }

    public static Config load(String fileName) {
        Config config = new Config();
        config.fileName = fileName;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream input = new FileInputStream(file);
            Yaml yaml = new Yaml();
            Map<String, Object> loaded = (Map<String, Object>) yaml.load(input);
            for (String key : loaded.keySet()) {
                config.options.put(key, loaded.get(key));
            }
        } catch (IOException e) {
            Nexus.LOGGER.severe("Failed to load configuration file: " + fileName);
        }
        config.save();
        return config;
    }

    public void set(String path, Object value) {
        this.options.put(path, value);
    }

    public Object get(String path) {
        return options.get(path);
    }

    public <T> T get(String path, T defaultValue) {
        Object value = this.get(path);
        if (value != null && defaultValue.getClass().isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    public String getServer() {
        return get("server", "irc.esper.net");
    }

    public int getPort() {
        return get("port", 5555);
    }

    public ArrayList<String> getChannels() {
        return channels;
    }

    public String getAccountPassword() {
        return get("account-password", "");
    }

    public String getServerPassword() {
        return get("server-password", "");
    }

    public String getCommandPrefix() {
        return get("command-prefix", "%");
    }
}