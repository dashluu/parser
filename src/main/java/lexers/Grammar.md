# Grammar

## Regex notations

I borrowed the following regular expression notations
from [Kotlin's grammar](https://kotlinlang.org/docs/reference/grammar.html).

* `|`: alternative.
* `*`: zero or more.
* `+`: one or more.
* `?`: zero or one.

## Alphanumeric and underscores

Grammatical rule for alphanumeric and underscore lexemes used in keywords, data types, and operators:

```
alnum_ -> ('_'|('a'-'z')|('A'-'Z'))('_'|('a'-'z')|('A'-'Z')|('0'-'9'))*
```

## Numeric expressions

```
digit -> '0' | '1' | ... | '9'
digits -> digit+
frac -> '.' digits
optFrac -> frac?
optExp -> ('e' ('+' | '-')? digits)?
number -> digits optFrac optExp | frac optExp
```