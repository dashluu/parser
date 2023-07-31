package parsers.function;

import ast.*;
import exceptions.ErrMsg;
import operators.OpTable;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;
import parsers.utils.TokParser;
import toks.Tok;
import toks.TokType;
import types.VoidType;

import java.io.IOException;

public class FunHeadParser {
    private TokParser tokParser;
    private FunHeadSemanChecker semanChecker;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser    a token parser.
     * @param semanChecker a semantics checker for function headers.
     */
    public void init(TokParser tokParser, FunHeadSemanChecker semanChecker) {
        this.tokParser = tokParser;
        this.semanChecker = semanChecker;
    }

    /**
     * Parses a function header.
     *
     * @param context the parsing context.
     * @return a ParseResult object as the result of parsing a function header.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseFunHead(ParseContext context) throws IOException {
        this.context = context;
        // Parse the function keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(TokType.FUN_DECL, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        if (context.getScope().getRetDtype() != null) {
            // Function is defined inside another function
            return context.raiseErr(new ErrMsg("A function definition cannot exist inside another function",
                    kwResult.getData()));
        }

        // Parse the function id
        ParseResult<Tok> idResult = tokParser.parseTok(TokType.ID, context);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing function's name", idResult.getFailTok()));
        }

        // Try parsing a parameter list
        ParseResult<ASTNode> paramListResult = parseParamList();
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        } else if (paramListResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid parameter list", paramListResult.getFailTok()));
        }

        // Try parsing a return type annotation
        // Failure indicates the function returns void
        TypeAnnASTNode typeAnnNode;
        ParseResult<ASTNode> typeAnnResult = parseTypeAnn();
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return typeAnnResult;
        } else if (typeAnnResult.getStatus() == ParseStatus.OK) {
            typeAnnNode = (TypeAnnASTNode) typeAnnResult.getData();
        } else {
            typeAnnNode = voidRetTypeAnnAST();
        }

        FunDefASTNode funDefNode = new FunDefASTNode(idResult.getData(), null);
        ParamListASTNode paramListNode = (ParamListASTNode) paramListResult.getData();
        funDefNode.setParamListNode(paramListNode);
        typeAnnNode.setLeft(funDefNode);
        return semanChecker.checkSeman(typeAnnNode, context);
    }

    /**
     * Parses a function parameter list.
     *
     * @return a ParseResult object as the result of parsing a function parameter list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseParamList() throws IOException {
        // Parse '('
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN, context);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        ParseResult<Tok> commaResult;
        ParseResult<ASTNode> paramResult;
        ParamListASTNode paramListNode = new ParamListASTNode();
        boolean end = false;
        // Boolean to indicate if this is the first parameter
        boolean firstArg = true;

        while (!end) {
            // Parse ')'
            parenResult = tokParser.parseTok(TokType.RPAREN, context);
            if (parenResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (!(end = parenResult.getStatus() == ParseStatus.OK)) {
                if (!firstArg) {
                    // If this is not the first parameter in the list, ',' must be present
                    commaResult = tokParser.parseTok(TokType.COMMA, context);
                    if (commaResult.getStatus() == ParseStatus.ERR) {
                        return ParseResult.err();
                    } else if (commaResult.getStatus() == ParseStatus.FAIL) {
                        return context.raiseErr(new ErrMsg("Missing ','", commaResult.getFailTok()));
                    }
                }

                paramResult = parseParam();
                if (paramResult.getStatus() == ParseStatus.ERR) {
                    return paramResult;
                } else if (paramResult.getStatus() == ParseStatus.FAIL) {
                    return context.raiseErr(new ErrMsg("Invalid parameter", paramResult.getFailTok()));
                }

                paramListNode.addChild(paramResult.getData());
                firstArg = false;
            }
        }

        return ParseResult.ok(paramListNode);
    }

    /**
     * Parses a function parameter in the form 'name:type'.
     *
     * @return a ParseResult object as the result of parsing a function parameter.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseParam() throws IOException {
        // Parse the parameter's name
        ParseResult<Tok> nameResult = tokParser.parseTok(TokType.ID, context);
        if (nameResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (nameResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(nameResult.getFailTok());
        }

        Tok nameTok = nameResult.getData();
        // Parse the type annotation
        ParseResult<ASTNode> typeAnnResult = parseTypeAnn();
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return typeAnnResult;
        } else if (typeAnnResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing a data type for parameter '" + nameTok.getVal() + "'",
                    typeAnnResult.getFailTok()));
        }

        TypeAnnASTNode typeAnnNode = (TypeAnnASTNode) typeAnnResult.getData();
        ParamDeclASTNode paramNode = new ParamDeclASTNode(nameTok, null);
        typeAnnNode.setLeft(paramNode);
        return ParseResult.ok(typeAnnNode);
    }

    /**
     * Parses a type annotation.
     *
     * @return a ParseResult object as the result of parsing a type annotation.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseTypeAnn() throws IOException {
        // Try parsing ':'
        ParseResult<Tok> typeAnnResult = tokParser.parseTok(TokType.COLON, context);
        if (typeAnnResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (typeAnnResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(typeAnnResult.getFailTok());
        }

        // Parse a data type
        ParseResult<Tok> dtypeResult = tokParser.parseTok(TokType.ID, context);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Expected a data type for type annotation",
                    dtypeResult.getFailTok()));
        }

        Tok typeAnnTok = typeAnnResult.getData();
        TypeAnnASTNode typeAnnNode = new TypeAnnASTNode(typeAnnTok, null);
        Tok dtypeTok = dtypeResult.getData();
        ASTNode dtypeNode = new DtypeASTNode(dtypeTok, null);
        typeAnnNode.setDtypeNode(dtypeNode);
        return ParseResult.ok(typeAnnNode);
    }

    /**
     * Constructs a partial AST for void return type annotation.
     *
     * @return an AST node as the root of the constructed AST.
     */
    private TypeAnnASTNode voidRetTypeAnnAST() {
        Tok typeAnnTok = new Tok(OpTable.COLON, TokType.COLON, null);
        TypeAnnASTNode typeAnnNode = new TypeAnnASTNode(typeAnnTok, null);
        Tok dtypeTok = new Tok(VoidType.ID, TokType.ID, null);
        ASTNode dtypeNode = new DtypeASTNode(dtypeTok, null);
        typeAnnNode.setDtypeNode(dtypeNode);
        return typeAnnNode;
    }
}
