package main.visitor.name;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.compileError.nameError.*;
import main.symbolTable.*;
import main.symbolTable.exceptions.ItemAlreadyExistsException;
import main.symbolTable.exceptions.ItemNotFoundException;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.symbolTable.items.VariableSymbolTableItem;
import main.visitor.*;
import main.visitor.name.Graph;

public class ErrorPrinter extends Visitor<Void> {
    private int numberOfErrors;
    private int lastID;
    private Graph<String> structsGraph;
    private String currentScope;

    public ErrorPrinter() {
        lastID = 0;
        numberOfErrors = 0;
        structsGraph = new Graph<>();
    }

    private String generateName() {
        lastID++;
        return Integer.toString(lastID);
    }

    public int getNumberOfErrors() { return numberOfErrors; }

    @Override
    public Void visit(Program program) {
        SymbolTable programST = new SymbolTable();
        SymbolTable.push(programST);
        SymbolTable.root = programST;

        for (StructDeclaration structDeclaration : program.getStructs()) {
            structsGraph.addVertex(structDeclaration.getStructName().getName());
            SymbolTable structSymbolTable = new SymbolTable(SymbolTable.top);
            StructSymbolTableItem structSymbolTableItem = new StructSymbolTableItem(structDeclaration);
            structSymbolTableItem.setStructSymbolTable(structSymbolTable);
            try {
                String startKey = FunctionSymbolTableItem.START_KEY;
                String name = startKey.concat(structSymbolTableItem.getName());
                SymbolTable.top.getItem(name);
                FunctionStructConflict functionStructConflict;
                functionStructConflict = new FunctionStructConflict(
                        structDeclaration.getLine(), structSymbolTableItem.getName());
                System.out.println(functionStructConflict.getMessage());
                numberOfErrors++;
            }
            catch (ItemNotFoundException ex) {
                // there is no conflict with structs
            }

            try {
                programST.put(structSymbolTableItem);
            }
            catch (ItemAlreadyExistsException ex) {
                DuplicateStruct duplicateStruct;
                duplicateStruct = new DuplicateStruct(
                        structDeclaration.getLine(), structSymbolTableItem.getName());
                System.out.println(duplicateStruct.getMessage());
                numberOfErrors++;
                structSymbolTableItem.setName(generateName());
                try {
                    programST.put(structSymbolTableItem);
                }
                catch(ItemAlreadyExistsException ex2) {}
            }
            SymbolTable.push(structSymbolTable);
            currentScope = structSymbolTableItem.getKey();
            structDeclaration.accept(this);
        }

        for (FunctionDeclaration functionDeclaration : program.getFunctions()) {
            SymbolTable functionSymbolTable = new SymbolTable(SymbolTable.top);
            FunctionSymbolTableItem functionSymbolTableItem = new FunctionSymbolTableItem(functionDeclaration);
            functionSymbolTableItem.setFunctionSymbolTable(functionSymbolTable);
            try {
                String startKey = StructSymbolTableItem.START_KEY;
                String name = startKey.concat(functionSymbolTableItem.getName());
                SymbolTable.top.getItem(name);
                FunctionStructConflict functionStructConflict;
                functionStructConflict = new FunctionStructConflict(
                        functionDeclaration.getLine(), functionSymbolTableItem.getName());
                System.out.println(functionStructConflict.getMessage());
                numberOfErrors++;
            }
            catch (ItemNotFoundException ex) {
                // there is no conflict with structs
            }
            try {
                programST.put(functionSymbolTableItem);
            }
            catch (ItemAlreadyExistsException ex) {
                DuplicateFunction duplicateFunction;
                duplicateFunction = new DuplicateFunction(
                        functionDeclaration.getLine(), functionSymbolTableItem.getName());
                System.out.println(duplicateFunction.getMessage());
                numberOfErrors++;
                functionSymbolTableItem.setName(generateName());
                try {
                    programST.put(functionSymbolTableItem);
                }
                catch(ItemAlreadyExistsException ex2) {}
            }
            SymbolTable.push(functionSymbolTable);
            currentScope = functionSymbolTableItem.getKey();
            functionDeclaration.accept(this);
        }

        MainDeclaration mainDeclaration = program.getMain();
        SymbolTable mainSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(mainSymbolTable);
        currentScope = "main"; //todo
        mainDeclaration.accept(this);
        SymbolTable.pop(); // pop program

        // finding cyclic dependency in structs
        structsGraph.findSCC();
        for (StructDeclaration structDeclaration : program.getStructs()) {
            String structName = structDeclaration.getStructName().getName()
            int line = structDeclaration.getLine();
            if (structsGraph.isInCycle(structName)) {
                CyclicDependency cyclicDependency = new CyclicDependency(line, structName);
                System.out.println(cyclicDependency.getMessage());
            }
        }
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration functionDec) {
        for (VariableDeclaration variableDeclaration : functionDec.getArgs())
            variableDeclaration.accept(this);
        functionDec.getBody().accept(this);
        SymbolTable.pop();
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
            String startKey = StructSymbolTableItem.START_KEY;
            String name = startKey.concat(variableSymbolTableItem.getName());
            SymbolTable.top.getItem(name);
            VarStructConflict varStructConflict = new VarStructConflict(variableDec.getLine(), variableSymbolTableItem.getName());
            System.out.println(varStructConflict.getMessage());
            numberOfErrors++;
        }
        catch (ItemNotFoundException ex) {
            // there is no conflict with structs
        }

        try {
            String startKey = FunctionSymbolTableItem.START_KEY;
            String name = startKey.concat(variableSymbolTableItem.getName());
            SymbolTable.top.getItem(name);
            VarFunctionConflict varFunctionConflict;
            varFunctionConflict = new VarFunctionConflict(variableDec.getLine(), variableSymbolTableItem.getName());
            System.out.println(varFunctionConflict.getMessage());
            numberOfErrors++;
        }
        catch (ItemNotFoundException ex) {
            // there is no conflict with functions
        }

        try{
            SymbolTable.top.getItem(variableSymbolTableItem.getKey());
            DuplicateVar duplicateVar;
            duplicateVar = new DuplicateVar(variableDec.getLine(), variableSymbolTableItem.getName());
            System.out.println(duplicateVar.getMessage());
            variableSymbolTableItem.setName(generateName());
            numberOfErrors++;
        }
        catch(ItemNotFoundException ex){
            // there is no duplicated var
        }

        try {
            SymbolTable.top.put(variableSymbolTableItem);
            // check if an struct variable is declared in other structs scope
            String startKey = StructSymbolTableItem.START_KEY;
            boolean isStructVariable = variableDeclaration.gatVarType() instanceof StructType;
            boolean isInStructScope = SymbolTable.top.pre == SymbolTable.root && currentScope.startsWith(startKey);
            
            if (isStructVariable && isInStructScope) {
                StructType structType = variableDeclaration.getVarType()
                String varStructName = structType.getStructName().getName();
                String scopeStructName = currentScope.substring(startKey.length());
                if (varStructName.equals(scopeStructName))
                    structsGraph.addSelfLoop(scopeStructName);
                else
                    structsGraph.addEdge(scopeStructName, varStructName);
            }
        }
        catch(ItemAlreadyExistsException ex) {}
        return null;
    }

    @Override
    public Void visit(StructDeclaration structDec) {
        structDec.getBody().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(SetGetVarDeclaration setGetVarDec) {
        SymbolTable setGetSymbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(setGetSymbolTable);
        VariableDeclaration fieldDec = new VariableDeclaration(
                setGetVarDec.getVarName(), setGetVarDec.getVarType());
        fieldDec.setLine(setGetVarDec.getLine());
        fieldDec.accept(this);
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
}
