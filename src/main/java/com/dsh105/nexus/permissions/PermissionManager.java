package com.dsh105.nexus.permissions;

import com.dsh105.nexus.command2.PermissionHandler;
import org.pircbotx.User;

public class PermissionManager implements PermissionHandler {

    public PermissionManager() {

    }

    private void loadPermissions() {

    }

    @Override
    public boolean checkPermission(User user, String permission) {
        return false;
    }
}
