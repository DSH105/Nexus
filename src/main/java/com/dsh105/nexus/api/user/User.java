package com.dsh105.nexus.api.user;

import com.dsh105.nexus.api.command.CommandSender;
import com.dsh105.nexus.api.permission.Permissible;

public interface User extends CommandSender, Permissible {

    public org.pircbotx.User getAsIrcUser();

}
