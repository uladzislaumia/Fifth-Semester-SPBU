fun main() {
    val tm = TuringMachine.getInstance("tm.txt")
    val lba = LinearBoundedAutomaton.getInstance("lba.txt")
    val zeroTypeGrammar = ZeroTypeGrammar.getInstance(tm)
    val oneTypeGrammar = OneTypeGrammar.getInstance(lba)

    tm.printMainInfo()
    lba.printMainInfo()
    zeroTypeGrammar.printMainInfo()
    oneTypeGrammar.printMainInfo()

    while (true) {
        println("Available commands:")
        println("\"ck0 <number>\": check a number for derivability in 0-type grammar generated from \"tm.txt\"")
        println("\"ck1 <number>\": check a number for derivability in 1-type grammar generated from \"lba.txt\"")
        println("\"q\": complete execution")

        val s = readLine()
        if (s != null) {
            if (s == "q") {
                break
            } else if (s.matches("ck0 [1-9]+[0-9]*".toRegex())) {
                val n = s.split(" ")[1].toInt()
                zeroTypeGrammar.checkNumberForDerivability(n)
            } else if (s.matches("ck1 [1-9]+[0-9]*".toRegex())) {
                val n = s.split(" ")[1].toInt()
                oneTypeGrammar.checkNumberForDerivability(n)
            } else {
                println("Unknown command or incorrect argument!\n")
            }
        }
    }

//    for (i in 1..7) {
//        zeroTypeGrammar.checkWordForReachability("1".repeat(i))
//        oneTypeGrammar.checkWordForReachability(lba.leftMarker + "1".repeat(i) + lba.rightMarker)
//    }

//    val interpreter = Interpreter()
//    for (i in 1..100) {
//        interpreter.run("1".repeat(i), tm)
//        interpreter.run(lba.leftMarker + "1".repeat(i) + lba.rightMarker, lba)
//    }

//    zeroTypeGrammar.saveToFile("zero_type_grammar.txt")
//    oneTypeGrammar.saveToFile("one_type_grammar.txt")
}

