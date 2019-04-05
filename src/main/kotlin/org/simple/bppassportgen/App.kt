package org.simple.bppassportgen

import java.util.logging.Logger

fun main() {
  App().run()
}

class App {

  val logger = Logger.getLogger("App")

  fun run() {
    logger.info("RUNNING")
  }
}
