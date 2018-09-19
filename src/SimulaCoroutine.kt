import java.lang.IllegalStateException
import kotlin.coroutines.*

typealias Coroutine = Continuation<Unit>

interface SimulaCoroutine<T> {
    suspend fun suspend(value: T)
    val isDone:Boolean get
    fun resume(): T
}

fun <T>buildCoroutine(block: suspend SimulaCoroutine<T>.() -> Unit): SimulaCoroutine<T>  {
    val co = SimCoroutine<T>()
    with(co) {
        print("[SQ]")
        nextStep = block.createCoroutine(receiver = this, completion = this)
        print("[/SQ]")
    }
    return co
}

class SimCoroutine<T> : SimulaCoroutine<T>, Coroutine {
    override val context: CoroutineContext get() = EmptyCoroutineContext
    lateinit var nextStep: Coroutine
    private var done = false;
    private var value: T? = null

    override val isDone get() = done


    override fun resumeWith(result: SuccessOrFailure<Unit>) {
        print("[X]")
        done=true
    }

    override fun resume():T {
        if (done) throw IllegalStateException("Cannot resume finished SimulaCoroutine")
        print("[CN]")
        nextStep.resume(Unit)
        return value!!
    }

    override suspend fun suspend(value: T) {
        print("[Y:$value]")
        this.value = value
        return suspendCoroutine { continuation -> this.nextStep = continuation }
    }
}