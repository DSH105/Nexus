package com.dsh105.nexus.api.user;

import com.dsh105.nexus.api.permission.Permissible;

public interface User extends Permissible {

    public org.pircbotx.User getAsIrcUser();

}
