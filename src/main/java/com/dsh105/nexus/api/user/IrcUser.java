package com.dsh105.nexus.api.user;

import com.dsh105.nexus.permissions.Permission;

import java.util.List;

public class IrcUser implements User {

    private final org.pircbotx.User user;

    public IrcUser(final org.pircbotx.User user) {
        this.user = user;
    }

    @Override
    public org.pircbotx.User getAsIrcUser() {
        return this.user;
    }

    @Override
    public String getName() {
        return this.user.getNick();
    }

    @Override
    public void sendMessage(String message) {
        this.user.send().message(message);
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isIrcUser() {
        return true;
    }

    // TODO
    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    // TODO
    @Override
    public boolean hasPermission(Permission permission) {
        return false;
    }

    // TODO
    @Override
    public List<Permission> getPermissions() {
        return null;
    }
}
