package parse.expr;

import ast.ASTNode;
import ast.JSONWalker;
import exceptions.SyntaxErr;
import lex.LexReader;
import lex.Lexer;
import parse.dtype.DtypeParser;
import parse.scope.ScopeType;
import parse.utils.*;
import parse.utils.ParseContext;
import parse.scope.Scope;
import parse.scope.ScopeStack;

import java.io.*;

public class ExprMain {
    public static void main(String[] args) {
        String inFilename = args[0];
        String outFilename = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inFilename));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename))) {
            LexReader lexReader = new LexReader(reader);
            Lexer lexer = new Lexer(lexReader);
            TokMatcher tokMatcher = new TokMatcher();
            DtypeParser dtypeParser = new DtypeParser();
            ExprSemanChecker semanChecker = new ExprSemanChecker();
            ExprParser parser = new ExprParser();

            tokMatcher.init(lexer);
            parser.init(lexer, tokMatcher, dtypeParser, semanChecker);

            ParseContext context = ParseContext.createContext();
            Scope globalScope = new Scope(ScopeType.MODULE, null);
            ScopeStack scopeStack = context.getScopeStack();
            scopeStack.push(globalScope);
            ParseResult<ASTNode> result = parser.parseExpr(context);
            scopeStack.pop();
            if (context.hasErr()) {
                throw new SyntaxErr(context.getErrMsg());
            } else if (result.getStatus() == ParseStatus.OK) {
                ASTNode exprNode = result.getData();
                JSONWalker walker = new JSONWalker();
                writer.write(walker.walk(exprNode));
            }
        } catch (SyntaxErr | IOException e) {
            e.printStackTrace();
        }
    }
}