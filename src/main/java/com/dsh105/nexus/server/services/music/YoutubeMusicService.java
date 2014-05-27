package com.dsh105.nexus.server.services.music;

import com.dsh105.nexus.server.NexusServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YoutubeMusicService {

    private static final Logger logger = LogManager.getLogger("MusicService");

    private final NexusServer nexusServer;

    public YoutubeMusicService(final NexusServer nexusServer) {
        this.nexusServer = nexusServer;
    }
}
