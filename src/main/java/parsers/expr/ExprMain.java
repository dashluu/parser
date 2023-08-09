package parsers.expr;

import ast.ASTNode;
import ast.JSONWalker;
import exceptions.SyntaxErr;
import lexers.LexReader;
import lexers.Lexer;
import parsers.scope.ScopeType;
import parsers.utils.*;
import parsers.utils.ParseContext;
import parsers.scope.Scope;
import parsers.scope.ScopeStack;

import java.io.*;

public class ExprMain {
    public static void main(String[] args) {
        String inFilename = args[0];
        String outFilename = args[1];
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(inFilename));
            writer = new BufferedWriter(new FileWriter(outFilename));

            LexReader lexReader = new LexReader(reader);
            Lexer lexer = new Lexer(lexReader);
            TokMatcher tokMatcher = new TokMatcher();
            ExprSemanChecker semanChecker = new ExprSemanChecker();
            ExprParser parser = new ExprParser();

            tokMatcher.init(lexer);
            parser.init(lexer, tokMatcher, semanChecker);

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
                writer.write(walker.getJSON(exprNode));
            }
        } catch (SyntaxErr | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}