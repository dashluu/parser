# Lexer

## Structural components

The code for lexer is in the package `lexers`. It includes:

* **CharReader**: has an internal buffer which holds characters read from the input stream.
* **AlnumUnderscoreLexer**: reads alphanumeric and underscore characters to form a lexeme.
* **KeywordLexer**: inherits from AlnumUnderscoreLexer and reads a keyword lexeme using the KeywordTable object.
* **OpLexer**: reads an operator lexeme using the OperatorTable object.
* **NumLexer**: reads a number(integer or floating-point) lexeme.
* **Lexer**: uses one of the component lexers above to read a lexeme in a switch-case or if-else fashion.

## Grammar

### Regex notations

I borrowed the following regular expression notations
from [Kotlin's grammar](https://kotlinlang.org/docs/reference/grammar.html).

* `|`: alternative.
* `*`: zero or more.
* `+`: one or more.
* `?`: zero or one.

### Alphanumeric and underscores

Grammatical rule for alphanumeric and underscore lexemes used in keywords, data types, and operators:

```
alnum_ -> ('_'|('a'-'z')|('A'-'Z'))('_'|('a'-'z')|('A'-'Z')|('0'-'9'))*
```

### Numeric expressions

```
digit -> '0' | '1' | ... | '9'
digits -> digit+
frac -> '.' digits
optFrac -> frac?
optExp -> ('e' ('+' | '-')? digits)?
number -> digits optFrac optExp | frac optExp
```