package kotsim

import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.*

typealias SimStep = Continuation<Unit>
typealias Time = Int


fun Simulation(block: SimulationClass.() -> Unit) {
    val simulation = SimulationClass()
    block(simulation)
    simulation.runSimulation()
}

class SimulationClass {

    interface SimulationProcess {
        suspend fun pause()
    }

    public fun buildSimProcess(name: String = "SimProcess", block: suspend SimulationProcess.() -> Unit): SimProcess {
        fun surround(prim: suspend SimulationProcess.() -> Unit, inBlock: suspend SimulationProcess.() -> Unit): suspend SimProcess.() -> Unit {
            return inBlock
        }

        val co = SimProcess(name)
        val blk = surround(block) {
            log("starting $name")
            block(co)
            log("terminating $name")
        }
        co.nextStep = blk.createCoroutine(co, co)
        queue.add(co)
        return co
    }

    class SimProcess(val name: String) : SimulationProcess, SimStep {
        override val context: CoroutineContext get() = EmptyCoroutineContext
        lateinit var nextStep: SimStep
        private var done = false;

        val isDone get() = done
        var nextTime: Time = 0

        override fun resumeWith(result: SuccessOrFailure<Unit>) {
            done = true
        }

        fun resume() {
            if (done) throw IllegalStateException("Cannot resume finished SimpleCoroutine")
            nextStep.resume(Unit)
        }

        override suspend fun pause() {
            return suspendCoroutine { continuation -> this.nextStep = continuation }
        }
    }

    inner class Resource(val capacity: Int, val name: String = "res") {
        inner class ResourceRequest(val process: SimProcess, val amount: Int)

        var idleCount = capacity
        val waiting: Queue<ResourceRequest> = ArrayDeque<ResourceRequest>()

        suspend fun acquire(amount: Int = 1) {
            if (idleCount >= amount) { // We have what is asked for
                log("taking resource ${name}[$amount/$idleCount]")
                idleCount -= amount
            } else { // not enough idle resources
                log("awaiting resource ${name}[$amount/$idleCount]")
                waiting.add(ResourceRequest(current, amount))
                current.pause()
            }
        }

        suspend fun release(amount: Int = 1) {
            if (waiting.size > 0) { // is anyone waiting to grab the released resources
                val waitFor = waiting.peek().amount // what is the first in the queue waiting for
                if (waitFor <= amount + idleCount) { // we release more than what the first is waiting for
                    with(waiting.poll().process) {
                        nextTime = now
                        queue.add(this)
                    }
                    idleCount = idleCount + amount - waitFor // claim our share
                    log("claiming ${name}[$amount/$idleCount]")
                    hold(0)
                } else { // not enough released for first to claim,
                    idleCount += amount
                    log("increasing ${name}[$amount/$idleCount]")
                }
            } else { // noone was waiting, so release them
                idleCount += amount
                log("relasing ${name}[$amount/$idleCount]")
            }
        }

        suspend fun use(amount: Int, block: suspend () -> Unit) {
            acquire(amount)
            block()
            release( amount)
        }
    }

    private var queue = PriorityQueue<SimProcess>(10) { a, b -> a.nextTime - b.nextTime }

    private lateinit var current: SimProcess

    private var now: Int = 0


    suspend fun hold(holdTime: Int) {
        with(current) {
            nextTime = now + holdTime
            queue.add(current)
        }
        current.pause()
    }


    private fun log(msg: String) {
        println("${current.name} @ $now: $msg")
    }

    fun runSimulation() {
        while (queue.isNotEmpty()) {
            current = queue.poll()
            now = current.nextTime
            if (!current.isDone) {
                log("resuming ${current.name}")
                current.resume()
            }
        }
    }
}





