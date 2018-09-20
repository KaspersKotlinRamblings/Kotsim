package kotsim

import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.*

private typealias SimStep = Continuation<Unit>

internal class SimulationClass : Simulation {


    override fun buildSimProcess(name: String, block: suspend SimulationProcess.() -> Unit): SimulationProcess {
        fun wrap(inBlock: suspend SimulationProcess.() -> Unit): suspend SimulationProcess.() -> Unit {
            return inBlock
        }

        val co = SimProcess(name)
        val blk = wrap {
            log("starting $name")
            block(co)
            log("terminating $name")
        }
        co.nextStep = blk.createCoroutine(co, co)
        queue.add(co)
        return co
    }

    override fun buildResource(capacity: Int, name: String) : Resource{
        return ResourceClass(capacity,name)
    }
    internal inner class SimProcess(val name: String) : SimulationProcess, SimStep {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        internal lateinit var nextStep: SimStep
        private var done = false

        internal val isDone get() = done
        internal var nextTime: Time = 0

        override fun resumeWith(result: SuccessOrFailure<Unit>) {
            done = true
        }

        fun resume() {
            if (done) throw IllegalStateException("Cannot resume finished SimProcess")
            nextStep.resume(Unit)
        }

        override suspend fun pause() {
            return suspendCoroutine { continuation -> this.nextStep = continuation }
        }

        override suspend fun hold(holdTime: Int) {
            nextTime = now + holdTime
            queue.add(this)
            pause()
        }
    }

    inner class ResourceClass(capacity: Int, private val name: String = "res") : Resource {
        private inner class ResourceRequest(val process: SimProcess, val amount: Int)

        private var idleCount = capacity
        private val waiting: Queue<ResourceRequest> = ArrayDeque<ResourceRequest>()

        override suspend fun acquire(amount: Int) {
            if (idleCount >= amount) { // We have what is asked for
                log("taking resource $name[$amount/$idleCount]")
                idleCount -= amount
            } else { // not enough idle resources
                log("awaiting resource $name[$amount/$idleCount]")
                waiting.add(ResourceRequest(curProcess, amount))
                curProcess.pause()
            }
        }

        override suspend fun release(amount: Int) {
            if (waiting.size > 0) { // is anyone waiting to grab the released resources
                val waitFor = waiting.peek().amount // what is the first in the queue waiting for
                if (waitFor <= amount + idleCount) { // we release more than what the first is waiting for
                    with(waiting.poll().process) {
                        nextTime = now
                        queue.add(this)
                    }
                    idleCount = idleCount + amount - waitFor // claim our share
                    log("claiming $name[$amount/$idleCount]")
                    curProcess.hold(0)
                } else { // not enough released for first to claim,
                    idleCount += amount
                    log("increasing $name[$amount/$idleCount]")
                }
            } else { // noone was waiting, so release them
                idleCount += amount
                log("relasing $name[$amount/$idleCount]")
            }
        }

        override suspend fun use(amount: Int, block: suspend () -> Unit) {
            acquire(amount)
            block()
            release(amount)
        }
    }

    private var queue = PriorityQueue<SimProcess>(10, compareBy(SimProcess::nextTime))

    internal lateinit var curProcess: SimProcess
    override val current: SimulationProcess get() = curProcess

    override var now: Time = 0


    override fun log(msg: String) {
        println("${curProcess.name} @ $now: $msg")
    }

    fun runSimulation() {
        while (queue.isNotEmpty()) {
            curProcess = queue.poll()
            now = curProcess.nextTime
            if (!curProcess.isDone) {
                log("resuming ${curProcess.name}")
                curProcess.resume()
            }
        }
    }
}