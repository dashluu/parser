package parsers.expr;

import ast.*;
import exceptions.ErrMsg;
import lexers.LexResult;
import lexers.LexStatus;
import lexers.Lexer;
import operators.OpTable;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import parsers.utils.TokParser;
import toks.Tok;
import toks.TokType;
import parsers.utils.ParseContext;
import global_utils.Pair;

import java.io.IOException;

public class ExprParser {
    private Lexer lexer;
    private TokParser tokParser;
    private ExprSemanChecker semanChecker;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param lexer        a lexer.
     * @param tokParser    a parser that consumes valid tokens.
     * @param semanChecker an expression semantics checker.
     */
    public void init(Lexer lexer, TokParser tokParser, ExprSemanChecker semanChecker) {
        this.lexer = lexer;
        this.tokParser = tokParser;
        this.semanChecker = semanChecker;
    }

    // General expressions

    /**
     * Parses an expression.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing an expression.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseExpr(ParseContext context) throws IOException {
        this.context = context;
        ParseResult<ASTNode> exprResult = parseInfixExpr(null);
        ParseStatus exprStatus = exprResult.getStatus();
        if (exprStatus == ParseStatus.ERR || exprStatus == ParseStatus.FAIL) {
            return exprResult;
        }
        ASTNode exprNode = exprResult.getData();
        return semanChecker.checkSeman(exprNode, context);
    }

    // Helper and utility methods

    /**
     * Parses a prefix operator.
     *
     * @return a ParseResult object as the result of parsing a prefix operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parsePrefixOp() throws IOException {
        LexResult<Tok> opResult = lexer.lookahead(context);
        if (opResult.getStatus() != LexStatus.OK) {
            return ParseErr.raise(opResult.getErrMsg());
        }

        Tok opTok = opResult.getData();
        if (opTok.getType() == TokType.EOS || !context.getOpTable().isPrefixOp(opTok.getType())) {
            return ParseResult.fail(opTok);
        }

        lexer.consume();
        UnASTNode opNode = new UnOpASTNode(opTok, null);
        return ParseResult.ok(opNode);
    }

    /**
     * Parses a postfix operator.
     *
     * @return a ParseResult object as the result of parsing a postfix operator.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parsePostfixOp() throws IOException {
        LexResult<Tok> tokResult = lexer.lookahead(context);
        if (tokResult.getStatus() != LexStatus.OK) {
            return ParseErr.raise(tokResult.getErrMsg());
        }

        Tok opTok = tokResult.getData();
        TokType opId = opTok.getType();
        if (opTok.getType() == TokType.EOS || !context.getOpTable().isPostfixOp(opId)) {
            return ParseResult.fail(opTok);
        }

        lexer.consume();
        UnASTNode opNode = new UnOpASTNode(opTok, null);
        return ParseResult.ok(opNode);
    }

    // Primary expressions

    /**
     * Parses a primary expression.
     * Grammar:
     * primary-expression: identifier | literal-expression | parenthesized-expression
     *
     * @return a ParseResult object as the result of parsing a primary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parsePrimary() throws IOException {
        ParseResult<ASTNode> result = parseIdClause();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        result = parseLiteral();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        return parseParenExpr();
    }

    /**
     * Parses an identifier and also an argument list following it if there is any.
     *
     * @return a ParseResult object as the result of parsing the identifier and the argument list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseIdClause() throws IOException {
        // Parse an id first
        ParseResult<Tok> idResult = tokParser.parseTok(TokType.ID, context);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(idResult.getFailTok());
        }

        Tok idTok = idResult.getData();
        // Try parsing an argument list
        ParseResult<ASTNode> argListResult = parseArgList(idTok);
        if (argListResult.getStatus() == ParseStatus.ERR) {
            return argListResult;
        } else if (argListResult.getStatus() == ParseStatus.OK) {
            return ParseResult.ok(argListResult.getData());
        }

        ASTNode idNode = new ASTNode(idTok, ASTNodeType.ID, null);
        return ParseResult.ok(idNode);
    }

    /**
     * Parses a function call's argument list.
     *
     * @param funIdTok the function's identifier token.
     * @return a ParseResult object as the result of parsing the function call's argument list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseArgList(Tok funIdTok) throws IOException {
        // Parse '('
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN, context);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        FunCallASTNode funCallNode = new FunCallASTNode(funIdTok, null);
        ParseResult<Tok> sepResult;
        ParseResult<ASTNode> argResult;
        boolean end = false;
        // Boolean to indicate if this is the first argument
        boolean firstArg = true;

        while (!end) {
            // Parse ')'
            parenResult = tokParser.parseTok(TokType.RPAREN, context);
            if (parenResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (!(end = parenResult.getStatus() == ParseStatus.OK)) {
                if (!firstArg) {
                    // If this is not the first argument in the list, ',' must be present
                    sepResult = tokParser.parseTok(TokType.COMMA, context);
                    if (sepResult.getStatus() == ParseStatus.ERR) {
                        return ParseResult.err();
                    } else if (sepResult.getStatus() == ParseStatus.FAIL) {
                        return ParseErr.raise(new ErrMsg("Missing ','", sepResult.getFailTok()));
                    }
                }

                argResult = parseInfixExpr(null);
                if (argResult.getStatus() == ParseStatus.ERR) {
                    return argResult;
                } else if (argResult.getStatus() == ParseStatus.FAIL) {
                    return ParseErr.raise(new ErrMsg("Invalid argument expression", argResult.getFailTok()));
                }

                funCallNode.addChild(argResult.getData());
                firstArg = false;
            }
        }

        return ParseResult.ok(funCallNode);
    }

    /**
     * Parses a literal expression.
     *
     * @return a ParseResult object as the result of parsing a literal expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseLiteral() throws IOException {
        LexResult<Tok> literalResult = lexer.lookahead(context);
        if (literalResult.getStatus() != LexStatus.OK) {
            return ParseErr.raise(literalResult.getErrMsg());
        }

        Tok literalTok = literalResult.getData();
        TokType literalTokType = literalTok.getType();
        if (literalTokType != TokType.INT_LITERAL &&
                literalTokType != TokType.FLOAT_LITERAL &&
                literalTokType != TokType.BOOL_LITERAL) {
            return ParseResult.fail(literalTok);
        }

        lexer.consume();
        LiteralASTNode literalNode = new LiteralASTNode(literalTok, null);
        return ParseResult.ok(literalNode);
    }

    /**
     * Parses a parenthesized expression.
     * Grammar:
     * parenthesized-expression: '(' expression ')'
     *
     * @return a ParseResult object as the result of parsing an expression inside a pair of parentheses.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseParenExpr() throws IOException {
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN, context);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        ParseResult<ASTNode> exprResult = parseInfixExpr(null);
        // Do not return when failed, parse ')' before returning
        if (exprResult.getStatus() == ParseStatus.ERR) {
            return exprResult;
        }

        parenResult = tokParser.parseTok(TokType.RPAREN, context);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Missing ')'", parenResult.getFailTok()));
        }

        return exprResult;
    }

    // Prefix expression

    /**
     * Parses a sequence of prefix operators.
     *
     * @return a ParseResult object as the result of parsing a sequence of prefix operators.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<Pair<ASTNode, ASTNode>> parsePrefixOpSeq() throws IOException {
        ParseResult<ASTNode> result = parsePrefixOp();
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        UnASTNode root = (UnASTNode) result.getData();
        UnASTNode prevNode = root;
        UnASTNode currNode;
        boolean end = false;

        while (!end) {
            // Parse prefix operators in a sequence
            result = parsePrefixOp();
            if (result.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (!(end = result.getStatus() == ParseStatus.FAIL)) {
                currNode = (UnASTNode) result.getData();
                prevNode.setChild(currNode);
                prevNode = currNode;
            }
        }

        return ParseResult.ok(new Pair<>(root, prevNode));
    }

    /**
     * Parses a prefix expression.
     * Grammar:
     * prefix-expression: prefix-operator* postfix-expression
     *
     * @return a ParseResult object as the result of parsing a prefix expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parsePrefixExpr() throws IOException {
        ParseResult<Pair<ASTNode, ASTNode>> prefixResult = parsePrefixOpSeq();
        ParseStatus prefixStatus = prefixResult.getStatus();
        ASTNode root = null;
        UnASTNode prefixLeaf = null;
        if (prefixStatus == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (prefixStatus == ParseStatus.OK) {
            root = prefixResult.getData().first();
            prefixLeaf = (UnASTNode) prefixResult.getData().second();
        }

        ParseResult<ASTNode> postfixResult = parsePostfixExpr();
        ParseStatus postfixStatus = postfixResult.getStatus();
        if (postfixStatus == ParseStatus.ERR) {
            return postfixResult;
        } else if (postfixStatus == ParseStatus.FAIL) {
            if (prefixStatus == ParseStatus.FAIL) {
                // No prefix or postfix expression detected
                return postfixResult;
            } else {
                // There is at least one prefix operator but no postfix expression
                return ParseErr.raise(new ErrMsg("Expected an expression following the prefix operator",
                        postfixResult.getFailTok()));
            }
        }

        if (root == null) {
            return postfixResult;
        }

        prefixLeaf.setChild(postfixResult.getData());
        return ParseResult.ok(root);
    }

    // Postfix expressions

    /**
     * Parses a postfix expression.
     * Grammar:
     * postfix-expression: postfix-expression postfix-operator
     * postfix-expression: primary-expression
     * postfix-expression: function-call-expression
     *
     * @return a ParseResult object as the result of parsing a postfix expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parsePostfixExpr() throws IOException {
        // Try parsing primary expression
        ParseResult<ASTNode> primaryResult = parsePrimary();
        ParseStatus primaryStatus = primaryResult.getStatus();
        if (primaryStatus == ParseStatus.ERR || primaryStatus == ParseStatus.FAIL) {
            return primaryResult;
        }

        ParseResult<ASTNode> opResult;
        ASTNode root = primaryResult.getData();
        UnASTNode postfixNode;
        boolean end = false;

        while (!end) {
            // Parse postfix operators in a sequence
            opResult = parsePostfixOp();
            if (opResult.getStatus() == ParseStatus.ERR) {
                return opResult;
            } else if (!(end = opResult.getStatus() == ParseStatus.FAIL)) {
                postfixNode = (UnASTNode) opResult.getData();
                postfixNode.setChild(root);
                root = postfixNode;
            }
        }

        return ParseResult.ok(root);
    }

    // Infix expressions

    /**
     * Parses an infix operator.
     *
     * @return a ParseResult object as the result of parsing an infix operator.
     */
    private ParseResult<Tok> parseInfixOp() throws IOException {
        LexResult<Tok> opResult = lexer.lookahead(context);
        if (opResult.getStatus() != LexStatus.OK) {
            return ParseErr.raise(opResult.getErrMsg());
        }

        Tok opTok = opResult.getData();
        TokType opId = opTok.getType();
        if (opId == TokType.EOS || opId == TokType.SEMICOLON || opId == TokType.COMMA ||
                opId == TokType.RPAREN || opId == TokType.RBRACKETS) {
            return ParseResult.fail(opTok);
        }

        if (!context.getOpTable().isInfixOp(opId)) {
            return ParseErr.raise(new ErrMsg("Invalid infix operator '" + opTok.getVal() + "'", opTok));
        }

        return ParseResult.ok(opTok);
    }

    /**
     * Parses an infix expression using Pratt's algorithm.
     *
     * @param prevOpTok the previous operator token.
     * @return a ParseResult object as the result of parsing the infix expression.
     */
    private ParseResult<ASTNode> parseInfixExpr(Tok prevOpTok) throws IOException {
        ParseResult<ASTNode> leftResult = parsePrefixExpr();
        ParseStatus leftStatus = leftResult.getStatus();
        if (leftStatus == ParseStatus.ERR || leftStatus == ParseStatus.FAIL) {
            return leftResult;
        }

        OpTable opTable = context.getOpTable();
        ParseResult<Tok> opResult;
        Tok opTok;
        ParseResult<ASTNode> rightResult;
        ParseStatus rightStatus;
        BinOpASTNode binOpNode;

        while (true) {
            opResult = parseInfixOp();
            if (opResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (opResult.getStatus() == ParseStatus.FAIL) {
                return leftResult;
            }

            opTok = opResult.getData();
            if (prevOpTok != null && opTable.cmpPreced(opTok.getType(), prevOpTok.getType()) < 0) {
                // The current operator has lower precedence than the previous operator
                return leftResult;
            }

            lexer.consume();
            binOpNode = new BinOpASTNode(opTok, null);
            rightResult = parseInfixExpr(opTok);
            rightStatus = rightResult.getStatus();
            if (rightStatus == ParseStatus.ERR) {
                return rightResult;
            } else if (rightStatus == ParseStatus.FAIL) {
                return ParseErr.raise(new ErrMsg("Invalid expression", rightResult.getFailTok()));
            }

            binOpNode.setLeft(leftResult.getData());
            binOpNode.setRight(rightResult.getData());
            leftResult = ParseResult.ok(binOpNode);
        }
    }
}
