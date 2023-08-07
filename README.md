# Parser

## About the project

I have always wanted to understand what happens behind the scene every time I type my code in C, Python, Java, and
many other programming languages. The only way to do so is to build a small compiler(OK but maybe not from scratch :)).
The fun of engineering is to construct things and see how far they can take you. That is exactly what this project is
about. It reflects my journey in learning about compilers. However, compilers are difficult to build, so I'll split it
into a multipart project. This is the first part where I'll be constructing a lexer and a parser without using any
finite automata. If there is any way to improve this project, which I'm sure there is, any feedback will be appreciated.

## References

* My book of
  choice: [The Dragon Book](https://www.amazon.com/Compilers-Principles-Techniques-Tools-2nd/dp/0321486811).
* I also borrowed some ideas and read some code from these excellent resources:
    * [DoctorWkt on Github](https://github.com/DoctorWkt/acwj)
    * [Bob Nystrom's blog on Pratt's Parser](https://journal.stuffwithstuff.com/2011/03/19/pratt-parsers-expression-parsing-made-easy/)
    * [Matklad's Pratt parser implementation in Rust](https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html)
    * [Robert Nystrom's Crafting Interpreters](https://craftinginterpreters.com/)
* You cannot write a compiler without looking at some other compilers! So I chose the following list of compilers for
  references(mostly on grammar):
    * [C's grammar](https://learn.microsoft.com/en-us/cpp/c-language/c-language-syntax-summary?view=msvc-170)
    * [Swift's grammar](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/summaryofthegrammar#app-top)
    * [Swift's compiler](https://www.swift.org/swift-compiler/)
    * [Kotlin's grammar](https://kotlinlang.org/docs/reference/grammar.html)
* There are also websites that I used to play with Abstract Syntax Tree(AST):
    * [AST Explorer](https://astexplorer.net/)
    * [Swift AST Explorer](https://swift-ast-explorer.com/)

## Computer system basics

There are several phases in the compilation process:

* **Preprocessing**: modifies source code by processing include statements, directives and macros.
* **Compiling**: compiles preprocessed source code to assembly.
* **Assembling**: turns assembly instructions into relocatable machine code.
* **Linking**: links relocatable machine code with code from other object files to produce executables.

## Compiler v.s Interpreter

* **Compiler**: compiles the source language to a low-level target language.
* **Transpiler**: compiles the source language to another high-level target language.
* **Interpreter**: executes the source code, often line by line, to produce some output.
* **Hybrid**: this combines a compiler with an interpreter. For example, Java Virtual Machine(JVM) first compiles Java
  source code to an intermediate representation called bytecodes. JVM's interpreter then executes bytecode instructions
  to produce some output using a stack-based model.

## Compilation phases

* **Lexing(or tokenizing, aka lexical analysis)**: tokenizes the code and splits it into small units called
  tokens similar to how a sentence is tokenized into a list of words.
* **Parsing**: We can divide parsing into two smaller phases.
    * **Syntax analysis**: consumes the tokens and "stitches" them together by following some rules, or
      grammar. The result produced by parser is an AST.
    * **Semantic analysis**: figures out what the code is trying to do. Some things to do in this phase are
      type checking and resolving references to variables or functions.
* **Code generation**:
    * Traverses the AST to generate intermediate representation(IR) code.
    * There are two possible ways to process IR code:
        * Build an interpreter to execute each instruction.
        * Map IR code to other representations such as LLVM IR or WebAssembly.

## Lexer

### Architecture

The code for lexer is in the package `lexers`. It includes:

* **LexReader**: has an internal buffer which holds characters read from the input source.
* **AlnumUnderscoreLexer**: reads alphanumeric and underscore characters to form a token.
* **KeywordLexer**: inherits from AlnumUnderscoreLexer and reads a keyword token using the KeywordTable object.
* **OpLexer**: reads an operator token using the OperatorTable object.
* **NumLexer**: reads a number(integer or floating-point) token.
* **Lexer**: uses the lexers defined above to read a token in a switch-case or if-else fashion.
* **LexResult**: stores the result of extracting a token from the input source.

### Grammar

The grammar for lexer is discussed in `lexers/Grammar.md`.

## Parser

### Architecture

The code for parser is in the package `parsers`, which includes smaller packages:

* `expr`: contains the code for parsing expressions.
* `decl`: contains the code for parsing declaration statements.
* `ret`: contains the code for parsing return statements.
* `stmt`: contains the code for parsing general statements.
* `branch`: contains the code for parsing conditional branches and loops.
* `function`: contains the code for parsing functions.
* `scope`: contains the code for parsing scopes and blocks of code.
* `module`: contains the code for parsing modules.

In this project, smaller parsers typically consist of two components:

* The first pass checks if the syntax is correct, that is, if the grammatical rules are followed, and constructs an AST.
* The second pass traverses the constructed AST and checks the semantics, including identifiers, data types, and
  operator compatibilities.

In addition, parsing errors are not thrown directly using exceptions in Java but rather returned as an instance of
`ParseResult`. This gives the compiler more choice to handle them or bubble them up the stack.

Finally, global objects are stored in an instance of `ParseContext`, which is passed to parsing methods. We can then
initialize one instance of `ParseContext` in the main function or as we'll see later, one for each http request in our
demo app.

### Grammar

The grammar for parser is discussed in `parsers/Grammar.md`.

## Abstract Syntax Tree(AST)

The code for AST is stored in the package `ast`.

* AST nodes:
    * Each node has a value, a node type, and a data type.
    * `ASTNode`: the base class for other AST node classes.
* AST traversal:
    * `IASTVisitor`: an interface that uses the visitor pattern to traverse and process each node in the tree.
    * `IASTNodeIterator`: an interface that is used to loop over children nodes. This can only be applied to a node with
      multiple children, that is, it must inherit from `ListASTNode`.
    * `JSONWalker`: an implementation of `IASTVisitor` that displays the AST using JSON.

## Other components

### Tables

#### Keyword table

* The code for the keyword table is in the package `keywords`.
* The table is handcoded so values are predetermined.
* It stores all keywords that have been reserved for the language.

#### Operator table

* The code for the operator table is in the package `operators`.
* Similar to the keyword table, it is also handcoded.
* It is a collection of tables that store operators and their properties.

#### Type table

* The code for the type table is in the package `types`.
* Like other tables, it is handcoded.
* It is a collection of tables that contain primitive type information and mappings from literals to data types.

#### Symbol table

* The code for the symbol table is in the package `symbols`.
* Symbols refer to variables, constants, parameters, and functions.
* Symbol table maps an identifier to a `SymbolInfo` object that stores a symbol's information, including its data type.
* The currently supported primitive data types include 32-bit `Int`, `Float`, `Void`, and 8-bit `Bool`. Arrays are also
  being implemented.
* The compiler is statically typed(like C++ or Java) and not dynamically typed(like Python or JavaScript).

### Type compatibilities and operators

Each operator is only compatible with certain types. For example, binary addition is compatible with integers but not
boolean operands. Such specifications are stored in the operator table. During the parsing process, the parser looks up
the table to determine if the operands are compatible with the operator. If not, it raises a syntax error. There are
several classes associated with type compatibilities:

* **OpCompat**: the base class for operator compatibilities.
* **BinOpCompat**: a class inherited from `OpCompat` that determines binary operator compatibilities.
* **UnOpCompat**: a class inherited from `OpCompat` that determines unary operator compatibilities.

## Current language features

Keep in mind that the language is still buggy and these features might not work as expected.

### Variables

* **Variable declaration**: The user can use the `var` keyword to declare variables and `let` to declare constants.
* **Type inference**: the data type on the right-hand side of a variable definition can be deduced and assigned to the
  left-hand side. For example, the following is valid.

```
// Here, the compiler knows 'a' is of type Int using type inference
var a = 6;
// The statement above is equivalent to
var a:Int = 6;
```

### Functions

* A function is declared using the `fun` keyword.
* The compiler knows when a return statement is missing. If that is the case and the function returns type `Void`, it
  automatically adds a return statement to the function body.
* The compiler raises an error if the expected return type and the data type of the return value do not match.

```
// Sample function definition
fun def(a: Int): Int {...}
```

### Branches and loops

* The language supports if-else sequences and only while loops for now.
* The ternary operator is not supported.
* If-else blocks must be surrounded by curly braces.

```
// Sample if-else sequence
if (a == 3) {
  a = 4;
} elif (a == 4) {
  a = 5;
} else {
  a = 6;
}

// Not valid if-else
if (a == 3) a = 4;
else        a = 5;

// Sample while loop
while (a < 4) {
  a = a - 1;
}
```

### Program flow

There is no need for an entry point and the code is executed from top to bottom just like Python.

## Other related projects

* [ir](https://github.com/dashluu/ir): takes the AST produced by this parser and generates IR instructions.
* [langtools](https://github.com/dashluu/langtools): a demo web app that combines this parser with the IR
  generator [ir](https://github.com/dashluu/ir).