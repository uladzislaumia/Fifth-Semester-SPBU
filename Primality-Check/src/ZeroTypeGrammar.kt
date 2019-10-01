class ZeroTypeGrammar(
        _sigma: Set<String>,
        _genProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        _transProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        _delProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        private val machine: TuringMachine
) : Grammar(_sigma, _genProductions, _transProductions, _delProductions) {

    override fun printMainInfo() {
        println("* * * Zero-type grammar * * *")
        super.printMainInfo()
    }

    fun checkNumberForDerivability(n: Int) {
        val word = "1".repeat(n)

        // Initial configurations
        val output = mutableListOf<String>()
        val currentChain = mutableListOf<LexicalElement>()
        val a0 = LexicalElement.NonTerminal("A0")
        val a1 = LexicalElement.NonTerminal("A1")
        val a2 = LexicalElement.NonTerminal("A2")
        val a3 = LexicalElement.NonTerminal("A3")
        currentChain.add(a0)
        nextStep(currentChain, output, listOf(a0), genProductions[listOf(a0)]!!.find { it.size == 2 }!!)
        nextStep(currentChain, output, listOf(a0), genProductions[listOf(a0)]!!.find { it.size == 1 }!!)
        nextStep(currentChain, output, listOf(a1), genProductions[listOf(a1)]!!.first())
        for (i in 0 until word.length) nextStep(currentChain, output, listOf(a2), genProductions[listOf(a2)]!!.find { it.size == 2 && it[0].data.contains("1") }!!)
        nextStep(currentChain, output, listOf(a2), genProductions[listOf(a2)]!!.find { it.size == 1 }!!)
        for (i in 0 until word.length + 2) nextStep(currentChain, output, listOf(a3), genProductions[listOf(a3)]!!.find { it.size == 2 }!!)
        nextStep(currentChain, output, listOf(a3), genProductions[listOf(a3)]!!.find { it.size == 1 }!!)

        // Transition configurations
        var currentPosition = 1
        while (true) {
            val currentLexicalElement = currentChain[currentPosition]
            if (currentPosition < currentChain.size - 1) {
                var leftPart = listOf(currentLexicalElement, currentChain[currentPosition + 1])
                var rightPart = transProductions[leftPart]?.first()
                if (rightPart != null) {
                    if (nextStep(currentChain, output, leftPart, rightPart)) {
                        currentPosition++
                        continue
                    }
                }
                if (currentPosition > 0) {
                    leftPart = listOf(currentChain[currentPosition - 1], currentLexicalElement, currentChain[currentPosition + 1])
                    rightPart = transProductions[leftPart]?.first()
                    if (rightPart != null) {
                        if (nextStep(currentChain, output, leftPart, rightPart)) {
                            currentPosition--
                            continue
                        }
                    }
                }
            }
            break
        }

        val terminationStateTerminal = currentChain[currentPosition]
        if ((terminationStateTerminal.data.find { it in machine.states }) !in machine.finalStates) {
            println("Word $word (${word.length}) is not reachable in this grammar.")
            println()
            return
        }

        // Recovery configurations
        var i = currentPosition
        while (i < currentChain.size - 1) {
            val leftPart = listOf(terminationStateTerminal, currentChain[i + 1])
            val rightPart = delProductions[leftPart]?.first()
            rightPart?.let {
                nextStep(currentChain, output, leftPart, it)
            }
            i++
        }

        i = currentPosition - 1
        while (i >= 0) {
            val leftPart = listOf(currentChain[i], terminationStateTerminal)
            val rightPart = delProductions[leftPart]?.first()
            rightPart?.let {
                nextStep(currentChain, output, leftPart, it)
            }
            i--
        }

        while (nextStep(currentChain, output, listOf(terminationStateTerminal), delProductions[listOf(terminationStateTerminal)]!!.first())) { }

        println("Word $word (${word.length}) is reachable in this grammar:")
        for (s in output) println(s)
        println("Final chain: ${currentChain.joinToString("")}")
        println()
    }

    companion object {

        fun getInstance(machine: TuringMachine): ZeroTypeGrammar {
            // Production stages
            val genProductions = mutableMapOf<List<LexicalElement>, MutableSet<List<LexicalElement>>>()
            val transProductions = mutableMapOf<List<LexicalElement>, MutableSet<List<LexicalElement>>>()
            val delProductions = mutableMapOf<List<LexicalElement>, MutableSet<List<LexicalElement>>>()
            val epsilon = ""

            // Initial configurations
            val a0 = LexicalElement.NonTerminal("A0")
            val a1 = LexicalElement.NonTerminal("A1")
            val a2 = LexicalElement.NonTerminal("A2")
            val a3 = LexicalElement.NonTerminal("A3")

            // A0 -> (epsilon,blank)A0
            addProductionIntoGrammar(
                    genProductions,
                    listOf(a0),
                    listOf(LexicalElement.NonTerminal(listOf(epsilon, machine.blank)), a0)
            )
            // A0 -> A1
            addProductionIntoGrammar(
                    genProductions,
                    listOf(a0),
                    listOf(a1)
            )
            // A1 -> q0A2
            addProductionIntoGrammar(
                    genProductions,
                    listOf(a1),
                    listOf(LexicalElement.NonTerminal(machine.startState), a2)
            )
            // A2 -> (a,a)A2   a: from sigma
            for (a in machine.sigma)
                addProductionIntoGrammar(
                        genProductions,
                        listOf(a2),
                        listOf(LexicalElement.NonTerminal(listOf(a, a)), a2)
                )
            // A2 -> A3
            addProductionIntoGrammar(
                    genProductions,
                    listOf(a2),
                    listOf(a3)
            )
            // A3 -> (epsilon,blank)A3
            addProductionIntoGrammar(
                    genProductions,
                    listOf(a3),
                    listOf(LexicalElement.NonTerminal(listOf(epsilon, machine.blank)), a3)
            )
            // A3 -> epsilon
            addProductionIntoGrammar(
                    genProductions,
                    listOf(a3),
                    listOf(LexicalElement.Terminal(epsilon))
            )

            // Transition configurations
            for (transition in machine.delta.entries) {
                val q = transition.key.first
                val x = transition.key.second
                val p = transition.value.first
                val y = transition.value.second
                val direction = transition.value.third

                for (a in machine.sigma + epsilon) {
                    when (direction) {
                        "right" -> {
                            // q(a,X) -> (a,Y)p, если delta(q,X) = (p,Y,R), при этом a: from sigma or epsilon;
                            addProductionIntoGrammar(
                                    transProductions,
                                    listOf(LexicalElement.NonTerminal(q), LexicalElement.NonTerminal(listOf(a, x))),
                                    listOf(LexicalElement.NonTerminal(listOf(a, y)), LexicalElement.NonTerminal(p))
                            )
                        }
                        "left" -> {
                            // (b,C)q(a,X) -> p(b,C)(a,Y), если delta(q,X) = (p,Y,L), при этом a,b: from sigma or epsilon и C: from gamma
                            for (b in machine.sigma + epsilon) {
                                for (c in machine.gamma) {
                                    addProductionIntoGrammar(
                                            transProductions,
                                            listOf(LexicalElement.NonTerminal(listOf(b, c)), LexicalElement.NonTerminal(q), LexicalElement.NonTerminal(listOf(a, x))),
                                            listOf(LexicalElement.NonTerminal(p), LexicalElement.NonTerminal(listOf(b, c)), LexicalElement.NonTerminal(listOf(a, y)))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recovery configurations
            for (q in machine.finalStates) {
                val qState = LexicalElement.NonTerminal(q)
                // q -> epsilon, q из F
                addProductionIntoGrammar(
                        delProductions,
                        listOf(qState),
                        listOf(LexicalElement.Terminal(epsilon))
                )
                for (a in machine.sigma + epsilon) {
                    for (c in machine.gamma) {
                        // (a,C)q -> qaq, q из F, C из sigma, a из sigma или epsilon
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(LexicalElement.NonTerminal(listOf(a, c)), qState),
                                listOf(qState, LexicalElement.Terminal(a), qState)
                        )
                        // q(a,C) -> qaq, q из F, C из sigma, a из sigma или epsilon
                        addProductionIntoGrammar(
                                delProductions,
                                listOf(qState, LexicalElement.NonTerminal(listOf(a, c))),
                                listOf(qState, LexicalElement.Terminal(a), qState)
                        )
                    }
                }
            }
            return ZeroTypeGrammar(machine.sigma, genProductions, transProductions, delProductions, machine)
        }
    }
}