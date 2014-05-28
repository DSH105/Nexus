package com.dsh105.nexus.server.services.music;

import com.google.common.collect.Queues;

import java.util.Queue;

public class PlayList {

    private final Queue<Song> playList = Queues.newConcurrentLinkedQueue();

    public PlayList() {}

    public void addSong(final Song song) {
        this.playList.add(song);
    }

    public Song getNextSong() {
        return this.playList.element();
    }

    public void removeSong() {
        this.playList.remove();
    }
}
