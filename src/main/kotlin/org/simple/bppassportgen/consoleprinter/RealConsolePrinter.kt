package org.simple.bppassportgen.consoleprinter

class RealConsolePrinter : ConsolePrinter {
  override fun print(message: String) {
    println(message)
  }
}
