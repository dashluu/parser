package parsers.expr;

import ast.ASTNode;
import exceptions.SyntaxErr;
import lexers.Lexer;
import parsers.utils.*;

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

            Lexer lexer = new Lexer(reader);
            TokParser tokParser = new TokParser();
            ExprSyntaxPass syntaxPass = new ExprSyntaxPass();
            ExprASTPass astPass = new ExprASTPass();
            ExprSemanChecker semanChecker = new ExprSemanChecker();
            ExprParser parser = new ExprParser();

            tokParser.init(lexer);
            syntaxPass.init(lexer, tokParser);
            parser.init(syntaxPass, astPass, semanChecker);

            Scope global = new Scope(null);
            ParseErr err = ParseErr.getInst();
            ParseResult<ASTNode> result = parser.parseExpr(global);
            if (err.hasErr()) {
                throw new SyntaxErr(err.getMsg());
            } else if (result.getStatus() == ParseStatus.OK) {
                ASTNode exprNode = result.getData();
                writer.write("{" + exprNode.toJsonStr() + "}");
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