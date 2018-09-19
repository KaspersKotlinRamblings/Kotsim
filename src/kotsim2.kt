import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.*

typealias SimStep = Continuation<Unit>
typealias Time = Int



fun Simu( block : Simulation.()->Unit) {
    val simulation = Simulation()
    block(simulation)
    simulation.runSimulation()

}

class DummyYield

open class Simulation {
    // at some point this will be a proper enumeration, a command pattern or something third

    interface SimulationProcess {
        suspend fun pause()
    }

    fun buildSimProcess(name: String = "SimProcess", block: suspend SimulationProcess.() -> Unit): SimProcess  {
        fun surround ( prim: suspend SimulationProcess.()->Unit, inBlock : suspend SimulationProcess.()->Unit): suspend  SimProcess.()-> Unit {
            return inBlock
        }
        
        val co = SimProcess(name)
        val blk = surround(block) {
            log("starting $name")
            block(co)
            println("terminating $name")
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
            done=true
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
    }

    private var queue = PriorityQueue<SimProcess>(10) { a, b -> a.nextTime - b.nextTime }

    private lateinit var current: SimProcess

    private var now: Int = 0


    suspend fun SimulationProcess.hold(holdTime: Int) {
        with(current) {
            nextTime = now + holdTime
            queue.add(current)
        }
        this.pause()
    }

    suspend fun SimulationProcess.acquire(res: Resource, amount: Int = 1) {
        with(res) {
            if (idleCount >= amount) { // We have what is asked for
                log("taking resource ${res.name}[$amount/$idleCount]")
                idleCount -= amount
            } else { // not enough idle resources
                log("awaiting resource ${res.name}[$amount/$idleCount]")
                waiting.add(ResourceRequest(current, amount))
                pause()
            }
        }
    }

    suspend fun SimulationProcess.release(res: Resource, amount: Int = 1) {
        with(res) {
            if (waiting.size > 0) { // is anyone waiting to grab the released resources
                val waitFor = waiting.peek().amount // what is the first in the queue waiting for
                if (waitFor <= amount + idleCount) { // we release more than what the first is waiting for
                    with(waiting.poll().process) {
                        nextTime = now
                        queue.add(this)
                    }
                    idleCount = idleCount + amount - waitFor // claim our share
                    log("claiming ${res.name}[$amount/$idleCount]")
                    hold(0)
                } else { // not enough released for first to claim,
                    idleCount += amount
                    log("increasing ${res.name}[$amount/$idleCount]")
                }
            } else { // noone was waiting, so release them
                idleCount += amount
                log("relasing ${res.name}[$amount/$idleCount]")
            }
        }
    }

    suspend fun SimulationProcess.use(res: Resource, amount: Int = 1, block: suspend SimulationProcess.() -> Unit) {
        acquire(res, amount)
        block()
        release(res, amount)
    }

    private fun log(msg: String) {
        println("${current.name} @ $now: $msg")
    }

    fun runSimulation() {
        while (queue.isNotEmpty()) {
            current = queue.poll()
            now = current.nextTime
            if (! current.isDone) {
                log("resuming ${current.name}")
                current.resume()
            }
        }
    }
}




