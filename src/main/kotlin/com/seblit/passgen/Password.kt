package com.seblit.passgen

import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Represents a password made up of ordered Symbols. Produces as the output of a PasswordGenerator
 * */
class Password(private val symbols: Array<Symbol>) : Iterable<Symbol> {

    /**
     * @return an Iterator to all currently present Symbols
     * */
    override fun iterator(): Iterator<Symbol> {
        return symbols.iterator()
    }

    /**
     * @return a Stream to all currently present Symbols
     * */
    fun stream(): Stream<Symbol> {
        return StreamSupport.stream(this.spliterator(), false)
    }

    /**
     * Changes to the array won't take effect on the password
     * @return an Array with all currently present Symbols
     * */
    fun getSymbols(): Array<Symbol> {
        return symbols.clone()
    }

    /**
     * @return the Symbol at the provided index
     * @throws IndexOutOfBoundsException if index < 0 or >= length()
     * */
    operator fun get(index: Int): Symbol {
        return getSymbol(index)
    }

    /**
     * @return the Symbol count of this password
     * */
    fun length(): Int {
        return symbols.size
    }

    /**
     * @return the Symbol at the provided index
     * @throws IndexOutOfBoundsException if index < 0 or >= length()
     * */
    fun getSymbol(index: Int): Symbol {
        return symbols[index]
    }

    /**
     * Passwords are equal to another if they contain equal Symbols at the same order
     * @return true if `other` is a Password with equal Symbols at equal order as this
     * @param other the object to check equality with
     * */
    override fun equals(other: Any?): Boolean {
        if (other == null || other::class != this::class) return false
        other as Password
        return symbols.contentEquals(other.symbols)
    }

    /**
     * @return the hashcode of this Password's String representation
     * */
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    /**
     * @return a String consisting of all Symbols in order of this password
     * */
    override fun toString(): String {
        return symbols.joinToString(separator = "")
    }
}
