package com.dsh105.nexus.api.permission;

import com.dsh105.nexus.permissions.Permission;

import java.util.List;

public interface Permissible {

    public boolean hasPermission(final String permission);

    public boolean hasPermission(final Permission permission);

    public List<Permission> getPermissions();

}
