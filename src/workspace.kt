import kotlin.*
import kotlin.coroutines.*

fun main2(args: Array<String>) {
    val a = Ahh()
    var hhh = 9
    with(a) {
        19.foo()
    }
    println(a.x)
}

class Ahh {
    var x = 9
    fun Int.foo() {
        this@Ahh.x += this
    }
}

fun <T> on(obj: T, block: () -> Unit) {

}

fun aaa() = buildSequence {
    val aaa = AAA()
    yield(2)
    yieldAll(aaa.bbb())
    yield(-2)

}

class AAA {
    fun bbb() = buildSequence {
        for (i in 10..15) yield(i)
    }
}


fun main(args: Array<String>) = bla {
    //println(aaa().toList())
    val f = Fibbe { println("Making fibbes $aaa") }
    println("Done ${f.aaa}")
}

class Fibbe (val body: Fibbe.()->Unit){
    val aaa = "Inside job"
    init{
        println("Intializing")
        with(this){body()}
    }
}

fun bla (block : ()->Unit){
    println("Before")
    block()
    println("After")
}
