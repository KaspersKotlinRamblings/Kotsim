fun main(args: Array<String>) {
    object: Simulation() {
        val jetty = Resource(2,"Jetty") // Original demos: 2
        val tug = Resource(3, "Tug") // Original demos: 3

        fun Boat(start: Int, name: String ="" ) = simProcess(name) {
            hold(start)
            acquire(jetty,1)
                acquire(tug,2)
                    hold(2)
                release(tug,2)
                hold(14)
                acquire(tug,1)
                    hold(1)
                release(tug,1)
            release(jetty,1)
        }
        val boat1=Boat(0,"Boat1")
        val boat2=Boat(1, "Boat2")
        val boat3=Boat(7, "Boat3")
    }.runSimulation()
}