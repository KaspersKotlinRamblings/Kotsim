package samples

import kotsim.simulation

fun main(args: Array<String>) = simulation {

    val jetty = buildResource(2, "Jetty") // Original demos: 2
    val tug = buildResource(3, "Tug") // Original demos: 3

    fun Boat(start: Int, name: String = "") = buildSimProcess(name) {
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
    Boat(0, "Boat1")
    Boat(1, "Boat2")
    Boat(7, "Boat3")

}