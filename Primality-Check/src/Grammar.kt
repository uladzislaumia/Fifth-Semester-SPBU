import java.io.File
import java.io.PrintWriter

abstract class Grammar(
        protected val sigma: Set<String>,
        protected val genProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        protected val transProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>,
        protected val delProductions: Map<List<LexicalElement>, Set<List<LexicalElement>>>
) {
    private var productionNumber: Int = 0

    init {
        for (entry in genProductions.entries) productionNumber += entry.value.size
        for (entry in transProductions.entries) productionNumber += entry.value.size
        for (entry in delProductions.entries) productionNumber += entry.value.size
    }

    open fun printMainInfo() {
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

    protected fun nextStep(currentChain: MutableList<LexicalElement>, output: MutableList<String>, leftPart: List<LexicalElement>, rightPart: List<LexicalElement>): Boolean {
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

    companion object {
        fun addProductionIntoGrammar(grammar: MutableMap<List<LexicalElement>, MutableSet<List<LexicalElement>>>, leftPart: List<LexicalElement>, rightPart: List<LexicalElement>) {
            val productions = grammar[leftPart]
            if (productions == null) {
                grammar[leftPart] = mutableSetOf(rightPart)
            } else {
                productions.add(rightPart)
            }
        }
    }
}

