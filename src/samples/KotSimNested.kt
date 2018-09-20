package samples

import kotsim.simulation

fun main(args: Array<String>) = simulation {
    buildSimProcess("hello") {
        hold(10)
        // Just checking that we can spawn new processes in existing sim processes
        buildSimProcess("inner") {
            hold(12)
        }
        hold(7)
    }
    buildSimProcess("hi") {
        for (i in 1..12) {
            hold(i)
        }
    }
}