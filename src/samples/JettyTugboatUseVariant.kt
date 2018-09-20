package samples

import kotsim.simulation

fun main(args: Array<String>) = simulation {

    val jetty = resource(2, "Jetty") // Original demos: 2
    val tug = resource(3, "Tug") // Original demos: 3

    fun boat(start: Int, name: String = "") = simulationProcess(name) {
        hold(start)
        jetty.use( 1) {
            tug.use(2) {
                hold(2)
            }
            hold(14)
            tug.use( 1) {
                hold(1)
            }
        }
    }
    boat(0, "Boat1")
    boat(1, "Boat2")
    boat(7, "Boat3")

}