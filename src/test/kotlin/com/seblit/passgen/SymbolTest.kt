package com.seblit.passgen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SymbolTest {

    @Test
    fun testConstruction_Char(){
        val expectedChar = 'A'
        val symbol = Symbol(expectedChar)
        assertEquals(expectedChar.code, symbol.codePoint)
        assertEquals(Character.toString(expectedChar), symbol.toString())
    }

    @Test
    fun testConstruction_codePoint(){
        val expectedCodePoint = 'A'.code
        val symbol = Symbol(expectedCodePoint)
        assertEquals(expectedCodePoint, symbol.codePoint)
        assertEquals(Character.toString(expectedCodePoint), symbol.toString())
    }

    @Test
    fun testEquals(){
        val codePoint = 'A'.code
        val symbol = Symbol(codePoint)
        val same = Symbol(codePoint)
        val different = Symbol(codePoint+1)
        assertEquals(symbol, same)
        assertNotEquals(symbol, different)
    }

    @Test
    fun testHashcode(){
        val codePoint = 'A'.code
        val symbol = Symbol(codePoint)
        val same = Symbol(codePoint)
        val other = Symbol(codePoint+1)
        assertEquals(symbol.hashCode(), same.hashCode())
        assertNotEquals(symbol.hashCode(), other.hashCode())
    }

}