package samples

import kotsim.simulation

fun main(args: Array<String>) = simulation {
    simulationProcess("hello") {
        hold(10)
        // Just checking that we can spawn new processes in existing sim processes
        simulationProcess("inner") {
            hold(12)
        }
        hold(7)
    }
    simulationProcess("hi") {
        for (i in 1..12) {
            hold(i)
        }
    }
}