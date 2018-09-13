fun main(args: Array<String>) {

    val mySim =  object: Simulation() {
        val helloProcess = SimProcess {
            println("Hello 1 @ $now")
            hold(10)
            val newProcess = SimProcess {
                println("NewOne @ $now")
                hold(12)
                println("NewOne @ $now")
            }
            println("Hello 2 @ $now")
            hold(7)
        }
        val hiProcesse = SimProcess {
            for (i in 1..12) {
                println("Hi $i @ $now")
                hold( i )
            }
        }
    }
    mySim.runSimulation()
}