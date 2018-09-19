fun main(args: Array<String>) = Simu {

    val jetty = Resource(2, "Jetty") // Original demos: 2
    val tug = Resource(3, "Tug") // Original demos: 3

    fun Boat(start: Int, name: String = "") = simProcess(name) {
        hold(start)
        use(jetty, 1) {
            use(tug, 2) {
                hold(2)
            }
            hold(14)
            use(tug, 1) {
                hold(1)
            }
        }
    }

    val boat1 = Boat(0, "Boat1")
    val boat2 = Boat(1, "Boat2")
    val boat3 = Boat(7, "Boat3")

}