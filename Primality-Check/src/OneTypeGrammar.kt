import java.io.File
import java.io.PrintWriter
import java.util.regex.Pattern

class OneTypeGrammar(
        private val sigma: Set<String>,
        private val genProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        private val transProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        private val delProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        private val machine: LinearBoundedAutomaton
) {
    private var productionNumber: Int = 0

    init {
        for (entry in genProductions.entries) productionNumber += entry.value.size
        for (entry in transProductions.entries) productionNumber += entry.value.size
        for (entry in delProductions.entries) productionNumber += entry.value.size
    }

    fun printMainInfo() {
        println("* * * One-type grammar * * *")
        println("Sigma size: ${sigma.size}")
        println("Total productions: $productionNumber")
        println()
    }

    fun saveToFile(fileName: String) {
        val writer = PrintWriter(File(fileName))
        for (productions in genProductions.entries) {
            val leftPart = productions.key
            for (rightPart in productions.value) {
                writer.println("${leftPart.joinToString("")} -> ${rightPart.joinToString("")}")
            }
        }
        for (productions in transProductions.entries) {
            val leftPart = productions.key
            for (rightPart in productions.value) {
                writer.println("${leftPart.joinToString("")} -> ${rightPart.joinToString("")}")
            }
        }
        for (productions in delProductions.entries) {
            val leftPart = productions.key
            for (rightPart in productions.value) {
                writer.println("${leftPart.joinToString("")} -> ${rightPart.joinToString("")}")
            }
        }
        writer.close()
    }

    fun checkWordForReachability(word: String) {
        val output = mutableListOf<String>()
        val currentChain = mutableListOf<LexicalElement>()

        fun nextStep(leftPart: List<LexicalElement>, rightPart: List<LexicalElement>): Boolean {
            val searchResults = mutableListOf(currentChain.withIndex().filter { it.value == leftPart[0] && it.index + leftPart.size - 1 < currentChain.size }.map { it.index })
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
//                println(currentChain.joinToString("") + "     " + output.last())
                true
            } else false
        }

        // Initial configurations
        val isSingleSymbolWord = word.length == 3
        val a1 = LexicalElement.NonTerminal("A1")
        val a2 = LexicalElement.NonTerminal("A2")
        val oneTerminal = LexicalElement.Terminal("1")
        currentChain.add(a1)

        if (isSingleSymbolWord) {
            nextStep(listOf(a1), genProductions[listOf(a1)]!!.find { it.size == 1 && it[0].data.contains("1") }!!)
        } else {
            nextStep(listOf(a1), genProductions[listOf(a1)]!!.find { it.size == 2 && it[0].data.contains("1") }!!)
            val prod = genProductions[listOf(a2)]!!.find { it.size == 2 && it[0].data.contains("1") }!!
            for (i in 0 until word.length - 4) nextStep(listOf(a2), prod)
            nextStep(listOf(a2), genProductions[listOf(a2)]!!.find { it.size == 1 && it[0].data.contains("1") }!!)
        }

        // Transition configurations
        var currentPosition = 0
        while (true) {
            val currentLexicalElement = currentChain[currentPosition]
            var leftPart = listOf(currentLexicalElement)
            var rightPart = transProductions[leftPart]?.first()
            if (rightPart != null) {
                if (nextStep(leftPart, rightPart)) continue
            }

            if (currentPosition < currentChain.size - 1) {
                leftPart = listOf(currentLexicalElement, currentChain[currentPosition + 1])
                rightPart = transProductions[leftPart]?.first()
                if (rightPart != null) {
                    if (nextStep(leftPart, rightPart)) {
                        currentPosition++
                        continue
                    }
                }
            }

            if (currentPosition > 0) {
                leftPart = listOf(currentChain[currentPosition - 1], currentLexicalElement)
                rightPart = transProductions[leftPart]?.first()
                if (rightPart != null) {
                    if (nextStep(leftPart, rightPart)) {
                        currentPosition--
                        continue
                    }
                }
            }
            break
        }

        if ((currentChain[currentPosition].data.find { it in machine.states }) !in machine.finalStates) {
            println("Word $word (${word.length - 2}) is not reachable in this grammar.")
            println()
            return
        }

        // Recovery configurations
        var leftPart = listOf(currentChain[currentPosition])
        var rightPart = delProductions[leftPart]?.first()
        nextStep(leftPart, rightPart!!)

        var i = currentPosition
        while (i < currentChain.size - 1) {
            leftPart = listOf(currentChain[i], currentChain[i + 1])
            rightPart = delProductions[leftPart]?.first()
            rightPart?.let {
                nextStep(leftPart, it)
            }
            i++
        }

        i = currentPosition - 1
        while (i >= 0) {
            leftPart = listOf(currentChain[i], currentChain[i + 1])
            rightPart = delProductions[leftPart]?.first()
            rightPart?.let {
                nextStep(leftPart, it)
            }
            i--
        }

        println("Word $word (${word.length - 2}) is reachable in this grammar:")
        for (s in output) println(s)
        println("Final chain: ${currentChain.joinToString("")}")
        println()
    }

    companion object {

        fun createFromLBA(machine: LinearBoundedAutomaton): OneTypeGrammar {
            // Production stages
            val genProductions = mutableMapOf<List<LexicalElement>, MutableSet<List<LexicalElement>>>()
            val transProductions = mutableMapOf<List<LexicalElement>, MutableSet<List<LexicalElement>>>()
            val delProductions = mutableMapOf<List<LexicalElement>, MutableSet<List<LexicalElement>>>()

            // Initial configurations
            val a1 = LexicalElement.NonTerminal("A1")
            val a2 = LexicalElement.NonTerminal("A2")
            for (a in machine.sigma - machine.leftMarker - machine.rightMarker) {
                // |w| = 1
                // A1 -> [q0, @, a, a, $]
                addProductionIntoGrammar(
                        genProductions,
                        listOf(a1),
                        listOf(LexicalElement.NonTerminal(listOf(machine.startState, machine.leftMarker, a, a, machine.rightMarker)))
                )
                // |w| > 1
                // A1 -> [q0, @, a, a]A2
                addProductionIntoGrammar(
                        genProductions,
                        listOf(a1),
                        listOf(LexicalElement.NonTerminal(listOf(machine.startState, machine.leftMarker, a, a)), a2)
                )
                // A2 -> [a, a]A2
                addProductionIntoGrammar(
                        genProductions,
                        listOf(a2),
                        listOf(LexicalElement.NonTerminal(listOf(a, a)), a2)
                )
                // A2 -> [a, a, $]
                addProductionIntoGrammar(
                        genProductions,
                        listOf(a2),
                        listOf(LexicalElement.NonTerminal(listOf(a, a, machine.rightMarker)))
                )
            }

            // Transition configurations
            for (transition in machine.delta.entries) {
                val q = transition.key.first
                val x = transition.key.second
                val p = transition.value.first
                val y = transition.value.second

                for (a in machine.sigma - machine.leftMarker - machine.rightMarker) {
                    when (transition.value.third) {
                        "right" -> {
                            if (x == machine.leftMarker && y == machine.leftMarker) {
                                for (z in machine.gamma - machine.leftMarker - machine.rightMarker) {
                                    // |w| = 1
                                    // [q, @, Z, a, $] -> [@, p, Z, a, $], если (p, @, R) ∈ δ(q, @)
                                    addProductionIntoGrammar(
                                            transProductions,
                                            listOf(LexicalElement.NonTerminal(listOf(q, machine.leftMarker, z, a, machine.rightMarker))),
                                            listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, p, z, a, machine.rightMarker)))
                                    )
                                    // |w| > 1
                                    // [q, @, Z, a] -> [@, p, Z, a], если (p, @, R) ∈ δ(q, @)
                                    addProductionIntoGrammar(
                                            transProductions,
                                            listOf(LexicalElement.NonTerminal(listOf(q, machine.leftMarker, z, a))),
                                            listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, p, z, a)))
                                    )
                                }

                            } else {
                                // |w| = 1
                                // [@, q, X, a, $] -> [@, Y, a, p, $], если (p, Y, R) ∈ δ(q, X)
                                addProductionIntoGrammar(
                                        transProductions,
                                        listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a, machine.rightMarker))),
                                        listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, y, a, p, machine.rightMarker)))
                                )
                                // |w| > 1
                                // [q, X, a, $] -> [Y, a, p, $], если (p, Y, R) ∈ δ(q, X)
                                addProductionIntoGrammar(
                                        transProductions,
                                        listOf(LexicalElement.NonTerminal(listOf(q, x, a, machine.rightMarker))),
                                        listOf(LexicalElement.NonTerminal(listOf(y, a, p, machine.rightMarker)))
                                )
                                for (z in machine.gamma - machine.leftMarker - machine.rightMarker) {
                                    for (b in machine.sigma - machine.leftMarker - machine.rightMarker) {
                                        // [@, q, X, a][Z, b] -> [@, Y, a][p, Z, b], если (p, Y, R) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a)), LexicalElement.NonTerminal(listOf(z, b))),
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, y, a)), LexicalElement.NonTerminal(listOf(p, z, b)))
                                        )
                                        // [@, q, X, a][Z, b, $] -> [@, Y, a][p, Z, b, $], если (p, Y, R) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a)), LexicalElement.NonTerminal(listOf(z, b, machine.rightMarker))),
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, y, a)), LexicalElement.NonTerminal(listOf(p, z, b, machine.rightMarker)))
                                        )
                                        // [q, X, a][Z, b] -> [Y, a][p, Z, b], если (p, Y, R) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(q, x, a)), LexicalElement.NonTerminal(listOf(z, b))),
                                                listOf(LexicalElement.NonTerminal(listOf(y, a)), LexicalElement.NonTerminal(listOf(p, z, b)))
                                        )
                                        // [q, X, a][Z, b, $] -> [Y, a][p, Z, b, $], если (p, Y, R) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(q, x, a)), LexicalElement.NonTerminal(listOf(z, b, machine.rightMarker))),
                                                listOf(LexicalElement.NonTerminal(listOf(y, a)), LexicalElement.NonTerminal(listOf(p, z, b, machine.rightMarker)))
                                        )
                                    }
                                }
                            }
                        }
                        "left" -> {
                            if (x == machine.rightMarker && y == machine.rightMarker) {
                                for (z in machine.gamma - machine.leftMarker - machine.rightMarker) {
                                    // |w| = 1
                                    // [@, Z, a, q, $] -> [@, p, Z, a, $], если (p, $, L) ∈ δ(q, $)
                                    addProductionIntoGrammar(
                                            transProductions,
                                            listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, z, a, q, machine.rightMarker))),
                                            listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, p, z, a, machine.rightMarker)))
                                    )
                                    // |w| > 1
                                    // [Z, a, q, $] -> [p, Z, a, $], если (p, $, L) ∈ δ(q, $)
                                    addProductionIntoGrammar(
                                            transProductions,
                                            listOf(LexicalElement.NonTerminal(listOf(z, a, q, machine.rightMarker))),
                                            listOf(LexicalElement.NonTerminal(listOf(p, z, a, machine.rightMarker)))
                                    )
                                }
                            } else {
                                // |w| = 1
                                // [@, q, X, a, $] -> [p, @, Y, a, $], если (p, Y, L) ∈ δ(q, X)
                                addProductionIntoGrammar(
                                        transProductions,
                                        listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a, machine.rightMarker))),
                                        listOf(LexicalElement.NonTerminal(listOf(p, machine.leftMarker, y, a, machine.rightMarker)))
                                )
                                // |w| > 1
                                // [@, q, X, a] -> [p, @, Y, a], если (p, Y, L) ∈ δ(q, X)
                                addProductionIntoGrammar(
                                        transProductions,
                                        listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a))),
                                        listOf(LexicalElement.NonTerminal(listOf(p, machine.leftMarker, y, a)))
                                )
                                for (z in machine.gamma - machine.leftMarker - machine.rightMarker) {
                                    for (b in machine.sigma - machine.leftMarker - machine.rightMarker) {
                                        // [@, Z, b][q, X, a] -> [@, p, Z, b][Y, a], если (p, Y, L) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, z, b)), LexicalElement.NonTerminal(listOf(q, x, a))),
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, p, z, b)), LexicalElement.NonTerminal(listOf(y, a)))
                                        )
                                        // [Z, b][q, X, a] -> [p, Z, b][Y, a], если (p, Y, L) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(z, b)), LexicalElement.NonTerminal(listOf(q, x, a))),
                                                listOf(LexicalElement.NonTerminal(listOf(p, z, b)), LexicalElement.NonTerminal(listOf(y, a)))
                                        )
                                        // [Z, b][q, X, a, $] -> [p, Z, b][Y, a, $], если (p, Y, L) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(z, b)), LexicalElement.NonTerminal(listOf(q, x, a, machine.rightMarker))),
                                                listOf(LexicalElement.NonTerminal(listOf(p, z, b)), LexicalElement.NonTerminal(listOf(y, a, machine.rightMarker)))
                                        )
                                        // [@, Z, b][q, X, a, $] -> [@, p, Z, b][Y, a, $], если (p, Y, L) ∈ δ(q, X)
                                        addProductionIntoGrammar(
                                                transProductions,
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, z, b)), LexicalElement.NonTerminal(listOf(q, x, a, machine.rightMarker))),
                                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, p, z, b)), LexicalElement.NonTerminal(listOf(y, a, machine.rightMarker)))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recovery configurations
            for (a in machine.sigma - machine.leftMarker - machine.rightMarker) {
                for (x in machine.gamma - machine.leftMarker - machine.rightMarker) {
                    val aTerminal = LexicalElement.Terminal(a)
                    for (q in machine.finalStates) {
                        // |w| = 1
                        // [q, @, X, a, $] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(q, machine.leftMarker, x, a, machine.rightMarker))),
                                listOf(aTerminal)
                        )
                        // [@, q, X, a, $] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a, machine.rightMarker))),
                                listOf(aTerminal)
                        )
                        // [@, X, a, q, $] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, x, a, q, machine.rightMarker))),
                                listOf(aTerminal)
                        )
                        // |w| > 1
                        // [q, @, X, a] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(q, machine.leftMarker, x, a))),
                                listOf(aTerminal)
                        )
                        // [@, q, X, a] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, q, x, a))),
                                listOf(aTerminal)
                        )
                        // [q, X, a] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(q, x, a))),
                                listOf(aTerminal)
                        )
                        // [q, X, a, $] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(q, x, a, machine.rightMarker))),
                                listOf(aTerminal)
                        )
                        // [X, a, q, $] -> a, q из F
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(x, a, q, machine.rightMarker))),
                                listOf(aTerminal)
                        )
                    }

                    // |w| > 1
                    for (b in machine.sigma - machine.leftMarker - machine.rightMarker) {
                        val bTerminal = LexicalElement.Terminal(b)
                        // a[X, b] -> ab
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(aTerminal, LexicalElement.NonTerminal(listOf(x, b))),
                                listOf(aTerminal, bTerminal)
                        )
                        // a[X, b, $] -> ab
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(aTerminal, LexicalElement.NonTerminal(listOf(x, b, machine.rightMarker))),
                                listOf(aTerminal, bTerminal)
                        )
                        // [X, a]b -> ab
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(x, a)), bTerminal),
                                listOf(aTerminal, bTerminal)
                        )
                        // [@, X, a]b -> ab
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(machine.leftMarker, x, a)), bTerminal),
                                listOf(aTerminal, bTerminal)
                        )
                    }
                }
            }
            return OneTypeGrammar(machine.sigma, genProductions, transProductions, delProductions, machine)
        }

        private fun addProductionIntoGrammar(grammar: MutableMap<List<LexicalElement>, MutableSet<List<LexicalElement>>>, leftPart: List<LexicalElement>, rightPart: List<LexicalElement>) {
            val productions = grammar[leftPart]
            if (productions == null) {
                grammar[leftPart] = mutableSetOf(rightPart)
            } else {
                productions.add(rightPart)
            }
        }
    }
}