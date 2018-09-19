import kotsim.Simulation

fun main(args: Array<String>) = Simulation {

    val jetty = Resource(2, "Jetty") // Original demos: 2
    val tug = Resource(3, "Tug") // Original demos: 3

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

    val boat1 = Boat(0, "Boat1")
    val boat2 = Boat(1, "Boat2")
    val boat3 = Boat(7, "Boat3")

}