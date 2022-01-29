package org.simple.bppassportgen.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

interface SchedulersProvider {
  val io: Scheduler
  val computation: Scheduler
}

class RealSchedulersProvider : SchedulersProvider {
  override val io: Scheduler
    get() = Schedulers.io()

  override val computation: Scheduler
    get() = Schedulers.computation()
}
