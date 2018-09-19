import java.util.*

fun Simu( block : Simulation.()->Unit) {
    val simulation = Simulation()
    block(simulation)
    simulation.runSimulation()

}

class DummyYield

open class Simulation {
    // at some point this will be a proper enumeration, a command pattern or something third

    class SimProcess(val iterator: Iterator<DummyYield>, var nextTime: Int, val name: String = "")

    fun simProcess(name: String = "", block: suspend SequenceBuilder<DummyYield>.() -> Unit): SimProcess {
        val myIterator = buildSequence<DummyYield>(
                {
                    log("starting $name")
                    block()
                    log("terminating $name")
                }
        ).iterator()
        val process = SimProcess(myIterator, now, name)
        queue.add(process)
        return process
    }

    inner class Resource(val capacity: Int, val name: String = "res") {
        inner class ResourceRequest(val process: SimProcess, val amount: Int)

        var idleCount = capacity
        val waiting: Queue<ResourceRequest> = ArrayDeque<ResourceRequest>()
    }

    private var queue = PriorityQueue<SimProcess>(10) { a, b -> a.nextTime - b.nextTime }

    private lateinit var current: SimProcess

    private var now: Int = 0


    suspend fun SequenceBuilder<DummyYield>.hold(holdTime: Int) {
        with(current) {
            nextTime = now + holdTime
            queue.add(current)
        }
        yield(DummyYield())
    }

    suspend fun SequenceBuilder<DummyYield>.acquire(res: Resource, amount: Int = 1) {
        with(res) {
            if (idleCount >= amount) { // We have what is asked for
                log("taking resource ${res.name}[$amount/$idleCount]")
                idleCount -= amount
            } else { // not enough idle resources
                log("awaiting resource ${res.name}[$amount/$idleCount]")
                waiting.add(ResourceRequest(current, amount))
                yield(DummyYield())
            }
        }
    }

    suspend fun SequenceBuilder<DummyYield>.release(res: Resource, amount: Int = 1) {
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

    suspend fun SequenceBuilder<DummyYield>.use(res: Resource, amount: Int = 1, block: suspend SequenceBuilder<DummyYield>.() -> Unit) {
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
            if (current.iterator.hasNext()) {
                log("resuming ${current.name}")
                current.iterator.next()
            }
        }
    }
}




