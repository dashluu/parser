package parsers.decl;

import ast.ASTNode;
import exceptions.SyntaxErr;
import lexers.Lexer;
import parsers.expr.ExprParser;
import parsers.expr.ExprSemanChecker;
import parsers.utils.*;
import parsers.utils.ParseContext;
import parsers.utils.Scope;
import parsers.utils.ScopeStack;

import java.io.*;

public class DeclMain {
    public static void main(String[] args) {
        String inFilename = args[0];
        String outFilename = args[1];
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(inFilename));
            writer = new BufferedWriter(new FileWriter(outFilename));

            Lexer lexer = new Lexer(reader);
            TokParser tokParser = new TokParser();
            ExprSemanChecker exprSemanChecker = new ExprSemanChecker();
            ExprParser exprParser = new ExprParser();
            DeclSemanChecker declSemanChecker = new DeclSemanChecker();
            DeclParser declParser = new DeclParser();

            tokParser.init(lexer);
            exprParser.init(lexer, tokParser, exprSemanChecker);
            declParser.init(tokParser, exprParser, declSemanChecker);

            ParseContext context = ParseContext.createContext();
            Scope globalScope = new Scope(null);
            ScopeStack scopeStack = context.getScopeStack();
            scopeStack.push(globalScope);
            ParseResult<ASTNode> result = declParser.parseDecl(context);
            scopeStack.pop();
            if (context.hasErr()) {
                throw new SyntaxErr(context.getErrMsg());
            } else if (result.getStatus() == ParseStatus.OK) {
                ASTNode declNode = result.getData();
                writer.write("{" + declNode.toJsonStr() + "}");
            }

            reader.close();
            writer.close();
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
