# Grammar

The following grammatical rules are inspired by those
in [Swift](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/summaryofthegrammar#app-top)
and [Kotlin](https://kotlinlang.org/docs/reference/grammar.html).

## Regex notations

I borrowed the following regular expression notations
from [Kotlin's grammar](https://kotlinlang.org/docs/reference/grammar.html).

* `|`: alternative.
* `*`: zero or more.
* `+`: one or more.
* `?`: zero or one.

## Expressions

```
expression -> infix-expression
primary-expression -> identifier | literal-expression | parenthesized-expression
parenthesized-expression -> '(' expression ')'
prefix-expression -> prefix-operator* postfix-expression
postfix-expression -> primary-expression postfix-operator*
postfix-operator -> type-casting-operator
infix-expression -> prefix-expression infix-operator infix-expression
type-casting-operator -> 'as' type
```

## Declarations

```
declaration -> variable-declaration | constant-declaration
variable-declaration -> variable-declaration-head variable-name variable-initializer-with-type
constant-declaration -> constant-declaration-head constant-name constant-initializer-with-type
variable-initializer-with-type -> type-annotation initializer | type-annotation | initializer
constant-initializer-with-type -> type-annotation initializer | initializer
variable-declaration-head -> 'var'
constant-declaration-head -> 'let'
variable-name -> identifier
constant-name -> identifier
type-annotation -> ':' type
initializer -> '=' expression
```

## Return statements

```
return-statement -> 'return' expression
```

## Statements

```
statement -> (expression | declaration | return-statement)? ';'
```

## If statements

```
if-statement -> 'if' condition block else-clause*
else-clause -> 'elif' condition block else-clause* | 'else' block
condition -> '(' expression ')'
```

## While statements

```
while-statement -> 'while' condition block
condition -> '(' expression ')'
```

## Scopes

Scope is defined to be a sequential collection of statements and blocks.

```
scope -> (statement | if-statement | while-statement | function | block)*
```

## Blocks

A block represents a block of code surrounded by a pair of left and right bracket.

```
block -> '{' scope '}'
```

## Functions

```
function -> function-head function-name parameter-clause function-return-type? function-body
function-name -> identifier
parameter-clause -> '(' ')' | '(' parameter-list ')'
parameter-list -> parameter ',' parameter-list | parameter
parameter -> parameter-name type-annotation
parameter-name -> identifier
function-return-type -> type-annotation
function-body -> block
```

## Modules

A module is considered as a single unit of code.

```
module -> scope
```