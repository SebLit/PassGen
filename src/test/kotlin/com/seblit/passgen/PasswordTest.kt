package com.seblit.passgen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PasswordTest {

    @Test
    fun testLength() {
        val symbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        assertEquals(symbols.size, Password(symbols).length())
    }

    @Test
    fun testGetSymbol() {
        val symbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val password = Password(symbols)
        for (index in symbols.indices) {
            assertEquals(symbols[index], password[index])
            assertEquals(symbols[index], password.getSymbol(index))
        }
    }

    @Test
    fun testEquals() {
        val symbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val otherSymbols = arrayOf(Symbol('d'))
        val password = Password(symbols)
        val same = Password(symbols)
        val other = Password(otherSymbols)
        assertEquals(password, same)
        assertNotEquals(password, other)
    }

    @Test
    fun testHashcode() {
        val symbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val otherSymbols = arrayOf(Symbol('d'))
        val password = Password(symbols)
        val same = Password(symbols)
        val other = Password(otherSymbols)
        assertEquals(password.hashCode(), same.hashCode())
        assertNotEquals(password.hashCode(), other.hashCode())
    }

    @Test
    fun testToString() {
        val expected = "some string with utf16 symbols Ω"
        val symbols: Array<Symbol> = expected.codePoints().mapToObj(::Symbol).toArray { arrayOfNulls<Symbol>(it) }
        val result = Password(symbols).toString()
        assertEquals(expected, result)
    }

}