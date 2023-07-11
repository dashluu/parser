package parsers.module;

import ast.ASTNode;
import ast.ScopeASTNode;
import exceptions.SyntaxErr;
import lexers.Lexer;
import parsers.utils.ParseErr;
import parsers.utils.ParseResult;
import parsers.utils.ParseStatus;

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
            ParseResult<ScopeASTNode> result = moduleParser.parseModule();
            if (result.getStatus() == ParseStatus.ERR) {
                ParseErr err = ParseErr.getInst();
                throw new SyntaxErr(err.getMsg());
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
