package main.visitor.name;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.compileError.nameError.DuplicateFunction;
import main.compileError.nameError.DuplicateStruct;
import main.symbolTable.*;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.visitor.*;

public class ErrorPrinter extends Visitor<Void> {
    public void messagePrinter(int line, String message){
        System.out.println("Line " + line + ": " + message);
    }
    private int numberOfErrors;
    public int getNumberOfErrors() { return numberOfErrors; }
    public ErrorPrinter() {
        numberOfErrors = 0;
    }

    @Override
    public Void visit(Program program) {
        SymbolTable programST = new SymbolTable();
        SymbolTable.push(programST);
        SymbolTable.root = programST;
        for (StructDeclaration structDeclaration : program.getStructs()) {
            // SymbolTable structSymbolTable = new SymbolTable(SymbolTable.top);
            // SymbolTable.push(structSymbolTable);
            // structDeclaration.accept(this);
            StructSymbolTableItem structSymbolTableItem = new StructSymbolTableItem(structDeclaration);
            structSymbolTableItem.setStructSymbolTable(SymbolTable.top);
            SymbolTable.pop();
            try {
                programST.put(structSymbolTableItem);//todo try catch
            }
            catch (ItemAlreadyExistsException ex) {
                DuplicateStruct duplicateStruct;
                duplicateStruct = new DuplicateStruct(
                        structDeclaration.getLine(), structSymbolTableItem.getName());
                System.out.println(duplicateStruct.getMessage());
            }
        }
        for (FunctionDeclaration functionDeclaration : program.getFunctions()) {
            // SymbolTable functionSymbolTable = new SymbolTable(SymbolTable.top);
            // SymbolTable.push(functionSymbolTable);
            // FunctionSymbolTableItem functionSymbolTableItem = new FunctionSymbolTableItem(functionDeclaration);
            // functionSymbolTableItem.setFunctionSymbolTable(SymbolTable.top);
            // SymbolTable.pop();
            try {
                programST.put(functionSymbolTableItem);//todo try catch
            }
            catch (ItemAlreadyExistsException ex) {
                DuplicateFunction duplicateFunction;
                duplicateFunction = new DuplicateFunction(
                        functionDeclaration.getLine(), functionSymbolTableItem.getName());
                System.out.println(duplicateFunction.getMessage());
            }
            functionDeclaration.accept(this);
        }
        MainDeclaration mainDeclaration = program.getMain();
        SymbolTable mainSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(mainSymbolTable);
        mainDeclaration.accept(this);
        SymbolTable.pop();
        SymbolTable.pop(); // pop program
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
        SymbolTable mainSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(mainSymbolTable);
        mainDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDec) {
        VariableSymbolTableItem variableSymbolTableItem;
        variableSymbolTableItem = new VariableSymbolTableItem(variableDec.getVarName());
        try {
            SymbolTable.top.put(variableSymbolTableItem);
        }
        catch (ItemAlreadyExistsException ex) {
            DuplicateVar duplicateVar;
            duplicateVar = new DuplicateVar(variableDec.getLine(), variableSymbolTableItem.getName());
            System.out.println(duplicateVar.getMessage());
        }
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        SymbolTable structSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(structSymbolTable);
        structDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        SymbolTable setGetSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(setGetSymbolTable);
        for (VariableDeclaration variableDeclaration : setGetVarDec.getArgs())
            variableDeclaration.accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(AssignmentStmt assignmentStmt) {
        return null;
    }

    @Override
    public Void visit(BlockStmt blockStmt) {
        for (Statement statement: blockStmt.getStatements())
            statement.accept(this);
        return null;
    }

    @Override
    public Void visit(ConditionalStmt conditionalStmt) {
        SymbolTable thenBodySymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(thenBodySymbolTable);
        conditionalStmt.getThenBody().accept(this);
        SymbolTable.pop();

        if (conditionalStmt.getElseBody() != null) {
            SymbolTable elseBodySymbolTable = new SymbolTable(SymbolTable.top);
            SymbolTable.push(elseBodySymbolTable);
            conditionalStmt.getElseBody().accept(this);
            SymbolTable.pop();
        }
        return null;
    }

    @Override
    public Void visit(FunctionCallStmt functionCallStmt) {
        return null;
    }

    @Override
    public Void visit(DisplayStmt displayStmt) {
        return null;
    }

    @Override
    public Void visit(ReturnStmt returnStmt) {
        return null;
    }

    @Override
    public Void visit(LoopStmt loopStmt) {
        SymbolTable loopStmtSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(loopStmtSymbolTable);
        loopStmt.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDecStmt) {
        for (VariableDeclaration variableDeclaration: varDecStmt.getVars())
            variableDeclaration.accept(this);
        return null;
    }

    @Override
    public Void visit(ListAppendStmt listAppendStmt) {
        return null;
    }

    @Override
    public Void visit(ListSizeStmt listSizeStmt) {
        return null;
    }

    // @Override
    // public Void visit(BinaryExpression binaryExpression) {
    //     //todo
    //     messagePrinter(binaryExpression.getLine(), binaryExpression.toString());
    //     binaryExpression.getFirstOperand().accept(this);
    //     binaryExpression.getSecondOperand().accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(UnaryExpression unaryExpression) {
    //     //todo
    //     messagePrinter(unaryExpression.getLine(), unaryExpression.toString());
    //     unaryExpression.getOperand().accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(FunctionCall funcCall) {
    //     //todo
    //     messagePrinter(funcCall.getLine(), funcCall.toString());
    //     funcCall.getInstance().accept(this);
    //     for (Expression expression : funcCall.getArgs())
    //         expression.accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(Identifier identifier) {
    //     //todo
    //     messagePrinter(identifier.getLine(), identifier.toString());
    //     return null;
    // }

    // @Override
    // public Void visit(ListAccessByIndex listAccessByIndex) {
    //     //todo
    //     messagePrinter(listAccessByIndex.getLine(), listAccessByIndex.toString());
    //     listAccessByIndex.getInstance().accept(this);
    //     listAccessByIndex.getIndex().accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(StructAccess structAccess) {
    //     //todo
    //     messagePrinter(structAccess.getLine(), structAccess.toString());
    //     structAccess.getInstance().accept(this);
    //     structAccess.getElement().accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(ListSize listSize) {
    //     //todo
    //     messagePrinter(listSize.getLine(), listSize.toString());
    //     listSize.getArg().accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(ListAppend listAppend) {
    //     //todo
    //     messagePrinter(listAppend.getLine(), listAppend.toString());
    //     listAppend.getListArg().accept(this);
    //     listAppend.getElementArg().accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(ExprInPar exprInPar) {
    //     //todo
    //     messagePrinter(exprInPar.getLine(), exprInPar.toString());
    //     for (Expression expression : exprInPar.getInputs())
    //         expression.accept(this);
    //     return null;
    // }

    // @Override
    // public Void visit(IntValue intValue) {
    //     //todo
    //     messagePrinter(intValue.getLine(), intValue.toString());
    //     return null;
    // }

    // @Override
    // public Void visit(BoolValue boolValue) {
    //     //todo
    //     messagePrinter(boolValue.getLine(), boolValue.toString());
    //     return null;
    // }
}
