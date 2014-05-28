package com.dsh105.nexus.script.compiler

import java.io.{PrintWriter, StringWriter, File}
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.ConsoleReporter

trait Compiler {
   def compile(file: File)
}

class Scala extends Compiler {

  val encoding = "UTF-8"
  val generator: CodeGenerator = null

  private var scriptDir: File = null

  def init(scriptDir: File) = {
    this.scriptDir = scriptDir
    this.scriptDir.mkdirs
  }

  def compile(file: File) {
    val settings = new Settings()

    val stringWriter = new StringWriter();
    val reporter = new ConsoleReporter(settings, Console.in, new PrintWriter(stringWriter))

    val compiler = new Global(settings, reporter)

    (new compiler.Run) compile List(scriptDir.getCanonicalPath)
  }
}
