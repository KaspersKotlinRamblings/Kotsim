fun main(args: Array<String>) {

    val foo = { a: Int, b: Int -> (b - a) as Int }
    println("7, 10 " + foo(7,10))
}

