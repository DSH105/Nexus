package com.dsh105.nexus.api.command;

import com.dsh105.nexus.api.attach.Attachment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommandSender {

    private ConcurrentHashMap<String, Attachment<?>> attachments = new ConcurrentHashMap<>();

    public abstract String getName();

    public abstract void sendMessage(final String message);

    public abstract boolean isConsole();

    public abstract boolean isIrcUser();

    public boolean hasAttachments() {
        return !this.attachments.isEmpty();
    }

    public Map<String, Attachment<?>> getAttachments() {
        if (!hasAttachments()) {
            return null;
        }
        return this.attachments;
    }

    public <T> Attachment<T> get(String key) {
        return getAttachments() == null ? null : (Attachment<T>) getAttachments().get(key);
    }

    public void setAttachment(final String key, final Attachment<?> attachment) {
        if (!hasAttachments()) {
            this.attachments.put(key, attachment);
        }

        if (getAttachments().containsKey(key)) {
            throw new RuntimeException("Already registered an attachment with name: " + key);
        }

        this.attachments.put(key, attachment);
    }
}
