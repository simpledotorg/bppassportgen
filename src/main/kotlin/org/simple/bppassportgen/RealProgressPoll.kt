package org.simple.bppassportgen

import java.time.Duration

class RealProgressPoll(private val pollInterval: Duration) : ProgressPoll {
  override fun poll() {
    Thread.sleep(pollInterval.toMillis())
  }
}
