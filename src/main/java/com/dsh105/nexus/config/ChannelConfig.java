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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChannelConfig extends YamlConfig {

    protected String channelName;
    private ArrayList<String> disabledCommands = new ArrayList<>();

    public ChannelConfig(String channelName) {
        super("channels" + File.separator + channelName + ".yml");
        this.channelName = channelName;
        this.setDefaults();
        this.load();
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public void setDefaults() {
    }

    @Override
    public void loadData(Map<String, Object> loadedData) {
        super.loadData(loadedData);
        this.disabledCommands = this.get("disabledCommands", disabledCommands);
        this.set("disabledCommands", disabledCommands);
    }

    @Override
    public void save() {
        this.set("disabledCommands", disabledCommands);
        super.save();
    }

    public ArrayList<String> getDisabledCommands() {
        return new ArrayList<>(disabledCommands);
    }

    public void setCommandEnabled(String command) {
        this.setCommandStatus(command, true);
    }

    public void setCommandDisabled(String command) {
        this.setCommandStatus(command, false);
    }

    public void setCommandStatus(String command, boolean enabled) {
        if (enabled) {
            this.disabledCommands.remove(command);
        } else {
            if (!this.disabledCommands.contains(command)) {
                this.disabledCommands.add(command);
            }
        }
        this.save();
    }

    public boolean isEnabled(String command) {
        return !isDisabled(command);
    }

    public boolean isDisabled(String command) {
        return disabledCommands.contains(command) || disabledCommands.contains("all");
    }
}