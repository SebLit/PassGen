package com.seblit.passgen

import java.security.SecureRandom
import java.util.*
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.max

/**
 * Generates randomized passwords based of provided Symbols, settings and rules
 * @param random The Random used for randomization during password generation. By default, a new instance of SecureRandom is used
 * */
class PasswordGenerator(private val random: Random = SecureRandom()) {

    private val groups = ArrayList<GroupEntry>()
    private val rules = ArrayList<(current: Password, nextSymbol: Symbol, nextIndex: Int) -> Boolean>()

    /**
     * Adds a rule to this generator. This rule will be notified about every Symbol that is about to be added to a password and may accept or reject it.
     * If rejected, the Symbol is not added to the password and a new Symbol will be generated
     * @param rule The rule to apply during password generation
     * @return the generator for method chaining
     * */
    fun addRule(rule: (current: Password, insertion: Symbol, index: Int) -> Boolean): PasswordGenerator {
        synchronized(rules) {
            rules.add(rule)
        }
        return this
    }

    /**
     * Adds a SymbolGroup to this generator. For groups that are already present this call will be ignored.
     * You may add Groups that contain the same symbols, as long as they also contain individual symbols (see equality check of SymbolGroup).
     * However, keep in mind that this will increase the chance for duplicate Symbols to occur in generated passwords.
     * Optionally you can assign weights to groups which influences the chance of their Symbols to occur in the generated password. Symbols
     * in a group with weight 2 are twice a likely to be picked than from a group with weight 1.
     * By default, for each added group at least 1 Symbol must occur in the generated password. To adjust this, set the required count of a group
     * to <= 0 if it should be optional or >1 to force multiple occurrences of its Symbols
     * @param group The SymbolGroup that should be added
     * @param weight The weight of the group, default 1. Calls with a weight of <=0 are ignored
     * @param required The count of Symbols from this group that must occur in every generated password, default 1. Set do <=0 to make the group optional
     * @return the generator for method chaining
     * */
    fun addSymbols(group: SymbolGroup, weight: Int = 1, required: Int = 1): PasswordGenerator {
        synchronized(groups) {
            if (group.size() > 0 && !groups.any { it.group == group }) {
                for (count in 0..<0.coerceAtLeast(weight)) {
                    groups.add(GroupEntry(group, required))
                }
            }
        }
        return this
    }

    /**
     * Randomly generates a password based on currently registered SymbolGroups, rules and provided settings.
     * The generation process goes through following steps
     * - Create a local copy of currently registered groups and rules to prevent configuration changes during the generation process
     * - Validate the generation state. If the password can't be generated due to bad configuration, an Exception is thrown
     * - Generate password symbols individually
     *    - Gather available groups (based on repetition setting)
     *    - Check if there are required groups that aren't available anymore -> aborts and throws an Exception
     *    - Pick a random group. If there are still required groups to be satisfied, they are preferred. Otherwise, chance is based of group weight
     *    - Filter group Symbols for availability (based on repetition settings) and pick a random Symbol and Index
     *    - Run all rules on newly picked Symbol -> Pick a new group and Symbol if rules should reject current pick
     *    - Add Symbol to Password
     *    - Repeat until desired length is reached
     * @param length The desired Password length. Note that this refers to Symbol count and final String length may differ
     * @param maxRepetitions The maximum amount of times the same Symbol may occur in the password. null to allow any number of the same Symbol. Default null
     * @return the generated Password
     * @throws IllegalArgumentException if length <=0
     * @throws IllegalArgumentException if length is smaller than count of required Symbols (specified by required param when adding groups)
     * @throws IllegalArgumentException if maxRepetitions <= 0. Set to null instead to allow any number
     * @throws IllegalArgumentException if maxRepetitions requires more unique Symbols than currently available
     * @throws IllegalArgumentException if it is not possible to add all required Symbols(specified by required param when adding groups) due to repetition requirement
     * @throws IllegalStateException if no SymbolGroups have been added to this generator yet
     * */
    fun generate(length: Int, maxRepetitions: Int? = null): Password {
        // create synchronized copies and check states for generation invocation
        val groups =
            synchronized(this.groups) {
                ArrayList(this.groups)
            }
        val requiredGroups = initializeGeneration(length, groups, maxRepetitions)
        val rules =
            synchronized(this.rules) {
                ArrayList(rules)
            }

        // store progress and result
        val passwordSymbols = ArrayList<Symbol>(length)
        var currentPassword = Password(arrayOf())

        repeat(length) {
            // check if there are repetition requirements
            val availableSymbols = loadAvailableSymbols(groups, currentPassword, maxRepetitions)
            val availableGroups = loadAvailableGroups(groups, availableSymbols, maxRepetitions)
            checkRequiredGroups(groups, availableGroups, requiredGroups, maxRepetitions)

            // generate next symbol
            val (nextIndex, nextSymbol, requiredGroupIndex) = generateNextSymbol(
                rules,
                groups,
                availableGroups,
                requiredGroups,
                availableSymbols,
                passwordSymbols,
                currentPassword,
                maxRepetitions
            )
            passwordSymbols.add(nextIndex, nextSymbol)
            currentPassword = Password(passwordSymbols.toArray(arrayOf()))

            // remove required group if one was used
            requiredGroupIndex?.let { requiredGroups.removeAt(it) }
        }
        return currentPassword
    }

    private fun generateNextSymbol(
        rules: List<(current: Password, nextSymbol: Symbol, nextIndex: Int) -> Boolean>,
        groups: List<GroupEntry>,
        availableGroups: List<GroupEntry>,
        requiredGroups: List<Int>,
        availableSymbols: List<Symbol>,
        passwordSymbols: List<Symbol>,
        currentPassword: Password,
        maxRepetitions: Int?
    ): Triple<Int, Symbol, Int?> {
        var nextIndex: Int
        var nextSymbol: Symbol
        var requiredGroupIndex: Int? = null
        do {
            // choose a group. account for requiredGroups first
            val nextGroup = if (requiredGroups.isEmpty()) availableGroups[random.nextInt(availableGroups.size)].group
            else {
                requiredGroupIndex = random.nextInt(requiredGroups.size)
                groups[requiredGroups[requiredGroupIndex]].group
            }
            // decides which symbols of groups may still be added
            val availableGroupSymbols = loadAvailableGroupSymbols(nextGroup, availableSymbols, maxRepetitions)
            // generate next symbol and run through all rules
            nextSymbol = availableGroupSymbols[random.nextInt(availableGroupSymbols.size)]
            nextIndex = random.nextInt(1.coerceAtLeast(passwordSymbols.size))
        } while (rules.any { !it(currentPassword, nextSymbol, nextIndex) })
        return Triple(nextIndex, nextSymbol, requiredGroupIndex)
    }

    private fun checkRequiredGroups(
        groups: List<GroupEntry>,
        availableGroups: List<GroupEntry>,
        requiredGroups: List<Int>,
        maxRepetitions: Int?
    ) {
        maxRepetitions?.let {
            if (requiredGroups.any { !availableGroups.contains(groups[it]) }) {
                throw IllegalArgumentException("Failed to apply all required groups due to max repetitions: $maxRepetitions")
            }
        }
    }

    private fun loadAvailableGroupSymbols(
        group: SymbolGroup,
        availableSymbols: List<Symbol>,
        maxRepetitions: Int?
    ): List<Symbol> {
        return maxRepetitions?.let {
            group.filter { availableSymbols.contains(it) }
        } ?: group.toList()
    }

    private fun loadAvailableGroups(
        groups: List<GroupEntry>,
        availableSymbols: List<Symbol>,
        maxRepetitions: Int?
    ): List<GroupEntry> {
        return maxRepetitions?.let {
            groups.stream().filter { it.group.any { symbol -> availableSymbols.contains(symbol) } }
                .toList()
        } ?: groups
    }

    private fun loadAvailableSymbols(
        groups: List<GroupEntry>,
        currentPassword: Password,
        maxRepetitions: Int?
    ): List<Symbol> {
        var stream = groups.stream().distinct().flatMap { it.group.toList().stream() }.distinct()
        maxRepetitions?.let {
            stream = stream.filter { symbol -> currentPassword.count { symbol == it } < maxRepetitions }
        }
        return stream.toList()
    }

    private fun initializeGeneration(length: Int, groups: List<GroupEntry>, maxRepetitions: Int?): MutableList<Int> {
        if (length <= 0) {
            throw IllegalArgumentException("Password length $length is not > 0")
        }
        if (groups.isEmpty()) {
            throw IllegalStateException("No symbols have been added yet")
        }
        maxRepetitions?.let {
            if (maxRepetitions <= 0) {
                throw IllegalArgumentException("maxRepetition set to $maxRepetitions. Set a positive value to enable or null to disable repetition constraints")
            }
            val minSymbolCount = length / maxRepetitions.toDouble()
            val uniqueSymbols =
                groups.stream().distinct().flatMap { it.group.stream() }.distinct().count()
            if (minSymbolCount > uniqueSymbols) {
                throw IllegalArgumentException("Password with length $length requires at least ${ceil(minSymbolCount)} unique Symbols but only $uniqueSymbols are available")
            }
        }
        val requiredGroups =
            groups.stream().distinct().filter { it.required > 0 }
                .flatMap { Collections.nCopies(it.required, it).stream() }.map { groups.indexOf(it) }
                .collect(Collectors.toList())
        if (requiredGroups.size > length) {
            throw IllegalArgumentException("Can't fit ${requiredGroups.size} required symbols into password with length $length ")
        }
        return requiredGroups
    }

    private data class GroupEntry(val group: SymbolGroup, val required: Int)


}