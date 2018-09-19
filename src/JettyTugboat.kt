import kotsim.*

fun main(args: Array<String>) = Simulation {
    val jetty = Resource(2, "Jetty") // Original demos: 2
    val tug = Resource(3, "Tug") // Original demos: 3

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

    val boat1 = boat(0, "Boat1")
    val boat2 = boat(1, "Boat2")
    val boat3 = boat(7, "Boat3")
}
