fun main() {
    val tm = TuringMachine.createFromFile("tm.txt")
    val lba = LinearBoundedAutomaton.createFromFile("lba.txt")
    val zeroTypeGrammar = ZeroTypeGrammar.getInstance(tm)
    val oneTypeGrammar = OneTypeGrammar.getInstance(lba)

    tm.printMainInfo()
    lba.printMainInfo()
    zeroTypeGrammar.printMainInfo()
    oneTypeGrammar.printMainInfo()

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

