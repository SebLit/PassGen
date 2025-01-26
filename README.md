# PassGen
Provides an API to generate random passwords based of dynamic sets of Symbols and rules

# Symbol
A `Symbol` represents any UTF-16 character. `PasswordGenerator` generates 
its output by chaining Symbols. Initialize it either from a character or a code point
~~~
Symbol('a') // represents letter a
Symbol(0x1F600) // represents grinning emoji
~~~

# SymbolGroup
A `SymbolGroup` is a collection of one or more Symbols. They are provided 
to a `PasswordGenerator` to declare the set of Symbols the generator will use. 
You can add Symbols to a group individually or through its helper methods
~~~
SymbolGroup()
  .addSymbols(someSymbol) // adds a Symbol individually
  .addSymbols('a', 'b') // adds characters from varargs as individual Symbols
  .addSymbols("qwert") // adds characters from string as individual Symbols
  .addSymbols(someSymbolCollection) // adds symbols from a Collection
  .addSymbols(Regex("[0-9]") // adds all characters that match the regex, here digits 0 to 9
  .addSymbols{codePoint:Int -> when(Character.toString(codePoint)) { // adds all symbols that match the function
            "a", "b", "c" -> true
            else -> false
   }}
~~~



# PasswordGenerator
Generates random passwords of any length based on provided Symbols, settings and rules. 
Randomization is based of a `Random` instance. By default a `SecureRandom`is used, but 
a custom Random can be provided at instance creation
~~~
PasswordGenerator(someRandom)
~~~

## Symbols and settings
`PasswordGenerator` randomly picks `Symbols` from the `SymbolGroups` that have been provided to it. Simply add 
the desired groups to the generator, before calling the generate method
~~~
val password = PasswordGenerator()
  .addSymbols(someSymbolGroup)
  .generate(10) // password with 10 Symbol length
~~~

Groups can be required or optional. If required, the specified count determines the minimum amount of Symbols 
that will be present in every generated password from this group.
~~~
val password = PasswordGenerator()
  .addSymbols(requiredGroup) // 1 or more symbol must occur in password
  .addSymbols(multipleRequiredGroup, required = 2) // 2 or more symbols must occur in password
  .addSymbols(optionalGroup, required = 0) // 0 or more may occur in password
  .generate(10)
~~~

By assigning weights to groups you can influence the probability for Symbols from that group to occur in the password. 
A SymbolGroup with a weight of 2 has twice the probability in comparison to a SymbolGroup with a weight of 1

~~~
val password = PasswordGenerator()
  .addSymbols(lowWeightGroup, weight=1)
  .addSymbols(highWeightGroup, weight=2) // twice as likely to be picked for password generation
  .generate(10)
~~~

Another setting that can be configured for each password is Symbol repetition. By providing this value you can control 
how often the same Symbol is allowed to occur within the password
~~~
PasswordGenerator()
  .addSymbols(someSymbols)
  .generate(10, 2) // allows the same Symbol to occur max 2 times in the password
~~~

## Rules
Through custom callbacks it is possible to add additional rules that can add control to structure and content 
of a password. These may be things like preventing undesired patterns (i.e. asc/desc number sequences) or preventing 
Symbols under specific circumstances to occur in a password (i.e. if lower case letter already present, prevent same 
upper case letter and vice versa).  

~~~
PasswordGenerator()
    .addSymbols(numberGroup)
    .addRule{current, insertion, index ->
        // prevents asc/desc number sequences
        val number = insertion.toString().toInt()
        if(index != 0){
            // check if previous symbol is +-1 of value
            if(Math.abs(number - current[index-1].toString().toInt()) == 1){
                false
            }
        }
        if(index != current.length()){
            // check if following symbol is +-1 of value
            if(Math.abs(number - current[index].toString().toInt()) == 1){
                false
            }
        }
        true
    }.generate(10)
~~~