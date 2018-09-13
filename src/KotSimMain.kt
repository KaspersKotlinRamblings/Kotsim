fun main(args: Array<String>) {

    val mySim =  object: Simulation() {
        val helloProcess = SimProcess("hello") {
            hold(10)
            val newProcess = SimProcess("inner") {
                hold(12)
            }
            hold(7)
        }
        val hiProcesse = SimProcess("hi") {
            for (i in 1..12) {
                hold( i )
            }
        }
    }
    mySim.runSimulation()
}