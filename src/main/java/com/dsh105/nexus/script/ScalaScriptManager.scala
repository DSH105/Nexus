package com.dsh105.nexus.script

import com.dsh105.nexus.Nexus
import com.dsh105.nexus.command.CommandManager

class ScalaScriptManager(nexus: Nexus) {

  val botInstance = nexus

  def getNexusInstance: Nexus = {
    botInstance
  }

  def getCommandManager: CommandManager = {
    botInstance.getCommandManager
  }

  def getLang: String = {
    "Scala";
  }
}
