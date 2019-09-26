package org.simple.clinic.bppassportgen.util

import org.simple.bppassportgen.consoleprinter.ConsolePrinter

class NoOpConsolePrinter : ConsolePrinter {
  override fun print(message: String) {}
}
