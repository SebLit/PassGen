package com.seblit.passgen

import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Represents a group of Symbols that can be provided to a PasswordGenerator.
 * Note that Symbols can only occur once in a group and have no order
 * */
class SymbolGroup : Iterable<Symbol> {

    private val symbols = HashSet<Symbol>()

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
     * Changes to the array or group won't take effect on another
     * @return an Array with all currently present Symbols
     * */
    fun getSymbols(): Array<Symbol> {
        return symbols.toArray(arrayOf())
    }

    /**
     * Adds all Symbols from the provided collection to this group. Duplicates and already present Symbols will be ignored
     * @param symbols The Symbols to add
     * @return the group for method chaining
     * */
    fun addSymbols(symbols: Collection<Symbol>): SymbolGroup {
        this.symbols.addAll(symbols)
        return this
    }

    /**
     * Adds all Symbols from the provided vararg parameter to this group. Duplicates and already present Symbols will be ignored
     * @param symbols The Symbols to add
     * @return the group for method chaining
     * */
    fun addSymbols(vararg symbols: Symbol): SymbolGroup {
        this.symbols.addAll(symbols)
        return this
    }

    /**
     * Adds all UTF-16 Symbols that match the provided Regex. Already present Symbols will be ignored
     * @param regex The Regex to include the desired Symbols
     * @return the group for method chaining
     * */
    fun addSymbols(regex: Regex): SymbolGroup {
        return addSymbols { codePoint -> Character.toString(codePoint).matches(regex) }
    }

    /**
     * Adds all codepoints from the provided String as Symbols to this group. Duplicates and already present Symbols will be ignored
     * @param symbols The Symbols to add
     * @return the group for method chaining
     * */
    fun addSymbols(symbols: String): SymbolGroup {
        symbols.codePoints().distinct().forEach { this.symbols.add(Symbol(it)) }
        return this
    }

    /**
     * Adds all UTF-16 Symbols that are accepted by the provided callback. Already present Symbols will be ignored
     * @param matcher The callback to accept or decline specific codepoints. Returns true to accept and add to the group, false otherwise
     * @return the group for method chaining
     * */
    fun addSymbols(matcher: (codePoint: Int) -> Boolean): SymbolGroup {
        for (codePoint in Character.MIN_CODE_POINT..Character.MAX_CODE_POINT) {
            if (Character.isValidCodePoint(codePoint) && matcher(codePoint)) {
                symbols.add(Symbol(codePoint))
            }
        }
        return this;
    }

    /**
     * @return the current Symbol count
     * */
    fun size(): Int {
        return symbols.size
    }

    /**
     * SymbolGroups are equal to another if they contain equal Symbols
     * @return true if `other` is a SymbolGroup with equal Symbols as this
     * @param other the object to check equality with
     * */
    override fun equals(other: Any?): Boolean {
        if (other == null || other::class != this::class) return false
        other as SymbolGroup
        return symbols == other.symbols
    }

    /**
     * @return the sum of the hashcodes of all contained Symbols
     * */
    override fun hashCode(): Int {
        return symbols.sumOf { it.hashCode() }
    }

    /**
     * @return a String representation of this group containing all currently present Symbols as Strings
     * */
    override fun toString(): String {
        return "SymbolGroup[${symbols.joinToString(",", "{", "}")}]"
    }

}