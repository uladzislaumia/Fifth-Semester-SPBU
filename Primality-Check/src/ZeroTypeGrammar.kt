import java.io.File
import java.io.PrintWriter
import java.util.regex.Pattern

class ZeroTypeGrammar(
        private val sigma: Set<String>,
        private val genProductions: Map<String, List<List<String>>>,
        private val transProductions: Map<String, List<List<String>>>,
        private val delProductions: Map<String, List<List<String>>>,
        private val machine: TuringMachine
) {
    private var productionNumber: Int = 0

    init {
        for (entry in genProductions.entries) productionNumber += entry.value.size
        for (entry in transProductions.entries) productionNumber += entry.value.size
        for (entry in delProductions.entries) productionNumber += entry.value.size
    }

    fun printMainInfo() {
        println("* * * Zero-type grammar * * *")
        println("Sigma size: ${sigma.size}")
        println("Total productions: $productionNumber")
        println()
    }

    fun saveToFile(fileName: String) {
        val writer = PrintWriter(File(fileName))
        for (productions in genProductions.entries) {
            val leftPart = productions.key
            for (rightPart in productions.value) {
                writer.println("$leftPart -> ${rightPart.joinToString("")}")
            }
        }
        for (productions in transProductions.entries) {
            val leftPart = productions.key
            for (rightPart in productions.value) {
                writer.println("$leftPart -> ${rightPart.joinToString("")}")
            }
        }
        for (productions in delProductions.entries) {
            val leftPart = productions.key
            for (rightPart in productions.value) {
                writer.println("$leftPart -> ${rightPart.joinToString("")}")
            }
        }
        writer.close()
    }

    fun checkWordReachability(word: String) {
        val output = mutableListOf<String>()
        val currentChain = mutableListOf("A0")

        fun nextStep(leftPart: List<String>, rightPart: List<String>): Boolean {
            val searchResults = mutableListOf<List<Int>>()
            val primarySearchResult = currentChain.withIndex().filter { it.value == leftPart[0] && it.index + leftPart.size - 1 < currentChain.size }.map { it.index }
            searchResults.add(primarySearchResult)
            for (i in 1 until leftPart.size) {
                val currentSearchResult = searchResults[i - 1].filter { startIndex -> currentChain[startIndex + i] == leftPart[i] }
                if (currentSearchResult.isEmpty()) break
                searchResults.add(currentSearchResult)
            }

            return if (searchResults.size == leftPart.size && searchResults.last().isNotEmpty()) {
                val startIndex = searchResults.last()[0]
                for (i in 0 until leftPart.size) {
                    currentChain.removeAt(startIndex)
                }
                currentChain.addAll(startIndex, rightPart)
                output.add("${leftPart.joinToString("")} -> ${rightPart.joinToString("")}")
//                println(currentChain.joinToString(" ") + "     " + output.last())
                true
            } else {
                false
            }
        }

        // Generation stage
        nextStep(listOf("A0"), genProductions["A0"]!![0])
        nextStep(listOf("A0"), genProductions["A0"]!![1])
        nextStep(listOf("A1"), genProductions["A1"]!![0])
        for (i in 0 until word.length) nextStep(listOf("A2"), genProductions["A2"]!![0])
        nextStep(listOf("A2"), genProductions["A2"]!![1])
        for (i in 0 until word.length + 2) nextStep(listOf("A3"), genProductions["A3"]!![0])
        nextStep(listOf("A3"), genProductions["A3"]!![1])

        // MT working stage
        var currentStatePosition = 1
        while (currentChain[currentStatePosition] !in machine.finalStates) {
            val currentNonTerminal = currentChain[currentStatePosition]
            val nextNonTerminal = currentChain[currentStatePosition + 1]
            transProductions[currentNonTerminal + nextNonTerminal]?.let {
                nextStep(listOf(currentNonTerminal, nextNonTerminal), it[0])
                currentStatePosition++
            } ?: transProductions[currentChain[currentStatePosition - 1] + currentNonTerminal + nextNonTerminal]?.let {
                nextStep(listOf(currentChain[currentStatePosition - 1], currentNonTerminal, nextNonTerminal), it[0])
                currentStatePosition--
            } ?: break
        }
        val terminationState = currentChain[currentStatePosition]
        if (terminationState !in machine.finalStates) {
            println("Word $word is not reachable in this grammar.")
            println()
            return
        }

        // Deletion stage
        var currentPosition = currentStatePosition + 1
        val pattern = Pattern.compile("[(](.*),.*[)]")

        while (true) {
            val currentNonTerminal = currentChain[currentPosition]
            val matcher = pattern.matcher(currentNonTerminal)
            if (matcher.find()) {
                val value = matcher.group(1)
                nextStep(listOf(terminationState, currentNonTerminal), listOf(terminationState, value, terminationState))
                currentPosition++
            }
            currentPosition++
            if (currentPosition >= currentChain.size) break
        }

        currentPosition = currentStatePosition - 1
        while (true) {
            val currentNonTerminal = currentChain[currentPosition]
            val matcher = pattern.matcher(currentNonTerminal)
            if (matcher.find()) {
                val value = matcher.group(1)
                nextStep(listOf(currentNonTerminal, terminationState), listOf(terminationState, value, terminationState))
            }
            currentPosition--
            if (currentPosition < 0) break
        }

        while (nextStep(listOf(terminationState), listOf())) { }
        println("Word $word is reachable in this grammar:")
        for (s in output) println(s)
        println()
    }

    companion object {

        fun createFromMT(machine: TuringMachine): ZeroTypeGrammar {
            // Production stages
            val genProductions = mutableMapOf<String, MutableList<List<String>>>()
            val transProductions = mutableMapOf<String, MutableList<List<String>>>()
            val delProductions = mutableMapOf<String, MutableList<List<String>>>()
            val epsilon = ""

            // A0 -> (epsilon,blank)A0   &&   A0 -> A1
            addProductionIntoGrammar(genProductions, "A0", listOf("($epsilon,_)", "A0"))
            addProductionIntoGrammar(genProductions, "A0", listOf("A1"))

            // A1 -> q0A2
            addProductionIntoGrammar(genProductions, "A1", listOf(machine.startState, "A2"))

            // A2 -> (a,a)A2   a: from sigma   &&   A2 -> A3
            for (a in machine.sigma) addProductionIntoGrammar(genProductions, "A2", listOf("($a,$a)", "A2"))
            addProductionIntoGrammar(genProductions, "A2", listOf("A3"))

            // A3 -> (epsilon,blank)A3   &&   A3 -> epsilon
            addProductionIntoGrammar(genProductions, "A3", listOf("($epsilon,_)", "A3"))
            addProductionIntoGrammar(genProductions, "A3", listOf(epsilon))

            // q(a,A) -> (a,M)p   q,p: states; A,M: from gamma; a: from sigma or epsilon; where: delta(q,A) = (p,M,R)
            val transitionsToRight = machine.delta.entries.filter { entry -> entry.value.third == "right" }
            for (trans in transitionsToRight) {
                val q = trans.key.first
                val A = trans.key.second
                val p = trans.value.first
                val M = trans.value.second
                for (a in machine.sigma + epsilon) {
                    addProductionIntoGrammar(transProductions, "$q($a,$A)", listOf("($a,$M)", p))
                }
            }

            // (b,C)q(a,A) -> p(b,C)(a,M)   q,p: states; A,M,C: from gamma; a,b: from sigma or epsilon; where: delta(q,A) = (p,M,L)
            val transitionsToLeft = machine.delta.entries.filter { entry -> entry.value.third == "left" }
            for (trans in transitionsToLeft) {
                val q = trans.key.first
                val A = trans.key.second
                val p = trans.value.first
                val M = trans.value.second
                for (a in machine.sigma + epsilon) {
                    for (b in machine.sigma + epsilon) {
                        for (C in machine.gamma) {
                            addProductionIntoGrammar(transProductions, "($b,$C)$q($a,$A)", listOf(p, "($b,$C)", "($a,$M)"))
                        }
                    }
                }
            }

            // (a,C)q -> qaq   &&   q(a,C) -> qaq   &&   q -> epsilon   a: from sigma or epsilon; C: from gamma; q: final state
            for (finalState in machine.finalStates) {
                addProductionIntoGrammar(delProductions, finalState, listOf(epsilon))
                for (a in machine.sigma + epsilon) {
                    for (C in machine.gamma) {
                        addProductionIntoGrammar(delProductions, "($a,$C)$finalState", listOf(finalState, a, finalState))
                        addProductionIntoGrammar(delProductions, "$finalState($a,$C)", listOf(finalState, a, finalState))
                    }
                }
            }
            return ZeroTypeGrammar(machine.sigma, genProductions, transProductions, delProductions, machine)
        }

        private fun addProductionIntoGrammar(grammar: MutableMap<String, MutableList<List<String>>>, leftPart: String, rightPart: List<String>) {
            val productions = grammar[leftPart]
            if (productions == null) {
                grammar[leftPart] = mutableListOf(rightPart)
            } else {
                productions.add(rightPart)
            }
        }
    }
}