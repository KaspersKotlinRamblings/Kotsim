fun main(args: Array<String>) {

    val mySim =  object: Simulation() {
        val helloProcess = buildSimProcess("hello") {
            hold(10)
            val newProcess = buildSimProcess("inner") {
                hold(12)
            }
            hold(7)
        }
        val hiProcesse = buildSimProcess("hi") {
            for (i in 1..12) {
                hold( i )
            }
        }
    }
    mySim.runSimulation()
}