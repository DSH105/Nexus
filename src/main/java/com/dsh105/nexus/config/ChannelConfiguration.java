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

import com.dsh105.nexus.Nexus;

import java.io.File;
import java.util.HashMap;

public class ChannelConfiguration {

    private HashMap<String, ChannelConfig> channels = new HashMap<>();
    private ChannelConfig globalChannelConfig;

    public ChannelConfiguration() {
        globalChannelConfig = new ChannelConfig("GLOBAL");
        File channelsFolder = new File("channels");
        if (!channelsFolder.exists()) {
            channelsFolder.mkdirs();
        }
        for (File file : channelsFolder.listFiles()) {
            int extIndex = file.getName().lastIndexOf(".");
            String extension = "";
            if (extIndex > 0) {
                extension = file.getName().substring(extIndex + 1);
            }
            if (extension.equalsIgnoreCase("YML")) {
                String chanName = file.getName().substring(0, extIndex);
                this.channels.put(chanName, new ChannelConfig(chanName));
            }
        }
    }

    public ChannelConfig getChannel(String channelName) {
        if (channelName.equalsIgnoreCase("GLOBAL")) {
            return globalChannelConfig;
        }
        if (!channelName.startsWith("#")) {
            channelName = "#" + channelName;
        }
        ChannelConfig existing = channels.get(channelName);
        if (existing == null) {
            if (Nexus.getInstance().getChannel(channelName) != null) {
                ChannelConfig config = new ChannelConfig(channelName);
                channels.put(channelName, config);
            }
        }
        return existing;
    }

    public void removeChannel(String channelName) {
        this.removeChannel(channelName, false);
    }

    public void removeChannel(String channelName, boolean clearFile) {
        ChannelConfig config = channels.get(channelName);
        if (config != null) {
            channels.remove(channelName);
            if (clearFile) {
                config.clearFile();
            }
        }
    }
}