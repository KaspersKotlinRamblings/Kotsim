fun main(args: Array<String>) {

    val mySim =  object: Simulation() {
        val helloProcess = simProcess("hello") {
            hold(10)
            val newProcess = simProcess("inner") {
                hold(12)
            }
            hold(7)
        }
        val hiProcesse = simProcess("hi") {
            for (i in 1..12) {
                hold( i )
            }
        }
    }
    mySim.runSimulation()
}