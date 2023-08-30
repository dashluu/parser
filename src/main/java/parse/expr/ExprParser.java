package parse.expr;

import ast.*;
import exceptions.ErrMsg;
import global_utils.Pair;
import lex.LexResult;
import lex.LexStatus;
import lex.Lexer;
import operators.OpTable;
import parse.dtype.DtypeParser;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import parse.utils.ParseStatus;
import parse.utils.TokMatcher;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class ExprParser {
    private Lexer lexer;
    private TokMatcher tokMatcher;
    private DtypeParser dtypeParser;
    private ExprSemanChecker semanChecker;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param lexer        a lexer.
     * @param tokMatcher   a token matcher.
     * @param dtypeParser  a data type parser.
     * @param semanChecker a semantic checker for the expression.
     */
    public void init(Lexer lexer, TokMatcher tokMatcher, DtypeParser dtypeParser, ExprSemanChecker semanChecker) {
        this.lexer = lexer;
        this.tokMatcher = tokMatcher;
        this.dtypeParser = dtypeParser;
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
        LexResult<Tok> opResult = lexer.lookAhead(context);
        if (opResult.getStatus() != LexStatus.OK) {
            return context.raiseErr(opResult.getErrMsg());
        }

        Tok opTok = opResult.getData();
        if (opTok.getTokType() == TokType.EOS || !context.getOpTable().isPrefixOp(opTok.getTokType())) {
            return ParseResult.fail(opTok);
        }

        lexer.consume(context);
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
        LexResult<Tok> tokResult = lexer.lookAhead(context);
        if (tokResult.getStatus() != LexStatus.OK) {
            return context.raiseErr(tokResult.getErrMsg());
        }

        Tok opTok = tokResult.getData();
        TokType opId = opTok.getTokType();
        if (opTok.getTokType() == TokType.EOS || !context.getOpTable().isPostfixOp(opId)) {
            return ParseResult.fail(opTok);
        }

        lexer.consume(context);
        UnASTNode opNode = new UnOpASTNode(opTok, null);
        return ParseResult.ok(opNode);
    }

    // Primary expressions

    /**
     * Parses a primary expression.
     *
     * @return a ParseResult object as the result of parsing a primary expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parsePrimary() throws IOException {
        ParseResult<ASTNode> result = parseIdClause();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        result = parseLiteralExpr();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        return parseParenExpr();
    }

    /**
     * Parses a list of expressions independent of the left and right bracket type.
     *
     * @param leftTokType  the left bracket type identified by its token type.
     * @param rightTokType the right bracket type identified by its token type.
     * @param isArrLiteral true if this is an array literal and false otherwise.
     * @return a ParseResult object as the result of parsing a list of expressions.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseList(TokType leftTokType, TokType rightTokType, boolean isArrLiteral)
            throws IOException {
        ParseResult<Tok> bracketResult = tokMatcher.match(leftTokType, context);
        if (bracketResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (bracketResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(bracketResult.getFailTok());
        }

        Tok bracketTok = bracketResult.getData();
        SrcPos bracketStartPos = bracketTok.getSrcRange().getStartPos();
        MultichildASTNode groupNode;
        if (isArrLiteral) {
            groupNode = new ArrLiteralASTNode(null);
        } else {
            groupNode = new ExprListASTNode(null);
        }

        ParseResult<ASTNode> exprResult;
        ParseResult<Tok> commaResult;
        boolean end = false;
        // Boolean to indicate if this is the first expression
        boolean firstExpr = true;

        while (!end) {
            bracketResult = tokMatcher.match(rightTokType, context);
            if (bracketResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (!(end = bracketResult.getStatus() == ParseStatus.OK)) {
                if (!firstExpr) {
                    // If this is not the first expression, ',' must be present
                    commaResult = tokMatcher.match(TokType.COMMA, context);
                    if (commaResult.getStatus() == ParseStatus.ERR) {
                        return ParseResult.err();
                    } else if (commaResult.getStatus() == ParseStatus.FAIL) {
                        return context.raiseErr(new ErrMsg("Missing ','", commaResult.getFailTok()));
                    }
                }

                exprResult = parseInfixExpr(null);
                if (exprResult.getStatus() == ParseStatus.ERR) {
                    return exprResult;
                } else if (exprResult.getStatus() == ParseStatus.FAIL) {
                    return context.raiseErr(new ErrMsg("Invalid expression", exprResult.getFailTok()));
                }

                groupNode.addChild(exprResult.getData());
                firstExpr = false;
            }
        }

        bracketTok = bracketResult.getData();
        SrcPos bracketEndPos = bracketTok.getSrcRange().getEndPos();
        SrcRange bracketRange = new SrcRange(bracketStartPos, bracketEndPos);
        groupNode.setSrcRange(bracketRange);
        return ParseResult.ok(groupNode);
    }

    /**
     * Parses an array access expression.
     *
     * @return a ParseResult object as the result of parsing the array access expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseArrAccess() throws IOException {
        // Look ahead 2 steps for '['
        LexResult<Tok> parenResult = lexer.lookAhead(2, context);
        if (parenResult.getStatus() == LexStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == LexStatus.OK && parenResult.getData().getTokType() != TokType.LSQUARE) {
            return ParseResult.fail(parenResult.getData());
        }

        ParseResult<ASTNode> idResult = parseId();
        if (idResult.getStatus() == ParseStatus.ERR || idResult.getStatus() == ParseStatus.FAIL) {
            return idResult;
        }

        ParseResult<ASTNode> indexListResult = parseList(TokType.LSQUARE, TokType.RSQUARE, false);
        if (indexListResult.getStatus() == ParseStatus.ERR || indexListResult.getStatus() == ParseStatus.FAIL) {
            return indexListResult;
        }

        ArrAccessASTNode arrAccessNode = new ArrAccessASTNode(null);
        IdASTNode idNode = (IdASTNode) idResult.getData();
        ExprListASTNode indexListNode = (ExprListASTNode) indexListResult.getData();
        arrAccessNode.setIdNode(idNode);
        arrAccessNode.setIndexListNode(indexListNode);
        return ParseResult.ok(arrAccessNode);
    }

    /**
     * Parses an identifier clause.
     *
     * @return a ParseResult object as the result of parsing the identifier clause.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseIdClause() throws IOException {
        // Try parsing a function call
        ParseResult<ASTNode> result = parseFunCall();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }

        // Try parsing an array data type
        result = dtypeParser.parseArrDtype(context);
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }

        // Try parsing an array access expression
        result = parseArrAccess();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }

        return parseId();
    }

    /**
     * Parses an identifier.
     *
     * @return a ParseResult object as the result of parsing the identifier.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseId() throws IOException {
        ParseResult<Tok> idResult = tokMatcher.match(TokType.ID, context);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(idResult.getFailTok());
        }

        ASTNode idNode = new IdASTNode(idResult.getData(), null, false);
        return ParseResult.ok(idNode);
    }

    /**
     * Parses a function call.
     *
     * @return a ParseResult object as the result of parsing the function call.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseFunCall() throws IOException {
        // Look ahead 2 steps for '('
        LexResult<Tok> parenResult = lexer.lookAhead(2, context);
        if (parenResult.getStatus() == LexStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == LexStatus.OK && parenResult.getData().getTokType() != TokType.LPAREN) {
            return ParseResult.fail(parenResult.getData());
        }

        ParseResult<ASTNode> idResult = parseId();
        if (idResult.getStatus() == ParseStatus.ERR || idResult.getStatus() == ParseStatus.FAIL) {
            return idResult;
        }

        ParseResult<ASTNode> argListResult = parseList(TokType.LPAREN, TokType.RPAREN, false);
        if (argListResult.getStatus() == ParseStatus.ERR || argListResult.getStatus() == ParseStatus.FAIL) {
            return argListResult;
        }

        FunCallASTNode funCallNode = new FunCallASTNode(null);
        IdASTNode idNode = (IdASTNode) idResult.getData();
        ExprListASTNode argListNode = (ExprListASTNode) argListResult.getData();
        funCallNode.setIdNode(idNode);
        funCallNode.setArgListNode(argListNode);
        return ParseResult.ok(funCallNode);
    }

    /**
     * Parses a literal expression.
     *
     * @return a ParseResult object as the result of parsing a literal expression.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseLiteralExpr() throws IOException {
        ParseResult<ASTNode> result = parseLiteral();
        if (result.getStatus() == ParseStatus.ERR || result.getStatus() == ParseStatus.OK) {
            return result;
        }
        return parseArrLiteral();
    }

    /**
     * Parses an array literal.
     *
     * @return a ParseResult object as the result of parsing the array literal.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseArrLiteral() throws IOException {
        ParseResult<ASTNode> arrLiteralResult = parseList(TokType.LSQUARE, TokType.RSQUARE, true);
        if (arrLiteralResult.getStatus() == ParseStatus.ERR || arrLiteralResult.getStatus() == ParseStatus.FAIL) {
            return arrLiteralResult;
        }

        ArrLiteralASTNode arrLiteralNode = (ArrLiteralASTNode) arrLiteralResult.getData();
        return ParseResult.ok(arrLiteralNode);
    }

    /**
     * Parses a literal.
     *
     * @return a ParseResult object as the result of parsing a literal.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseLiteral() throws IOException {
        LexResult<Tok> literalResult = lexer.lookAhead(context);
        if (literalResult.getStatus() != LexStatus.OK) {
            return context.raiseErr(literalResult.getErrMsg());
        }

        Tok literalTok = literalResult.getData();
        TokType literalTokType = literalTok.getTokType();
        if (literalTokType != TokType.INT_LITERAL &&
                literalTokType != TokType.FLOAT_LITERAL &&
                literalTokType != TokType.BOOL_LITERAL) {
            return ParseResult.fail(literalTok);
        }

        lexer.consume(context);
        LiteralASTNode literalNode = new LiteralASTNode(literalTok, null);
        return ParseResult.ok(literalNode);
    }

    /**
     * Parses a parenthesized expression.
     *
     * @return a ParseResult object as the result of parsing an expression inside a pair of parentheses.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseParenExpr() throws IOException {
        ParseResult<Tok> parenResult = tokMatcher.match(TokType.LPAREN, context);
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

        parenResult = tokMatcher.match(TokType.RPAREN, context);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing ')'", parenResult.getFailTok()));
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
                prevNode.setExprNode(currNode);
                prevNode = currNode;
            }
        }

        return ParseResult.ok(new Pair<>(root, prevNode));
    }

    /**
     * Parses a prefix expression.
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
                return context.raiseErr(new ErrMsg("Expected an expression following the prefix operator",
                        postfixResult.getFailTok()));
            }
        }

        if (root == null) {
            return postfixResult;
        }

        prefixLeaf.setExprNode(postfixResult.getData());
        return ParseResult.ok(root);
    }

    // Postfix expressions

    /**
     * Parses a postfix expression.
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
                postfixNode.setExprNode(root);
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
        LexResult<Tok> opResult = lexer.lookAhead(context);
        if (opResult.getStatus() != LexStatus.OK) {
            return context.raiseErr(opResult.getErrMsg());
        }

        Tok opTok = opResult.getData();
        TokType opId = opTok.getTokType();
        if (opId == TokType.EOS || opId == TokType.SEMI || opId == TokType.COMMA ||
                opId == TokType.RPAREN || opId == TokType.RCURLY || opId == TokType.RSQUARE) {
            return ParseResult.fail(opTok);
        }

        if (!context.getOpTable().isInfixOp(opId)) {
            return context.raiseErr(new ErrMsg("Invalid infix operator '" + opTok.getVal() + "'", opTok));
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
            if (prevOpTok != null && opTable.cmpPreced(opTok.getTokType(), prevOpTok.getTokType()) < 0) {
                // The current operator has lower precedence than the previous operator
                return leftResult;
            }

            lexer.consume(context);
            binOpNode = new BinOpASTNode(opTok, null);
            rightResult = parseInfixExpr(opTok);
            rightStatus = rightResult.getStatus();
            if (rightStatus == ParseStatus.ERR) {
                return rightResult;
            } else if (rightStatus == ParseStatus.FAIL) {
                return context.raiseErr(new ErrMsg("Invalid expression", rightResult.getFailTok()));
            }

            binOpNode.setLeft(leftResult.getData());
            binOpNode.setRight(rightResult.getData());
            leftResult = ParseResult.ok(binOpNode);
        }
    }
}
