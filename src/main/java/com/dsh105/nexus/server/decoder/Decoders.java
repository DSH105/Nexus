package com.dsh105.nexus.server.decoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class Decoders {

    private static final Logger logger = LogManager.getLogger("Decoders");

    private static final Decoders instance = new Decoders();

    private static final ConcurrentHashMap<String, DecoderType> decoders = new ConcurrentHashMap<>();

    private Decoders() {}

    public static Decoders get() {
        return instance;
    }
}
