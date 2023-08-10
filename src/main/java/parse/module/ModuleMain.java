package parse.module;

import ast.ASTNode;
import ast.JSONWalker;
import exceptions.SyntaxErr;
import lex.LexReader;
import parse.scope.Scope;
import parse.scope.ScopeStack;
import parse.scope.ScopeType;
import parse.utils.*;

import java.io.*;

public class ModuleMain {
    public static void main(String[] args) {
        String inFilename = args[0];
        String outFilename = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inFilename));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename))) {
            LexReader lexReader = new LexReader(reader);
            ModuleParser moduleParser = new ModuleParser(lexReader);

            moduleParser.init();

            ParseContext context = ParseContext.createContext();
            Scope globalScope = new Scope(ScopeType.MODULE, null);
            ScopeStack scopeStack = context.getScopeStack();
            scopeStack.push(globalScope);
            ParseResult<ASTNode> result = moduleParser.parseModule(context);
            scopeStack.pop();
            if (context.hasErr()) {
                throw new SyntaxErr(context.getErrMsg());
            } else if (result.getStatus() == ParseStatus.OK) {
                ASTNode moduleNode = result.getData();
                JSONWalker walker = new JSONWalker();
                writer.write(walker.walk(moduleNode));
            }
        } catch (SyntaxErr | IOException e) {
            e.printStackTrace();
        }
    }
}
