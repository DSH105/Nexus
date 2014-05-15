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

package com.dsh105.nexus.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NicksConfig extends YamlConfig {

    private HashMap<String, String> nicks = new HashMap<>();

    public NicksConfig() {
        super("nicks.yml");
        this.setDefaults();
        this.load();
    }

    @Override
    public void setDefaults() {
    }

    @Override
    public void loadData(Map<String, Object> loadedData) {
        super.loadData(loadedData);
        ArrayList<String> loadedNicks = this.get("nicks", new ArrayList<String>());
        for (String s : loadedNicks) {
            String[] parts = s.split(":");
            this.nicks.put(parts[0], parts[1]);
        }
    }

    @Override
    public void save() {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : nicks.entrySet()) {
            list.add(entry.getKey() + ":" + entry.getValue());
        }
        this.set("nicks", list.toArray(new String[list.size()]));
        super.save();
    }

    public HashMap<String, String> getAllStoredNicks() {
        return new HashMap<>(nicks);
    }

    public String getAccountNameFor(String nick) {
        return nicks.get(nick.replaceAll("\\W", ""));
    }

    public void storeNick(String nick, String account) {
        nicks.put(nick.replaceAll("\\W", ""), account.replaceAll("\\W", ""));
    }
}