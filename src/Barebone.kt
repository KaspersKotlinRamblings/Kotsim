
// --------------------------

fun syvnitretten2() = buildCoroutine<Int> {
    suspend(7)
    suspend(9)
    suspend(13)
    print("«Last»")
}

fun main(args: Array<String>) {
    val ddd = syvnitretten2()
    /*print("<${ddd.resume()}>")
    print("<${ddd.resume()}>")
    print("<${ddd.resume()}>")


    print("<${ddd.resume()}>")
    if (ddd.isDone) {
        println();println("End of story")
    }*/
    while (!ddd.isDone)
        println("<${ddd.resume()}>")
    println();println("End of story")
}

