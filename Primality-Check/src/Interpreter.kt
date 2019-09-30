import java.lang.Math.abs

class Interpreter() {

    private val rightTapePart = mutableListOf<String>()
    private val leftTapePart = mutableListOf<String>()
    private var blank = ""

    fun run(input: String, machine: TuringMachine) {
        blank = machine.blank
        var currentPosition = 0

        for (symbol in input) {
            printOnTape(currentPosition, symbol.toString())
            currentPosition++
        }

        var currentState = machine.startState
        currentPosition = 0
        while (true) {
            val pair = currentState to getTapeSymbol(currentPosition)
            if (machine.delta.contains(pair)) {
                val transition = machine.delta[pair]!!
                currentState = transition.first
                printOnTape(currentPosition, transition.second)
                currentPosition += if (transition.third == "right") 1 else -1
            } else break
//            printTape(currentPosition)
        }
        println("Number: ${input.length}; Final state: $currentState")
        clear()
    }

    fun run(input: String, machine: LinearBoundedAutomaton) {
        var currentPosition = 0

        for (symbol in input) {
            printOnTape(currentPosition, symbol.toString())
            currentPosition++
        }

        var currentState = machine.startState
        currentPosition = 1
        while (true) {
            val pair = currentState to getTapeSymbol(currentPosition)
            if (machine.delta.contains(pair)) {
                val transition = machine.delta[pair]!!
                currentState = transition.first
                printOnTape(currentPosition, transition.second)
                currentPosition += if (transition.third == "right") 1 else -1
            } else break
//            printTape(currentPosition)
        }
        println("Number: ${input.length - 2}; Final state: $currentState")
        clear()
    }


    private fun getTapeSymbol(position: Int): String {
        return if (position >= 0) {
            if (position < rightTapePart.size) {
                rightTapePart[position]
            } else blank
        } else {
            val realPosition = abs(position + 1)
            if (realPosition < leftTapePart.size) {
                leftTapePart[abs(position + 1)]
            } else blank
        }
    }

    private fun printOnTape(position: Int, symbol: String) {
        if (position >= 0) {
            if (position < rightTapePart.size) {
                rightTapePart[position] = symbol
            } else {
                rightTapePart.add(symbol)
            }
        } else {
            val realPosition = abs(position + 1)
            if (realPosition < leftTapePart.size) {
                leftTapePart[realPosition] = symbol
            } else {
                leftTapePart.add(symbol)
            }
        }
    }

    private fun printTape(currentPosition: Int) {
        print("Tape: ")
        for (i in leftTapePart.size - 1 downTo 0) {
            print(leftTapePart[i])
            if (-i - 1 == currentPosition) print(".")
        }
        for (i in 0 until rightTapePart.size) {
            print(rightTapePart[i])
            if (i == currentPosition) print(".")
        }
        println()
    }

    private fun clear() {
        rightTapePart.clear()
        leftTapePart.clear()
        blank = ""
    }
}

