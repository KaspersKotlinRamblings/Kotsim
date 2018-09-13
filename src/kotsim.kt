import java.lang.IllegalArgumentException
import java.util.*

typealias SimProcessHandle = Iterator<Simulation.NextAction>

open class Simulation {
    // at some point this will be a proper enumeration, a command pattern or something third
    class NextAction(val action: String = "hold", val actionAt: Int)

    class ProcessQueueElement(val simProcess: Iterator<NextAction>, val time: Int, val name: String = "")

    inner class Resource(val capacity: Int) {
        var idleCount = capacity
        val waiting: Queue<SimProcessHandle> = ArrayDeque<SimProcessHandle>()


    }

    private var queue = PriorityQueue<ProcessQueueElement>(10, { a, b -> (a.time - b.time) as Int })

    private var current: ProcessQueueElement? = null

    var now: Int = 0

    fun SimProcess(name: String = "", block: suspend SequenceBuilder<NextAction>.() -> Unit): Iterator<NextAction> {
        val myIterator = buildSequence<NextAction>(
                { log("starting $name");block(); log("terminating $name") }).iterator()
        queue.add(ProcessQueueElement(myIterator, now, name))
        return myIterator
    }

    suspend fun SequenceBuilder<NextAction>.hold(holdTime: Int) {
        yield(NextAction("hold", now + holdTime))
    }

    suspend fun SequenceBuilder<NextAction>.acquire(res: Resource, amount: Int = 1) {
        with(res) {
            if (idleCount > 0) {
                idleCount--
            } else {
                waiting.add(current?.simProcess)
                yield(NextAction("waiting", Int.MAX_VALUE))
            }
        }
    }

    suspend fun SequenceBuilder<NextAction>.release(res: Resource, amount: Int = 1) {
        with(res) {
            if (waiting.size > 0) {
                var p = waiting.poll()
                yield(NextAction("hold", now))
            } else {
                idleCount++
            }
        }
    }

    fun log(msg: String) {
        println("@$now: $msg")
    }

    fun runSimulation() {
        while (queue.isNotEmpty()) {
            val next = queue.poll()
            current = next
            now = next.time
            if (next.simProcess.hasNext()) {
                log("resuming ${next.name}")
                val action = next.simProcess.next()
                when (action.action) {
                    "hold" -> queue.add(ProcessQueueElement(next.simProcess, action.actionAt, next.name))
                    "waiting" -> {
                    } // nothing - current in sleeping elsewhere
                    else -> throw IllegalArgumentException("Unknown action: ${action.action}")
                }
            }
        }
    }
}




