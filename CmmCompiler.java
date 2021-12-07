package main;

import main.visitor.name.ASTTreePrinter;
import main.visitor.name.ErrorPrinter;
import parsers.*;
import main.ast.nodes.Program;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class CmmCompiler {
    public void compile(CharStream textStream) {
        CmmLexer cmmLexer = new CmmLexer(textStream);
        CommonTokenStream tokenStream = new CommonTokenStream(cmmLexer);
        CmmParser cmmParser = new CmmParser(tokenStream);

        Program program = cmmParser.cmm().cmmProgram;
        ErrorPrinter errorPrinter = new ErrorPrinter();
        program.accept(errorPrinter);
        if (errorPrinter.getNumberOfErrors() == 0){
            ASTTreePrinter astTreePrinter = new ASTTreePrinter();
            program.accept(astTreePrinter);
        }
    }
}
