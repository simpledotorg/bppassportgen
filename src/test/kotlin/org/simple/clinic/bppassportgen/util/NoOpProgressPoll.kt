package org.simple.clinic.bppassportgen.util

import org.simple.bppassportgen.progresspoll.ProgressPoll

class NoOpProgressPoll : ProgressPoll {
  override fun poll() {}
}
