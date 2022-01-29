package org.simple.clinic.bppassportgen.scheduler

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.simple.bppassportgen.scheduler.SchedulersProvider

class TestSchedulersProvider : SchedulersProvider {
  override val io: Scheduler
    get() = Schedulers.trampoline()

  override val computation: Scheduler
    get() = Schedulers.trampoline()
}
