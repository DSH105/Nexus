package com.dsh105.nexus.server.decoder;

import com.dsh105.nexus.server.decoder.types.MusicRequest;
import com.dsh105.nexus.server.utils.ObjectEnum;

public class DecoderType {

    public static class MusicService extends ObjectEnum<DecoderType> {

        private static MusicService instance = new MusicService();

        // Decoders
        public static final DecoderType GET_CURRENT_SONG = new DecoderType(new MusicRequest());

        private MusicService() {
            super(DecoderType.class);
        }
    }

    private final RequestDecoder<?> requestDecoder;

    private DecoderType(final RequestDecoder<?> requestDecoder) {
        this.requestDecoder = requestDecoder;
    }

    public RequestDecoder<?> getRequestDecoder() {
        return this.requestDecoder;
    }
}
