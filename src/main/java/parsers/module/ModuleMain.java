package parsers.module;

import ast.ASTNode;
import exceptions.SyntaxErr;
import lexers.Lexer;
import parsers.parse_utils.ParseErr;
import parsers.parse_utils.ParseResult;
import parsers.parse_utils.ParseStatus;
import utils.ParseContext;
import utils.Scope;
import utils.ScopeStack;

import java.io.*;

public class ModuleMain {
    public static void main(String[] args) {
        String inFilename = args[0];
        String outFilename = args[1];
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(inFilename));
            writer = new BufferedWriter(new FileWriter(outFilename));

            Lexer lexer = new Lexer(reader);
            ModuleParser moduleParser = new ModuleParser(lexer);

            moduleParser.init();

            ParseContext context = ParseContext.createContext();
            Scope globalScope = new Scope(null);
            ScopeStack scopeStack = context.getScopeStack();
            scopeStack.push(globalScope);
            ParseResult<ASTNode> result = moduleParser.parseModule(context);
            scopeStack.pop();
            if (ParseErr.hasErr()) {
                throw new SyntaxErr(ParseErr.getMsg());
            } else if (result.getStatus() == ParseStatus.OK) {
                ASTNode moduleNode = result.getData();
                writer.write("{" + moduleNode.toJsonStr() + "}");
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
