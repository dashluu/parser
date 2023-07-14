package parsers.fun_def;

import exceptions.ErrMsg;
import parsers.utils.*;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class FunHeadSyntaxPass {
    private SyntaxBuff syntaxBuff;
    private TokParser tokParser;
    private static final ParseErr err = ParseErr.getInst();

    /**
     * Initializes the dependencies.
     *
     * @param tokParser a parser that consumes valid tokens.
     */
    public void init(TokParser tokParser) {
        this.tokParser = tokParser;
    }

    /**
     * Attempts to consume and check the syntax of a function header.
     *
     * @param syntaxBuff a buffer containing syntax information.
     * @param scope      the scope surrounding the function header.
     * @return a ParseResult object as the result of consuming a function header.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<SyntaxInfo> eatFunHead(SyntaxBuff syntaxBuff, Scope scope) throws IOException {
        this.syntaxBuff = syntaxBuff;
        // Consume the function keyword
        ParseResult<Tok> kwResult = tokParser.parseTok(TokType.FUN_DECL);
        if (kwResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (kwResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(kwResult.getFailTok());
        }

        if (scope.getRetType() != null) {
            // Function is defined inside another function
            return err.raise(new ErrMsg("A function definition cannot exist inside another function",
                    kwResult.getData()));
        }

        // Consume the function id
        ParseResult<Tok> idResult = tokParser.parseTok(TokType.ID);
        if (idResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (idResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Missing function's name", idResult.getFailTok()));
        }

        syntaxBuff.add(new SyntaxInfo(idResult.getData(), SyntaxTag.FUN_DEF));
        // Try consuming a parameter list
        ParseResult<SyntaxInfo> paramListResult = eatParamList();
        if (paramListResult.getStatus() == ParseStatus.ERR) {
            return paramListResult;
        } else if (paramListResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Invalid parameter list", paramListResult.getFailTok()));
        }

        // Try consuming the return type
        // No need to check if it fails since that indicates the function returns void
        ParseResult<SyntaxInfo> retTypeResult = eatTypeAnn();
        if (retTypeResult.getStatus() == ParseStatus.ERR) {
            return retTypeResult;
        }

        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume and check the syntax of a function parameter list.
     *
     * @return a ParseResult object as the result of consuming a function parameter list.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatParamList() throws IOException {
        // Consume '('
        ParseResult<Tok> parenResult = tokParser.parseTok(TokType.LPAREN);
        if (parenResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (parenResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(parenResult.getFailTok());
        }

        syntaxBuff.add(new SyntaxInfo(parenResult.getData(), SyntaxTag.LPAREN));
        ParseResult<Tok> commaResult;
        ParseResult<SyntaxInfo> paramResult;
        boolean end = false;
        // Boolean to indicate if this is the first parameter
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
                    // If this is not the first parameter in the list, ',' must be present
                    commaResult = tokParser.parseTok(TokType.COMMA);
                    if (commaResult.getStatus() == ParseStatus.ERR) {
                        return ParseResult.err();
                    } else if (commaResult.getStatus() == ParseStatus.FAIL) {
                        return err.raise(new ErrMsg("Missing ','", commaResult.getFailTok()));
                    }
                    syntaxBuff.add(new SyntaxInfo(commaResult.getData(), SyntaxTag.COMMA));
                }

                paramResult = eatParam();
                if (paramResult.getStatus() == ParseStatus.ERR) {
                    return paramResult;
                } else if (paramResult.getStatus() == ParseStatus.FAIL) {
                    return err.raise(new ErrMsg("Invalid parameter", paramResult.getFailTok()));
                }

                firstArg = false;
            }
        }

        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume and check the syntax of a function parameter.
     *
     * @return a ParseResult object as the result of consuming a function parameter.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatParam() throws IOException {
        // Parameter is in the form 'name:type'
        ParseResult<Tok> nameResult = tokParser.parseTok(TokType.ID);
        if (nameResult.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (nameResult.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(nameResult.getFailTok());
        }

        Tok nameTok = nameResult.getData();
        syntaxBuff.add(new SyntaxInfo(nameTok, SyntaxTag.PARAM));
        // Consume the data type
        ParseResult<SyntaxInfo> dtypeResult = eatTypeAnn();
        if (dtypeResult.getStatus() == ParseStatus.ERR) {
            return dtypeResult;
        } else if (dtypeResult.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Missing data type for parameter '" + nameTok.getVal() + "'",
                    dtypeResult.getFailTok()));
        }

        return ParseResult.ok(null);
    }

    /**
     * Attempts to consume and check the syntax of a type annotation.
     *
     * @return a ParseResult object as the result of checking the syntax of type annotation.
     * @throws IOException if there is an IO exception.
     */
    private ParseResult<SyntaxInfo> eatTypeAnn() throws IOException {
        // Try consuming ':'
        ParseResult<Tok> result = tokParser.parseTok(TokType.COLON);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return ParseResult.fail(result.getFailTok());
        }

        // Consume data type
        // No need to add colon to the syntax buffer
        result = tokParser.parseTok(TokType.ID);
        if (result.getStatus() == ParseStatus.ERR) {
            return ParseResult.err();
        } else if (result.getStatus() == ParseStatus.FAIL) {
            return err.raise(new ErrMsg("Expected a type id for type annotation", result.getFailTok()));
        }

        syntaxBuff.add(new SyntaxInfo(result.getData(), SyntaxTag.TYPE_ID));
        return ParseResult.ok(null);
    }
}
