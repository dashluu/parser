package parsers.fun_def;

import ast.ASTNode;
import ast.FunDefASTNode;
import ast.ParamDeclASTNode;
import ast.ParamListASTNode;
import exceptions.ErrMsg;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;
import types.TypeInfo;

import java.io.IOException;

public class FunHeadParser {
    private TokParser tokParser;

    /**
     * Initializes the dependencies.
     *
     * @param tokParser a token parser.
     */
    public void init(TokParser tokParser) {
        this.tokParser = tokParser;
    }

    /**
     * Parses a function header.
     *
     * @param funScope the scope surrounding the function header.
     * @return a ParseResult object as the result of parsing a function header.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseFunHead(Scope funScope) throws IOException {
        // Parse the function keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(TokType.FUN_DECL);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        if (funScope.getRetDtype() != null) {
            // Function is defined inside another function
            return ParseErr.raise(new ErrMsg("A function definition cannot exist inside another function",
                    kwResult.getData()));
        }

        // Parse the function id
        ParseResult<Tok> idResult = tokParser.parseTok(TokType.ID);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Missing function's name", idResult.getFailTok()));
        }

        // Try parsing a parameter list
        ParseResult<ASTNode> paramListResult = parseParamList();
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        } else if (paramListResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Invalid parameter list", paramListResult.getFailTok()));
        }

        // Try parsing a dummy return type
        // No need to check if it fails since that indicates the function returns void
        ParseResult<TypeInfo> retDtypeResult = parseTypeAnn();
        if (retDtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        }

        Tok idTok = idResult.getData();
        TypeInfo retDtype = retDtypeResult.getData();
        FunDefASTNode funDefNode = new FunDefASTNode(idTok, retDtype);
        ParamListASTNode paramListNode = (ParamListASTNode) paramListResult.getData();
        funDefNode.setParamListNode(paramListNode);
        return ParseResult.ok(funDefNode);
    }

    /**
     * Parses a function parameter list.
     *
     * @return a ParseResult object as the result of parsing a function parameter list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseParamList() throws IOException {
        // Parse '('
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN);
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
            parenResult = tokParser.parseTok(TokType.RPAREN);
            if (parenResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (!(end = parenResult.getStatus() == ParseStatus.OK)) {
                if (!firstArg) {
                    // If this is not the first parameter in the list, ',' must be present
                    commaResult = tokParser.parseTok(TokType.COMMA);
                    if (commaResult.getStatus() == ParseStatus.ERR) {
                        return ParseResult.err();
                    } else if (commaResult.getStatus() == ParseStatus.FAIL) {
                        return ParseErr.raise(new ErrMsg("Missing ','", commaResult.getFailTok()));
                    }
                }

                paramResult = parseParam();
                if (paramResult.getStatus() == ParseStatus.ERR) {
                    return ParseResult.err();
                } else if (paramResult.getStatus() == ParseStatus.FAIL) {
                    return ParseErr.raise(new ErrMsg("Invalid parameter", paramResult.getFailTok()));
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
        ParseResult<Tok> nameResult = tokParser.parseTok(TokType.ID);
        if (nameResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (nameResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(nameResult.getFailTok());
        }

        Tok nameTok = nameResult.getData();
        // Parse the type annotation
        ParseResult<TypeInfo> dtypeResult = parseTypeAnn();
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Missing a data type for parameter '" + nameTok.getVal() + "'",
                    dtypeResult.getFailTok()));
        }

        TypeInfo dtype = dtypeResult.getData();
        ParamDeclASTNode paramNode = new ParamDeclASTNode(nameTok, dtype);
        return ParseResult.ok(paramNode);
    }

    /**
     * Parses a type annotation.
     *
     * @return a ParseResult object as the result of parsing a type annotation.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<TypeInfo> parseTypeAnn() throws IOException {
        // Try parsing ':'
        ParseResult<Tok> result = tokParser.parseTok(TokType.COLON);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        // Parse a data type
        result = tokParser.parseTok(TokType.ID);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseErr.raise(new ErrMsg("Expected a type id for type annotation", result.getFailTok()));
        }

        // Create a dummy data type so we can check it in later phase
        String dtypeId = result.getData().getVal();
        TypeInfo dtype = new TypeInfo(dtypeId, -1);
        return ParseResult.ok(dtype);
    }
}
