sealed class LexicalElement(val data: List<String>) {

    override fun toString(): String = if (data.size == 1) data[0] else data.toString()

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) {
            return false
        } else {
            return data == (other as LexicalElement).data
        }
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    class Terminal(s: String) : LexicalElement(listOf(s))

    class NonTerminal(data: List<String>) : LexicalElement(data) {

        constructor(s: String) : this(listOf(s))
    }
}