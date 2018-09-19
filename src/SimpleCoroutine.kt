import java.lang.IllegalStateException
import kotlin.coroutines.*

typealias Coroutine = Continuation<Unit>

interface SimpleCoroutine<T> {
    suspend fun suspend(value: T)
    val isDone:Boolean get
    fun resume(): T
}

fun <T>buildCoroutine(block: suspend SimpleCoroutine<T>.() -> Unit): SimpleCoroutine<T>  {
    val co = SimCoroutine<T>()
    with(co) {
        print("[SQ]")
        nextStep = block.createCoroutine(receiver = this, completion = this)
        print("[/SQ]")
    }
    return co
}

open class SimCoroutine<T> : SimpleCoroutine<T>, Coroutine {
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
        if (done) throw IllegalStateException("Cannot resume finished SimpleCoroutine")
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