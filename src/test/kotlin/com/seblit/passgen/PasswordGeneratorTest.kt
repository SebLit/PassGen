package com.seblit.passgen

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordGeneratorTest {

    @Test
    fun testGenerate() {
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(Regex("[A-Z]")))
        val length = 100;
        val password = generator.generate(length)
        assertEquals(length, password.length())
    }

    @Test
    fun testGenerate_withRule() {
        val rule = mock<(current: Password, insertion: Symbol, index: Int) -> Boolean>()
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(Regex("[A-Z]")))
            .addRule(rule)
        val length = 100
        whenever(rule.invoke(any(), any(), any())).thenReturn(true)

        generator.generate(length)
        verify(rule, times(length)).invoke(any(), any(), any())

        reset(rule)
        val switch = AtomicBoolean(true) // simulates decline of every second validation
        whenever(rule.invoke(any(), any(), any())).thenAnswer {
            switch.set(!switch.get())
            switch.get()
        }
        generator.generate(length)
        verify(rule, times(length * 2)).invoke(any(), any(), any())
    }

    @Test
    fun testGenerate_optionals() {
        val optionalSymbol = Symbol('o')
        val requiredGroupCount = 3
        val mockedRandom = mock<Random>()
        val generator = PasswordGenerator(mockedRandom)
            .addSymbols(SymbolGroup().addSymbols(Regex("[A-Z]")))
            .addSymbols(SymbolGroup().addSymbols(Regex("[0-9]")))
            .addSymbols(SymbolGroup().addSymbols("!\"§$%&/()=?"))
            .addSymbols(SymbolGroup().addSymbols(optionalSymbol), required = 0)
        whenever(mockedRandom.nextInt(any())).thenAnswer {
            // force last index (optional group) when generator picks next group and is done with required ones
            val isFullGroupCount = it.arguments[0] == requiredGroupCount + 1
            if (isFullGroupCount) requiredGroupCount
            else
                it.callRealMethod()
        }
        for (count in 0..100) {
            // run a bunch of samples to eliminate chance of random test success
            // passwords with only the required symbol count as length will never contain the optional symbol
            // passwords with a higher count are forced to add the optional symbol through overridden random
            assertFalse { generator.generate(requiredGroupCount).getSymbols().any { it == optionalSymbol } }
            assertTrue { generator.generate(requiredGroupCount + 1).getSymbols().any { it == optionalSymbol } }
        }

    }

    @Test
    fun testGenerate_requiredMultiple() {
        val tripleSymbol = Symbol('3')
        val dualSymbol = Symbol('2')
        val singleSymbol = Symbol('1')
        val passwordSymbols = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(tripleSymbol), required = 3)
            .addSymbols(SymbolGroup().addSymbols(dualSymbol), required = 2)
            .addSymbols(SymbolGroup().addSymbols(singleSymbol))
            .generate(6)
            .getSymbols()

        assertEquals(3, passwordSymbols.count { it == tripleSymbol })
        assertEquals(2, passwordSymbols.count { it == dualSymbol })
        assertEquals(1, passwordSymbols.count { it == singleSymbol })

    }

    @Test
    fun testGenerate_weight() {
        val lowWeightSymbol = Symbol('l')
        val highWeightSymbol = Symbol('h')
        val lowWeight = 1
        val highWeight = 10
        val expectedRatio = lowWeight / highWeight.toDouble()
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(lowWeightSymbol), weight = lowWeight)
            .addSymbols(SymbolGroup().addSymbols(highWeightSymbol), weight = highWeight)

        for (count in 0..1000) {
            // run a bunch of samples to eliminate chance of random test success. use large lengths to get a reliable average
            val passwordSymbols = generator.generate(1000).getSymbols()
            val lowWeightCount = passwordSymbols.count { it == lowWeightSymbol }
            val highWeightCount = passwordSymbols.count { it == highWeightSymbol }
            val ratio = lowWeightCount / highWeightCount.toDouble()
            assertEquals(expectedRatio, ratio, 0.1)
        }
    }

    @Test
    fun testGenerate_customRandom() {
        val spyRandom = spy<Random>()
        val generator = PasswordGenerator(spyRandom)
            .addSymbols(SymbolGroup().addSymbols("test"))
        val length = 10
        generator.generate(length)
        // random group + random symbol + random index -> invocations = length * 3
        verify(spyRandom, times(length * 3)).nextInt(any())
    }

    @Test
    fun testGenerate_missingSymbols() {
        assertThrows<IllegalStateException> {
            PasswordGenerator().generate(1)
        }
    }

    @Test
    fun testGenerate_lengthRequirement() {
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(Symbol('a')))
            .addSymbols(SymbolGroup().addSymbols(Symbol('b')))
        assertThrows<IllegalArgumentException> {
            generator.generate(1)
        }
    }

    @Test
    fun testGenerate_invalidLength() {
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols("test"))
        assertThrows<IllegalArgumentException> {
            generator.generate(-1)
        }
    }

    @Test
    fun testGenerate_symbolRepetition_invalidSymbolCount() {
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(Symbol('c')))
        assertThrows<IllegalArgumentException> {
            generator.generate(2, 1)
        }
    }

    @Test
    fun testGenerate_symbolRepetition_requiredGroupFailure() {
        val generator = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(Symbol('c')), required = 2)
            .addSymbols(SymbolGroup().addSymbols("123456789"), required = 0)
        assertThrows<IllegalArgumentException> {
            generator.generate(2, 1)
        }
    }

    @Test
    fun testGenerate_symbolRepetition_testConstraint() {
        val firstSymbol = Symbol('1')
        val secondSymbol = Symbol('2')
        val thirdSymbol = Symbol('3')
        val fourthSymbol = Symbol('4')
        val password = PasswordGenerator()
            .addSymbols(SymbolGroup().addSymbols(firstSymbol))
            .addSymbols(SymbolGroup().addSymbols(secondSymbol))
            .addSymbols(SymbolGroup().addSymbols(thirdSymbol))
            .addSymbols(SymbolGroup().addSymbols(fourthSymbol))
            .generate(8, 2)
        assertEquals(2, password.count { it == firstSymbol })
        assertEquals(2, password.count { it == secondSymbol })
        assertEquals(2, password.count { it == thirdSymbol })
        assertEquals(2, password.count { it == fourthSymbol })
    }

}