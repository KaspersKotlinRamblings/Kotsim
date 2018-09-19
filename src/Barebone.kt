
// --------------------------

fun syvnitretten2() = buildCoroutine<Int> {
    suspend(7)
    suspend(9)
    suspend(13)
    print("«Last»")
}

fun main(args: Array<String>) {
    val ddd = syvnitretten2()
    while (!ddd.isDone)
        println("<${ddd.resume()}>")
    println("End of story")
}

