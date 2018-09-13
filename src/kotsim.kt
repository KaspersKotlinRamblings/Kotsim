import java.lang.IllegalArgumentException
import java.util.PriorityQueue



open class Simulation {
    // at some point this will be a proper enumeration, a command pattern or something third
    class NextAction(val action: String = "hold", val actionAt: Int)

    class ProcessQueueElement(val simProcess: Iterator<NextAction>, val time: Int)

    var queue = PriorityQueue<ProcessQueueElement>(10,{ a, b -> (a.time - b.time) as Int})

    var current: ProcessQueueElement? = null

    var now: Int = 0

    fun SimProcess(block: suspend SequenceBuilder<NextAction>.() -> Unit): Iterator<NextAction> {
        val myIterator = buildSequence<NextAction>(block).iterator()
        queue.add(ProcessQueueElement(myIterator,now) )
        return myIterator
    }

    suspend fun SequenceBuilder<NextAction>.hold(holdTime: Int) {
        yield( NextAction("hold", now + holdTime))
    }

    fun runSimulation() {
        while (queue.isNotEmpty()) {
            val next =queue.poll()
            current = next
            now = next.time
            if ( next.simProcess.hasNext() ) {
                val action = next.simProcess.next()
                when (action.action) {
                    "hold" -> queue.add(ProcessQueueElement(next.simProcess, action.actionAt))
                    "die" -> {} // nothing
                    else -> throw IllegalArgumentException("Unknown action: ${action.action}")
                }
            }
        }
    }
}




