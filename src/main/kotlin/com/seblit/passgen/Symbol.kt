package com.seblit.passgen

/**
 * Represents any UTF-16 symbol
 * @param codePoint The codepoint of the corresponding symbol
 * */
data class Symbol(val codePoint: Int) {
    /**
     * Creates a Symbol from a Char by using Char#code as codepoint
     * */
    constructor(character: Char) : this(character.code)

    /**
     * @return this Symbol as a String. Depending on the symbols codepoint this String may contain 2 Chars
     * */
    override fun toString(): String {
        return Character.toString(codePoint)
    }

    /**
     * Symbols are equal to another if they are assigned the same codepoint
     * @return true if `other` is a Symbol with the same codepoint as this
     * @param other the object to check equality with
     * */
    override fun equals(other: Any?): Boolean {
        if (other == null || other::class != this::class) return false
        other as Symbol
        return codePoint == other.codePoint
    }

    /**
     * @return the hashcode of this Symbol's codepoint
     * */
    override fun hashCode(): Int {
        return codePoint.hashCode()
    }
}