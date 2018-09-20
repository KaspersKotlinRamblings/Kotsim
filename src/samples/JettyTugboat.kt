package samples

import kotsim.*

fun main(args: Array<String>) = simulation {
    val jetty = buildResource(2, "Jetty") // Original demos: 2
    val tug = buildResource(3, "Tug") // Original demos: 3

    // Make a function which builds new sim processes
    fun boat(start: Int, name: String = "") = buildSimProcess(name) {
        hold(start)
        jetty.acquire( 1)
            tug.acquire( 2)
                hold(2)
            tug.release( 2)
            hold(14)
            tug.acquire( 1)
                hold(1)
            tug.release( 1)
        jetty.release(1)
    }
    // start three boats
    boat(0, "Boat1")
    boat(1, "Boat2")
    boat(7, "Boat3")
}
