package com.seblit.passgen

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SymbolGroupTest {

    @Test
    fun testAddSymbols_Collection() {
        val expectedSymbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val collection = listOf(*expectedSymbols)
        val symbols = SymbolGroup().addSymbols(collection).getSymbols()
        assertContentEquals(expectedSymbols, symbols)
    }

    @Test
    fun testAddSymbols_varargs() {
        val expectedSymbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val symbols = SymbolGroup().addSymbols(*expectedSymbols).getSymbols()
        assertContentEquals(expectedSymbols, symbols)
    }

    @Test
    fun testAddSymbols_Regex() {
        val expectedSymbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val regex = Regex("[abc]")
        val symbols = SymbolGroup().addSymbols(regex).getSymbols()
        assertContentEquals(expectedSymbols, symbols)
    }

    @Test
    fun testAddSymbols_matcher() {
        val expectedSymbols = arrayOf(Symbol('a'), Symbol('b'), Symbol('c'))
        val matcher = {codePoint:Int -> when(Character.toString(codePoint)) {
            "a", "b", "c" -> true
            else -> false
        } }
        val symbols = SymbolGroup().addSymbols(matcher).getSymbols()
        assertContentEquals(expectedSymbols, symbols)
    }

    @Test
    fun testSize() {
        val size = SymbolGroup().addSymbols(Regex("[abc]")).size()
        assertEquals(3, size)
    }

    @Test
    fun testEquals() {
        val commonSymbol = Symbol('a')
        val uniqueSymbol = Symbol('b')
        val group = SymbolGroup().addSymbols(commonSymbol)
        val same = SymbolGroup().addSymbols(commonSymbol)
        val other = SymbolGroup().addSymbols(uniqueSymbol)
        assertEquals(group, same)
        assertNotEquals(group, other)
    }

    @Test
    fun testHashcode() {
        val commonSymbol = Symbol('a')
        val uniqueSymbol = Symbol('b')
        val group = SymbolGroup().addSymbols(commonSymbol)
        val same = SymbolGroup().addSymbols(commonSymbol)
        val other = SymbolGroup().addSymbols(uniqueSymbol)
        assertEquals(group.hashCode(), same.hashCode())
        assertNotEquals(group.hashCode(), other.hashCode())
    }

}