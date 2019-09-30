import java.io.File
import java.io.FileNotFoundException
import java.util.*

class LinearBoundedAutomaton(
        val sigma: Set<String>,
        val gamma: Set<String>,
        val leftMarker: String,
        val rightMarker: String,
        val states: Set<String>,
        val finalStates: Set<String>,
        val startState: String,
        val delta: Map<Pair<String, String>, Triple<String, String, String>>
) {
    private var transitionNumber: Int = 0

    init {
        for (entry in delta.entries) transitionNumber++
    }

    fun printMainInfo() {
        println("* * * Linear bounded automaton * * *")
        println("Sigma size: ${sigma.size}")
        println("Gamma size: ${gamma.size}")
        println("Total states: ${states.size}")
        println("Total transitions: $transitionNumber")
        println()
    }

    companion object {

        fun createFromFile(fileName: String): LinearBoundedAutomaton {
            return try {
                val sigma = mutableSetOf<String>()
                val gamma = mutableSetOf<String>()
                val states = mutableSetOf<String>()
                val finalStates = mutableSetOf<String>()
                val delta = mutableMapOf<Pair<String, String>, Triple<String, String, String>>()

                val scanner = Scanner(File(fileName))
                sigma.addAll(scanner.nextLine().split(" "))
                gamma.addAll(scanner.nextLine().split(" "))
                val s = scanner.nextLine().split(" ")
                val leftMarker = s[0]
                val rightMarker = s[1]
                val startState = scanner.nextLine()
                finalStates.addAll(scanner.nextLine().split(" "))

                while (scanner.hasNextLine()) {
                    val line = scanner.nextLine()
                    val data = line.split(" ")
                    if (data.size == 5) {
                        delta[data[0] to data[1]] = Triple(data[2], data[3], data[4])
                        states.add(data[0])
                        states.add(data[2])
                    }
                }
                LinearBoundedAutomaton(sigma, gamma, s[0], s[1], states, finalStates, startState, delta)
            } catch (e: FileNotFoundException) {
                error("Cannot read LinearBoundedAutomaton from file: $fileName")
            }
        }
    }
}