package main.visitor.name;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.visitor.*;

public class ASTTreePrinter extends Visitor<Void> {
    public void messagePrinter(int line, String message){
        System.out.println("Line " + line + ": " + message);
    }

    @Override
    public Void visit(Program program) {
        messagePrinter(program.getLine(), program.toString());
        for (StructDeclaration structDeclaration: program.getStructs())
            structDeclaration.accept(this);
        for (FunctionDeclaration functionDeclaration:program.getFunctions())
            functionDeclaration.accept(this);
        program.getMain().accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDec) {
        messagePrinter(functionDec.getLine(), functionDec.toString());
        functionDec.getFunctionName().accept(this);
        for (VariableDeclaration variableDeclaration : functionDec.getArgs())
            variableDeclaration.accept(this);
        functionDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDec) {
        messagePrinter(mainDec.getLine(), mainDec.toString());
        mainDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        //todo
        messagePrinter(variableDec.getLine(), variableDec.toString());
        variableDec.getVarName().accept(this);
        if (variableDec.getDefaultValue() != null)
            variableDec.getDefaultValue().accept(this);
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        //todo
        messagePrinter(structDec.getLine(), structDec.toString());
        structDec.getStructName().accept(this);
        structDec.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        //todo
        messagePrinter(setGetVarDec.getLine(), setGetVarDec.toString());
        setGetVarDec.getVarName().accept(this);
        for (VariableDeclaration variableDeclaration : setGetVarDec.getArgs())
            variableDeclaration.accept(this);
        setGetVarDec.getSetterBody().accept(this);
        setGetVarDec.getGetterBody().accept(this);
        return null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        //todo
        messagePrinter(assignmentStmt.getLine(), assignmentStmt.toString());
        assignmentStmt.getLValue().accept(this);
        assignmentStmt.getRValue().accept(this);
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        //todo
        messagePrinter(blockStmt.getLine(), blockStmt.toString());
        for (Statement statement: blockStmt.getStatements())
            statement.accept(this);
            return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        //todo
        messagePrinter(conditionalStmt.getLine(), conditionalStmt.toString());
        conditionalStmt.getCondition().accept(this);
        conditionalStmt.getThenBody().accept(this);
        if (conditionalStmt.getElseBody() != null)
            conditionalStmt.getElseBody().accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        //todo
        messagePrinter(functionCallStmt.getLine(), functionCallStmt.toString());
        functionCallStmt.getFunctionCall().accept(this);
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        //todo
        messagePrinter(displayStmt.getLine(), displayStmt.toString());
        displayStmt.getArg().accept(this);
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        //todo
        messagePrinter(returnStmt.getLine(), returnStmt.toString());
        returnStmt.getReturnedExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        //todo
        messagePrinter(loopStmt.getLine(), loopStmt.toString());
        loopStmt.getCondition().accept(this);
        loopStmt.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        //todo
        messagePrinter(varDecStmt.getLine(), varDecStmt.toString());
        for (VariableDeclaration variableDeclaration: varDecStmt.getVars())
            variableDeclaration.accept(this);
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        //todo
        messagePrinter(listAppendStmt.getLine(), listAppendStmt.toString());
        listAppendStmt.getListAppendExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        //todo
        messagePrinter(listSizeStmt.getLine(), listSizeStmt.toString());
        listSizeStmt.getListSizeExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(BinaryExpression binaryExpression) {
        //todo
        messagePrinter(binaryExpression.getLine(), binaryExpression.toString());
        binaryExpression.getFirstOperand().accept(this);
        binaryExpression.getSecondOperand().accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryExpression unaryExpression) {
        //todo
        messagePrinter(unaryExpression.getLine(), unaryExpression.toString());
        unaryExpression.getOperand().accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionCall funcCall) {
        //todo
        messagePrinter(funcCall.getLine(), funcCall.toString());
        funcCall.getInstance().accept(this);
        for (Expression expression : funcCall.getArgs())
            expression.accept(this);
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
        //todo
        messagePrinter(identifier.getLine(), identifier.toString());
        return null;
    }

    @Override
    public Void visit(ListAccessByIndex listAccessByIndex) {
        //todo
        messagePrinter(listAccessByIndex.getLine(), listAccessByIndex.toString());
        listAccessByIndex.getInstance().accept(this);
        listAccessByIndex.getIndex().accept(this);
        return null;
    }

    @Override
    public Void visit(StructAccess structAccess) {
        //todo
        messagePrinter(structAccess.getLine(), structAccess.toString());
        structAccess.getInstance().accept(this);
        structAccess.getElement().accept(this);
        return null;
    }

    @Override
    public Void visit(ListSize listSize) {
        //todo
        messagePrinter(listSize.getLine(), listSize.toString());
        listSize.getArg().accept(this);
        return null;
    }

    @Override
    public Void visit(ListAppend listAppend) {
        //todo
        messagePrinter(listAppend.getLine(), listAppend.toString());
        listAppend.getListArg().accept(this);
        listAppend.getElementArg().accept(this);
        return null;
    }

    @Override
    public Void visit(ExprInPar exprInPar) {
        //todo
        messagePrinter(exprInPar.getLine(), exprInPar.toString());
        for (Expression expression : exprInPar.getInputs())
            expression.accept(this);
        return null;
    }

    @Override
    public Void visit(IntValue intValue) {
        //todo
        messagePrinter(intValue.getLine(), intValue.toString());
        return null;
    }

    @Override
    public Void visit(BoolValue boolValue) {
        //todo
        messagePrinter(boolValue.getLine(), boolValue.toString());
        return null;
    }
}
