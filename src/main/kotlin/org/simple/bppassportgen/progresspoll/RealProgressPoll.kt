package org.simple.bppassportgen.progresspoll

import java.time.Duration

class RealProgressPoll(private val pollInterval: Duration) : ProgressPoll {
  override fun poll() {
    Thread.sleep(pollInterval.toMillis())
  }
}
