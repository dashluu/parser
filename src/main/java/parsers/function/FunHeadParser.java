package parsers.function;

import ast.*;
import exceptions.ErrMsg;
import parsers.dtype.DtypeParser;
import parsers.utils.*;
import toks.SrcPos;
import toks.SrcRange;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class FunHeadParser {
    private TokMatcher tokMatcher;
    private DtypeParser dtypeParser;
    private FunHeadSemanChecker semanChecker;
    private ParseContext context;

    /**
     * Initializes the dependencies.
     *
     * @param tokMatcher   a token matcher.
     * @param dtypeParser  a data type parser.
     * @param semanChecker a semantic checker for the function header.
     */
    public void init(TokMatcher tokMatcher, DtypeParser dtypeParser, FunHeadSemanChecker semanChecker) {
        this.tokMatcher = tokMatcher;
        this.dtypeParser = dtypeParser;
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
        ParseResult<Tok> kwResult = tokMatcher.parseTok(TokType.FUN_DECL, context);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        if (context.getScope().isInFun() != null) {
            // Function is defined inside another function
            return context.raiseErr(new ErrMsg("A function definition cannot exist inside another function",
                    kwResult.getData()));
        }

        // Parse the function id
        ParseResult<Tok> idResult = tokMatcher.parseTok(TokType.ID, context);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing a function identifier", idResult.getFailTok()));
        }

        // Parse the function signature
        ParseResult<ASTNode> funSignResult = parseFunSign();
        if (funSignResult.getStatus() == ParseStatus.ERR) {
            return funSignResult;
        } else if (funSignResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing a function signature", funSignResult.getFailTok()));
        }

        FunDefASTNode funDefNode = new FunDefASTNode(kwResult.getData(), null);
        IdASTNode funIdNode = new IdASTNode(idResult.getData(), null, false);
        FunSignASTNode funSignNode = (FunSignASTNode) funSignResult.getData();
        funDefNode.setIdNode(funIdNode);
        funDefNode.setSignNode(funSignNode);
        return semanChecker.checkSeman(funDefNode, context);
    }

    /**
     * Parses a function signature.
     *
     * @return a ParseResult object as the result of parsing the function signature.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseFunSign() throws IOException {
        FunSignASTNode funSignNode = new FunSignASTNode(null);

        // Parse a parameter list
        ParseResult<ASTNode> paramListResult = parseParamList();
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        } else if (paramListResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Invalid parameter list", paramListResult.getFailTok()));
        }

        ParamListASTNode paramListNode = (ParamListASTNode) paramListResult.getData();
        funSignNode.setParamListNode(paramListNode);

        // Parse return type annotation
        ParseResult<ASTNode> retDtypeResult = dtypeParser.parseTypeAnn(context);
        if (retDtypeResult.getStatus() == ParseStatus.ERR) {
            return retDtypeResult;
        } else if (retDtypeResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.ok(funSignNode);
        }

        DtypeASTNode retDtypeNode = (DtypeASTNode) retDtypeResult.getData();
        funSignNode.setRetDtypeNode(retDtypeNode);
        return ParseResult.ok(funSignNode);
    }

    /**
     * Parses a function parameter list.
     *
     * @return a ParseResult object as the result of parsing a function parameter list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<ASTNode> parseParamList() throws IOException {
        // Parse '('
        ParseResult<Tok> parenResult = tokMatcher.parseTok(TokType.LPAREN, context);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        Tok parenTok = parenResult.getData();
        SrcPos paramListStartPos = parenTok.getSrcRange().getStartPos();
        ParamListASTNode paramListNode = new ParamListASTNode();
        ParseResult<Tok> commaResult;
        ParseResult<ASTNode> paramResult;
        boolean end = false;
        // Boolean to indicate if this is the first parameter
        boolean firstArg = true;

        while (!end) {
            // Parse ')'
            parenResult = tokMatcher.parseTok(TokType.RPAREN, context);
            if (parenResult.getStatus() == ParseStatus.ERR) {
                return ParseResult.err();
            } else if (!(end = parenResult.getStatus() == ParseStatus.OK)) {
                if (!firstArg) {
                    // If this is not the first parameter in the list, ',' must be present
                    commaResult = tokMatcher.parseTok(TokType.COMMA, context);
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

        parenTok = parenResult.getData();
        SrcPos paramListEndPos = parenTok.getSrcRange().getEndPos();
        SrcRange paramListRange = new SrcRange(paramListStartPos, paramListEndPos);
        paramListNode.setSrcRange(paramListRange);
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
        ParseResult<Tok> nameResult = tokMatcher.parseTok(TokType.ID, context);
        if (nameResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (nameResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(nameResult.getFailTok());
        }

        Tok nameTok = nameResult.getData();
        IdASTNode nameNode = new IdASTNode(nameTok, null, false);
        // Parse the data type with type annotation
        ParseResult<ASTNode> dtypeResult = dtypeParser.parseTypeAnn(context);
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return dtypeResult;
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return context.raiseErr(new ErrMsg("Missing a data type for parameter '" + nameTok.getVal() + "'",
                    dtypeResult.getFailTok()));
        }

        DtypeASTNode dtypeNode = (DtypeASTNode) dtypeResult.getData();
        ParamDeclASTNode paramDeclNode = new ParamDeclASTNode(null);
        paramDeclNode.setIdNode(nameNode);
        paramDeclNode.setDtypeNode(dtypeNode);
        return ParseResult.ok(paramDeclNode);
    }
}
