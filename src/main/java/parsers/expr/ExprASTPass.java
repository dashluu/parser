package parsers.expr;

import ast.*;
import exceptions.ErrMsg;
import operators.OpTable;
import parsers.utils.*;
import symbols.FunInfo;
import symbols.SymbolInfo;
import symbols.SymbolTable;
import symbols.SymbolType;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;
import types.TypeTable;

public class ExprASTPass {
    private final ParseErr err = ParseErr.getInst();
    private final TypeTable typeTable = TypeTable.getInst();
    private final OpTable opTable = OpTable.getInst();
    private SyntaxBuff syntaxBuff;
    private Scope scope;

    /**
     * Constructs an AST for an expression
     *
     * @param syntaxBuff a syntax buffer.
     * @param scope      the scope surrounding the expression.
     * @return a ParseResult object as the result of constructing the AST.
     */
    public ParseResult<ASTNode> doExpr(SyntaxBuff syntaxBuff, Scope scope) {
        this.syntaxBuff = syntaxBuff;
        this.scope = scope;
        return doInfixExpr(null);
    }

    /**
     * Constructs an AST node for a prefix or postfix operator.
     *
     * @return the constructed AST node.
     */
    private UnASTNode doPrefixPostfixOp() {
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok opTok = syntaxInfo.getTok();
        return new UnOpASTNode(opTok, null);
    }

    /**
     * Checks a data type and constructs an AST for a type conversion operation.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doTypeConv() {
        // Type conversion operator
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok typeConvTok = syntaxInfo.getTok();
        // Target type to be converted to
        syntaxInfo = syntaxBuff.forward();
        Tok dtypeTok = syntaxInfo.getTok();
        String dtypeId = dtypeTok.getVal();
        TypeInfo dtype = typeTable.getType(dtypeId);
        if (dtype == null) {
            return err.raise(new ErrMsg("Invalid data type '" + dtypeId + "'", dtypeTok));
        }
        TypeConvASTNode typeConvNode = new TypeConvASTNode(typeConvTok, dtype);
        return ParseResult.ok(typeConvNode);
    }

    /**
     * Checks an id and constructs an AST node for the id.
     *
     * @return a ParseResult object as the result of constructing the AST node.
     */
    private ParseResult<ASTNode> doId() {
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok idTok = syntaxInfo.getTok();
        String id = idTok.getVal();
        SymbolTable symbolTable = scope.getSymbolTable();
        SymbolInfo symbol = symbolTable.getSymbol(id);
        if (symbol == null) {
            return err.raise(new ErrMsg("Invalid id '" + id + "'", idTok));
        } else if (symbol.getSymbolType() == SymbolType.FUN) {
            return err.raise(new ErrMsg("Unexpected function '" + id + "'", idTok));
        }

        TypeInfo dtype = symbol.getDtype();
        ASTNode idNode = switch (symbol.getSymbolType()) {
            case VAR -> new VarIdASTNode(idTok, dtype);
            case CONST -> new ConstIdASTNode(idTok, dtype);
            default -> new ParamASTNode(idTok, dtype);
        };

        return ParseResult.ok(idNode);
    }

    /**
     * Constructs an AST node for a literal.
     *
     * @return a ParseResult object as the result of constructing the AST node.
     */
    private ParseResult<ASTNode> doLiteral() {
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok literalTok = syntaxInfo.getTok();
        TokType literalTokType = literalTok.getType();
        // Data type exists so no need to check
        TypeInfo dtype = typeTable.getType(literalTokType);
        ASTNode literalNode = new LiteralASTNode(literalTok, dtype);
        return ParseResult.ok(literalNode);
    }

    /**
     * Constructs an AST for an expression inside a pair of parentheses.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doParenGroup() {
        // '('
        syntaxBuff.forward();
        // Expression in between
        ParseResult<ASTNode> exprResult = doInfixExpr(null);
        // ')'
        syntaxBuff.forward();
        return ParseResult.ok(exprResult.getData());
    }

    /**
     * Constructs an AST for a function call.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doFunCall() {
        SyntaxInfo syntaxInfo = syntaxBuff.forward();
        Tok funIdTok = syntaxInfo.getTok();
        String funId = funIdTok.getVal();
        SymbolTable symbolTable = scope.getSymbolTable();
        FunInfo funInfo = (FunInfo) symbolTable.getSymbol(funId);
        if (funInfo == null) {
            return err.raise(new ErrMsg("Invalid function id '" + funId + "'", funIdTok));
        }

        FunCallASTNode funCallNode = new FunCallASTNode(funIdTok, funInfo.getDtype());
        int numArgs = funInfo.countParams();
        int i = 0;
        boolean firstArg = true;
        ParseResult<ASTNode> argResult;
        // '('
        syntaxBuff.forward();

        while (syntaxBuff.peek().getTag() != SyntaxTag.RPAREN) {
            if (!firstArg) {
                // ','
                syntaxBuff.forward();
            }
            // TODO: check the number of arguments
            argResult = doInfixExpr(null);
            if (argResult.getStatus() == ParseStatus.ERR) {
                return argResult;
            }
            funCallNode.addChild(argResult.getData());
            firstArg = false;
            ++i;
        }

        // Check the number of arguments
        if (i != numArgs) {
            return err.raise(new ErrMsg("Expected the number of arguments to be " + numArgs + " but got " + i +
                    " for function '" + funId + "'", funIdTok));
        }

        // ')'
        syntaxBuff.forward();
        return ParseResult.ok(funCallNode);
    }

    /**
     * Constructs an AST for a primary expression.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doPrimary() {
        Tok tok = syntaxBuff.peek().getTok();
        SyntaxTag tag = syntaxBuff.peek().getTag();
        return switch (tag) {
            case ID -> doId();
            case FUN_CALL -> doFunCall();
            case LITERAL -> doLiteral();
            case LPAREN -> doParenGroup();
            // This never occurs
            default -> ParseResult.fail(tok);
        };
    }

    /**
     * Constructs an AST for a postfix expression.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doPostfixExpr() {
        ParseResult<ASTNode> primaryResult = doPrimary();
        if (primaryResult.getStatus() == ParseStatus.ERR) {
            return primaryResult;
        }

        SyntaxInfo syntaxInfo = syntaxBuff.peek();
        SyntaxTag tag = syntaxInfo.getTag();
        if (tag != SyntaxTag.POSTFIX && tag != SyntaxTag.TYPE_CONV) {
            return primaryResult;
        }

        ASTNode root = null;
        ParseResult<ASTNode> postfixResult;
        UnASTNode postfixNode;
        boolean end = false;

        while (!end) {
            syntaxInfo = syntaxBuff.peek();
            tag = syntaxInfo.getTag();
            end = tag != SyntaxTag.POSTFIX && tag != SyntaxTag.TYPE_CONV;
            if (!end) {
                if (tag == SyntaxTag.TYPE_CONV) {
                    postfixResult = doTypeConv();
                    if (postfixResult.getStatus() == ParseStatus.ERR) {
                        return postfixResult;
                    }
                    postfixNode = (UnASTNode) postfixResult.getData();
                } else {
                    postfixNode = doPrefixPostfixOp();
                }
                postfixNode.setChild(root);
                root = postfixNode;
            }
        }

        return ParseResult.ok(root);
    }

    /**
     * Constructs an AST for a sequence of prefix operators.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<Pair<ASTNode, ASTNode>> doPrefixOpSeq() {
        SyntaxInfo syntaxInfo = syntaxBuff.peek();
        if (syntaxInfo.getTag() != SyntaxTag.PREFIX) {
            return ParseResult.fail(syntaxInfo.getTok());
        }

        UnASTNode root = null;
        UnASTNode parentNode = null;
        UnASTNode currNode;
        boolean end = false;

        while (!end) {
            syntaxInfo = syntaxBuff.peek();
            end = syntaxInfo.getTag() != SyntaxTag.PREFIX;
            if (!end) {
                currNode = doPrefixPostfixOp();
                if (parentNode == null) {
                    root = currNode;
                } else {
                    parentNode.setChild(currNode);
                }
                parentNode = currNode;
            }
        }

        return ParseResult.ok(new Pair<>(root, parentNode));
    }

    /**
     * Constructs an AST for a prefix expression.
     *
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doPrefixExpr() {
        ParseResult<Pair<ASTNode, ASTNode>> prefixResult = doPrefixOpSeq();
        ASTNode root = null;
        UnASTNode prefixLeaf = null;
        if (prefixResult.getStatus() == ParseStatus.OK) {
            root = prefixResult.getData().first();
            prefixLeaf = (UnASTNode) prefixResult.getData().second();
        }

        ParseResult<ASTNode> postfixResult = doPostfixExpr();
        if (postfixResult.getStatus() == ParseStatus.ERR) {
            return postfixResult;
        }
        if (root == null) {
            return postfixResult;
        }

        prefixLeaf.setChild(postfixResult.getData());
        return ParseResult.ok(root);
    }

    /**
     * Constructs an AST for an infix expression using Pratt's algorithm.
     *
     * @param prevOpTok the previous operator token.
     * @return a ParseResult object as the result of constructing the AST.
     */
    private ParseResult<ASTNode> doInfixExpr(Tok prevOpTok) {
        ParseResult<ASTNode> leftResult = doPrefixExpr();
        if (leftResult.getStatus() == ParseStatus.ERR) {
            return leftResult;
        }

        SyntaxInfo syntaxInfo;
        Tok opTok;
        ASTNodeType leftNodeType;
        ASTNode leftNode, rightNode;
        BinOpASTNode binNode;

        while (true) {
            syntaxInfo = syntaxBuff.peek();
            if (syntaxInfo.getTag() != SyntaxTag.INFIX) {
                return leftResult;
            }

            opTok = syntaxInfo.getTok();
            leftNode = leftResult.getData();
            leftNodeType = leftNode.getNodeType();

            if (opTok.getType() == TokType.ASSIGNMENT && leftNodeType != ASTNodeType.VAR_ID &&
                    leftNodeType != ASTNodeType.PARAM) {
                return err.raise(new ErrMsg("Expected a mutable id before assignment", opTok));
            }

            if (prevOpTok != null && opTable.cmpPreced(opTok.getType(), prevOpTok.getType()) < 0) {
                // The current operator has lower precedence than the previous operator
                return leftResult;
            }

            syntaxBuff.forward();
            binNode = new BinOpASTNode(opTok, null);
            ParseResult<ASTNode> rightResult = doInfixExpr(opTok);
            if (rightResult.getStatus() == ParseStatus.ERR) {
                return rightResult;
            }

            rightNode = rightResult.getData();
            binNode.setLeft(leftNode);
            binNode.setRight(rightNode);
            leftResult = ParseResult.ok(binNode);
        }
    }
}
