package parsers.utils;

import ast.ASTNode;
import ast.DtypeASTNode;
import ast.TypeAnnASTNode;
import exceptions.ErrMsg;
import toks.Tok;
import toks.TokType;

import java.io.IOException;

public class TypeAnnParser {
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
     * Parses a type annotation.
     *
     * @return a ParseResult object as the result of parsing a type annotation.
     * @throws IOException if there is an IO exception.
     */
    public ParseResult<ASTNode> parseTypeAnn(ParseContext context) throws IOException {
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
}
