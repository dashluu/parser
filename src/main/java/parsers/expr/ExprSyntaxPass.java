package parsers.expr;

import exceptions.ErrMsg;
import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import operators.OpTable;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class ExprSyntaxPass {
    private Lexer lexer;
    private TokParser tokParser;
    private SyntaxBuff syntaxBuff;
    private static final ParseErr ERR = ParseErr.getInst();
    private static final OpTable OP_TABLE = OpTable.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param lexer     a lexer.
     * @param tokParser a parser that consumes valid tokens.
     */
    public void init(Lexer lexer, TokParser tokParser) {
        this.lexer = lexer;
        this.tokParser = tokParser;
    }

    // General expressions

    /**
     * Checks the syntax of an expression in a scope
     *
     * @param syntaxBuff a buffer containing syntax information.
     * @return a ParseResult object as the result of checking an expression's syntax.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<SyntaxInfo> eatExpr(SyntaxBuff syntaxBuff) throws IOException {
        this.syntaxBuff = syntaxBuff;
        return eatInfixExpr();
    }

    // Helper and utility methods

    /**
     * Consumes a prefix operator.
     *
     * @return a ParseResult object as the result of consuming a prefix operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatPrefixOp() throws IOException {
        LexResult<Tok> opResult = lexer.lookahead();
        if (opResult.getStatus() != LexStatus.OK) {
            return ERR.raise(opResult.getErrMsg());
        }

        Tok opTok = opResult.getData();
        if (opTok.getType() == TokType.EOS || !OP_TABLE.isPrefixOp(opTok.getType())) {
            return ParseResult.fail(opTok);
        }

        lexer.consume();
        syntaxBuff.add(new SyntaxInfo(opTok, SyntaxTag.PREFIX));
        return ParseResult.ok(null);
    }

    /**
     * Consumes a postfix operator.
     *
     * @return a ParseResult object as the result of consuming a postfix operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatPostfixOp() throws IOException {
        LexResult<Tok> tokResult = lexer.lookahead();
        if (tokResult.getStatus() != LexStatus.OK) {
            return ERR.raise(tokResult.getErrMsg());
        }

        Tok opTok = tokResult.getData();
        TokType opId = opTok.getType();
        if (opTok.getType() == TokType.EOS || !OP_TABLE.isPostfixOp(opId) || opId == TokType.TYPE_CONV) {
            return ParseResult.fail(opTok);
        }

        lexer.consume();
        syntaxBuff.add(new SyntaxInfo(opTok, SyntaxTag.POSTFIX));
        return ParseResult.ok(null);
    }

    // Primary expressions

    /**
     * Attempts to consume and check the syntax of a primary expression.
     * Grammar:
     * primary-expression: identifier | literal-expression | parenthesized-expression
     *
     * @return a ParseResult object as the result of checking the syntax of a primary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatPrimary() throws IOException {
        ParseResult<SyntaxInfo> result = eatIdClause();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        result = eatLiteral();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        return eatParen();
    }

    /**
     * Attempts to consume an identifier(id) and also an argument list following it.
     *
     * @return a ParseResult object as the result of consuming an id and an argument list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatIdClause() throws IOException {
        // Consume an id first
        ParseResult<Tok> idResult = tokParser.parseTok(TokType.ID);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(idResult.getFailTok());
        }

        SyntaxInfo syntaxInfo = new SyntaxInfo(idResult.getData(), SyntaxTag.ID);
        syntaxBuff.add(syntaxInfo);
        // Try consuming an argument list
        ParseResult<SyntaxInfo> argListResult = eatArgList();
        if (argListResult.getStatus() == ParseStatus.ERR) {
            return argListResult;
        } else if (argListResult.getStatus() == ParseStatus.OK) {
            syntaxInfo.setTag(SyntaxTag.FUN_CALL);
        }

        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume a function call's argument list.
     *
     * @return a ParseResult object as the result of consuming a function call's argument list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatArgList() throws IOException {
        // Consume '('
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        syntaxBuff.add(new SyntaxInfo(parenResult.getData(), SyntaxTag.LPAREN));
        ParseResult<Tok> commaResult;
        ParseResult<SyntaxInfo> exprResult;
        boolean end = false;
        // Boolean to indicate if this is the first argument
        boolean firstArg = true;

        while (!end) {
            // Consume ')'
            parenResult = tokParser.parseTok(TokType.RPAREN);
            if (parenResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (parenResult.getStatus() == ParseStatus.OK) {
                end = true;
                syntaxBuff.add(new SyntaxInfo(parenResult.getData(), SyntaxTag.RPAREN));
            } else {
                if (!firstArg) {
                    // If this is not the first argument in the list, ',' must be present
                    commaResult = tokParser.parseTok(TokType.COMMA);
                    if (commaResult.getStatus() == ParseStatus.ERR) {
                        return ParseResult.err();
                    } else if (commaResult.getStatus() == ParseStatus.FAIL) {
                        return ERR.raise(new ErrMsg("Missing ','", commaResult.getFailTok()));
                    }
                    syntaxBuff.add(new SyntaxInfo(commaResult.getData(), SyntaxTag.COMMA));
                }

                exprResult = eatInfixExpr();
                if (exprResult.getStatus() == ParseStatus.ERR) {
                    return exprResult;
                } else if (exprResult.getStatus() == ParseStatus.FAIL) {
                    return ERR.raise(new ErrMsg("Invalid argument expression", exprResult.getFailTok()));
                }

                firstArg = false;
            }
        }

        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume a literal expression.
     *
     * @return a ParseResult object as the result of consuming a literal.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatLiteral() throws IOException {
        LexResult<Tok> literalResult = lexer.lookahead();
        if (literalResult.getStatus() != LexStatus.OK) {
            return ERR.raise(literalResult.getErrMsg());
        }

        Tok literalTok = literalResult.getData();
        if (literalTok.getType() != TokType.INT_LITERAL &&
                literalTok.getType() != TokType.FLOAT_LITERAL &&
                literalTok.getType() != TokType.BOOL_LITERAL) {
            return ParseResult.fail(literalTok);
        }

        lexer.consume();
        syntaxBuff.add(new SyntaxInfo(literalTok, SyntaxTag.LITERAL));
        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume a parenthesized expression.
     * Grammar:
     * parenthesized-expression: '(' expression ')'
     *
     * @return a ParseResult object as the result of consuming an expression inside a pair of parentheses.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatParen() throws IOException {
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        syntaxBuff.add(new SyntaxInfo(parenResult.getData(), SyntaxTag.LPAREN));
        ParseResult<SyntaxInfo> exprResult = eatInfixExpr();
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        }

        parenResult = tokParser.parseTok(TokType.RPAREN);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ERR.raise(new ErrMsg("Missing ')'", parenResult.getFailTok()));
        }

        syntaxBuff.add(new SyntaxInfo(parenResult.getData(), SyntaxTag.RPAREN));
        return ParseResult.ok(null);
    }

    // Prefix expression

    /**
     * Attempts to consume a sequence of prefix operators.
     *
     * @return a ParseResult object as the result of consuming a sequence of prefix operators.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatPrefixOpSeq() throws IOException {
        ParseResult<SyntaxInfo> result = eatPrefixOp();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.FAIL) {
            return result;
        }

        boolean end = false;

        while (!end) {
            // Parse prefix operators in a sequence
            result = eatPrefixOp();
            if (result.getStatus() == ParseStatus.ERR) {
                return result;
            }
            end = result.getStatus() == ParseStatus.FAIL;
        }

        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume and check the syntax of a prefix expression.
     * Grammar:
     * prefix-expression: prefix-operator* postfix-expression
     *
     * @return a ParseResult object as the result of checking the syntax of a prefix expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatPrefixExpr() throws IOException {
        ParseResult<SyntaxInfo> prefixResult = eatPrefixOpSeq();
        if (prefixResult.getStatus() == ParseStatus.ERR) {
            return prefixResult;
        }

        ParseResult<SyntaxInfo> postfixResult = eatPostfixExpr();

        if (postfixResult.getStatus() == ParseStatus.ERR) {
            return postfixResult;
        } else if (postfixResult.getStatus() == ParseStatus.FAIL) {
            if (prefixResult.getStatus() == ParseStatus.FAIL) {
                // No prefix or postfix expression detected
                return postfixResult;
            } else {
                // There is at least one prefix operator but no postfix expression
                return ERR.raise(new ErrMsg("Expected an expression following the prefix operator",
                        postfixResult.getFailTok()));
            }
        }

        return ParseResult.ok(null);
    }

    // Postfix expressions

    /**
     * Attempts to consume and check the syntax of a postfix expression.
     * Grammar:
     * postfix-expression: postfix-expression postfix-operator
     * postfix-expression: primary-expression
     * postfix-expression: function-call-expression
     *
     * @return a ParseResult object as the result of checking the syntax of a postfix expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatPostfixExpr() throws IOException {
        // Try parsing primary expression
        ParseResult<SyntaxInfo> primaryResult = eatPrimary();
        if (primaryResult.getStatus() == ParseStatus.ERR || primaryResult.getStatus() == ParseStatus.FAIL) {
            return primaryResult;
        }

        ParseResult<SyntaxInfo> opResult;
        boolean end = false;

        while (!end) {
            // Parse postfix operators in a sequence
            opResult = eatPostfixOp();
            if (opResult.getStatus() == ParseStatus.ERR) {
                return opResult;
            }
            end = opResult.getStatus() == ParseStatus.FAIL;
        }

        return ParseResult.ok(null);
    }

    // Infix expressions

    /**
     * Attempts to consume an infix operator.
     *
     * @param opTok the operator token.
     * @return a ParseResult object as the result of consuming an infix operator.
     */
    private ParseResult<SyntaxInfo> eatInfixOp(Tok opTok) {
        TokType opId = opTok.getType();
        if (opId == TokType.EOS || opId == TokType.SEMICOLON || opId == TokType.COMMA ||
                opId == TokType.RPAREN || opId == TokType.RBRACKETS) {
            return ParseResult.fail(opTok);
        }
        // The lhs expression is guaranteed to be valid
        if (!OP_TABLE.isInfixOp(opId)) {
            return ERR.raise(new ErrMsg("Invalid infix operator '" + opTok.getVal() + "'", opTok));
        }
        return ParseResult.ok(null);
    }

    /**
     * Checks the syntax of an infix expression.
     *
     * @return a ParseResult object as the result of checking the syntax of an infix expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatInfixExpr() throws IOException {
        boolean firstOperand = true;
        ParseResult<SyntaxInfo> lresult, infixResult;
        LexResult<Tok> opResult;
        Tok opTok;

        while (true) {
            // Parse left operand
            lresult = eatPrefixExpr();
            if (lresult.getStatus() == ParseStatus.ERR) {
                return lresult;
            } else if (lresult.getStatus() == ParseStatus.FAIL) {
                if (firstOperand) {
                    return lresult;
                } else {
                    return ERR.raise(new ErrMsg("Invalid expression", lresult.getFailTok()));
                }
            }

            firstOperand = false;
            // Parse operator
            opResult = lexer.lookahead();
            if (opResult.getStatus() != LexStatus.OK) {
                return ERR.raise(opResult.getErrMsg());
            }

            opTok = opResult.getData();
            infixResult = eatInfixOp(opTok);
            if (infixResult.getStatus() == ParseStatus.ERR) {
                return infixResult;
            } else if (infixResult.getStatus() == ParseStatus.FAIL) {
                return lresult;
            }

            lexer.consume();
            syntaxBuff.add(new SyntaxInfo(opTok, SyntaxTag.INFIX));
        }
    }
}
