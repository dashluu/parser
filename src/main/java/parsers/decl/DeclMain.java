package parsers.decl;

import ast.ASTNode;
import exceptions.SyntaxErr;
import lexers.Lexer;
import parsers.expr.ExprASTPass;
import parsers.expr.ExprSemanChecker;
import parsers.expr.ExprSyntaxPass;
import parsers.utils.*;

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
            ExprSyntaxPass exprSyntaxPass = new ExprSyntaxPass();
            ExprASTPass exprASTPass = new ExprASTPass();
            ExprSemanChecker exprSemanChecker = new ExprSemanChecker();
            DeclSyntaxPass declSyntaxPass = new DeclSyntaxPass();
            DeclASTPass declASTPass = new DeclASTPass();
            DeclSemanChecker declSemanChecker = new DeclSemanChecker();
            DeclParser declParser = new DeclParser();

            tokParser.init(lexer);
            exprSyntaxPass.init(lexer, tokParser);
            declSyntaxPass.init(tokParser, exprSyntaxPass);
            declASTPass.init(exprASTPass);
            declSemanChecker.init(exprSemanChecker);
            declParser.init(declSyntaxPass, declASTPass, declSemanChecker);

            Scope global = new Scope(null);
            ParseResult<ASTNode> result = declParser.parseDecl(global);
            if (result.getStatus() == ParseStatus.ERR) {
                ParseErr err = ParseErr.getInst();
                throw new SyntaxErr(err.getMsg());
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
