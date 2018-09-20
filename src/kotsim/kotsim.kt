package kotsim

typealias Time = Long

fun simulation(block: Simulation.() -> Unit) {
    val simulation = SimulationClass()
    block(simulation)
    simulation.runSimulation()
}

interface Simulation {
    val now: Time
    val current: SimulationProcess

    fun buildSimProcess(name: String = "SimProcess", block: suspend SimulationProcess.() -> Unit): SimulationProcess
    fun buildResource(capacity: Int, name: String = "res") : Resource
    fun log(msg: String)
}

interface SimulationProcess {
    suspend fun pause()
    suspend fun hold(holdTime: Int)
}


interface Resource {
    suspend fun acquire(amount: Int = 1)
    suspend fun release(amount: Int = 1)
    suspend fun use(amount: Int, block: suspend () -> Unit)
}







