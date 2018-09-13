fun main(args: Array<String>) {

    val mySim =  object: Simulation() {
        val jetty = Resource(2)
        val tug = Resource(3)

        fun Boat(start: Int) = SimProcess {
            hold(start)
            acquire(jetty)
                acquire(tug)
                    hold(2)
                release(tug)
                hold(14)
                acquire(tug)
                    hold(1)
                release(tug)
            release(jetty)
        }
        val boat1=Boat(0)
        val boat2=Boat(1)
        val boat3=Boat(15)
    }
    mySim.runSimulation()
}