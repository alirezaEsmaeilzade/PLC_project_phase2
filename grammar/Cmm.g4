grammar Cmm;

@header{
     import main.ast.nodes.*;
     import main.ast.nodes.declaration.*;
     import main.ast.nodes.declaration.struct.*;
     import main.ast.nodes.expression.*;
     import main.ast.nodes.expression.operators.*;
     import main.ast.nodes.expression.values.*;
     import main.ast.nodes.expression.values.primitive.*;
     import main.ast.nodes.statement.*;
     import main.ast.types.*;
     import main.ast.types.primitives.*;
     import java.util.*;
 }

cmm returns[Program cmmProgram]:
    NEWLINE p = program {$cmmProgram = $p.programRet;} NEWLINE EOF;

program returns[Program programRet]:
    {$programRet = new Program();
     int line = 1;
     $programRet.setLine(line);}
    (s = structDeclaration {$programRet.addStruct($s.structDeclarationRet);})*
    (f = functionDeclaration {$programRet.addFunction($f.functionDeclarationRet);})*
    (m = main {$programRet.setMain($m.mainRet);});

main returns[MainDeclaration mainRet]:
    (m = MAIN)
    {$mainRet = new MainDeclaration();
     int line = $m.getLine();
     $mainRet.setLine(line);}
    LPAR RPAR (b = body {$mainRet.setBody($b.bodyRet);});

//todo
structDeclaration returns[StructDeclaration structDeclarationRet]:
    STRUCT identifier ((BEGIN structBody NEWLINE+ END) | (NEWLINE+ singleStatementStructBody SEMICOLON?)) NEWLINE+;

//todo
singleVarWithGetAndSet :
    type identifier functionArgsDec BEGIN NEWLINE+ setBody getBody END;

//todo
singleStatementStructBody :
    varDecStatement | singleVarWithGetAndSet;

//todo
structBody :
    (NEWLINE+ (singleStatementStructBody SEMICOLON)* singleStatementStructBody SEMICOLON?)+;

//todo
getBody :
    GET body NEWLINE+;

//todo
setBody :
    SET body NEWLINE+;

//todo
functionDeclaration returns[FunctionDeclaration functionDeclarationRet]:
    {$functionDeclarationRet = new FunctionDeclaration();}
    (t = type {$functionDeclarationRet.setReturnType($t.typeRet);} |
    VOID {$functionDeclarationRet.setReturnType(new VoidType());})
    i = identifier {$functionDeclarationRet.setFunctionName($i.identifierRet);}
    f = functionArgsDec {$functionDeclarationRet.setArgs($f.functionArgsDecRet);}
    b = body {$functionDeclarationRet.setBody($b.bodyRet);}
    NEWLINE+;

//todo
functionArgsDec returns[ArrayList<VariableDeclaration> functionArgsDecRet]:
    {$functionArgsDecRet = new ArrayList<>();}
    LPAR (t1 = type i1 = identifier
    {$functionArgsDecRet.add(new VariableDeclaration($i1.identifierRet, $t1.typeRet));}
    (COMMA t2 = type i2 = identifier
    {$functionArgsDecRet.add(new VariableDeclaration($i2.identifierRet, $t2.typeRet));})*)? RPAR ;

//todo
functionArguments returns[ArrayList<Expression> args]:
    {$args = new ArrayList<>();}
    (e1 = expression {$args.add($e1.expressionRet);}
    (COMMA e2 = expression {$args.add($e2.expressionRet);})*)?;

//todo
body returns[Statement bodyRet]:
    (b = blockStatement {$bodyRet = $b.blockStatementRet;}) |
    (NEWLINE+ (s = singleStatement {$bodyRet = $s.singleStatementRet;}) (SEMICOLON)?);

//todo
loopCondBody returns[Statement loopCondBodyRet]:
    (b = blockStatement {$loopCondBodyRet = $b.blockStatementRet;}) |
    (NEWLINE+ s = singleStatement {$loopCondBodyRet = $s.singleStatementRet;});

//todo how implement for *?
blockStatement returns[BlockStmt blockStatementRet]://
    {$blockStatementRet = new BlockStmt();}
    b = BEGIN
    {int line = $b.getLine();
     $blockStatementRet.setLine(line);}
    (NEWLINE+ (s1 = singleStatement {$blockStatementRet.addStatement($s1.singleStatementRet);} SEMICOLON)*
    (s2 = singleStatement {$blockStatementRet.addStatement($s2.singleStatementRet);}) (SEMICOLON)?)+
    NEWLINE+ END;

//todo very important we haven't got variableDeclaration in varDecStatement !!!
varDecStatement returns[VarDecStmt varDecStatementRet]:
    {ArrayList<VariableDeclaration> vars = new ArrayList<>();
     VariableDeclaration instance1, instance2;}
    t  = type i1 = identifier
    {instance1 = new VariableDeclaration($i1.identifierRet, $t.typeRet);}
    (ASSIGN e1 = orExpression {instance1.setDefaultValue($e1.orExpressionRet);} )?
    {vars.add(instance1);}
    (COMMA (i2 = identifier {instance2 = new VariableDeclaration($i2.identifierRet, $t.typeRet);})
    (ASSIGN e2 = orExpression {instance2.setDefaultValue($e2.orExpressionRet);} )?
    {vars.add(instance2);})*
    {$varDecStatementRet = new VarDecStmt();
     $varDecStatementRet.setVars(vars);};

//todo set line for last LPar
functionCallStmt returns[FunctionCallStmt functionCallStmtRet]:
    {Expression instance;}
    o = otherExpression {instance = $o.otherExpressionRet;}
    ((l1 = LPAR f1 = functionArguments RPAR)
    {instance = new FunctionCall(instance, $f1.args);
     instance.setLine($l1.getLine());} |
    (d = DOT i = identifier)
    {instance = new StructAccess(instance, $i.identifierRet);
     instance.setLine($d.getLine());})*
    (l2 = LPAR f2 = functionArguments RPAR
    {FunctionCall funcCall = new FunctionCall(instance, $f2.args);
     funcCall.setLine($l2.getLine());
     $functionCallStmtRet = new FunctionCallStmt(funcCall);
     $functionCallStmtRet.setLine($l2.getLine());});

//todo
returnStatement returns[ReturnStmt returnStatementRet]:
    {$returnStatementRet = new ReturnStmt();}
    r = RETURN
    {int line = $r.getLine();
     $returnStatementRet.setLine(line);}
    (e = expression {$returnStatementRet.setReturnedExpr($e.expressionRet);})?;

//todo
ifStatement returns[ConditionalStmt ifStatementRet]:
    i = IF e = expression
    {$ifStatementRet = new ConditionalStmt($e.expressionRet);
     int line = $i.getLine();
     $ifStatementRet.setLine(line);}
    ((l = loopCondBody {$ifStatementRet.setThenBody($l.loopCondBodyRet);})|
     (b = body {$ifStatementRet.setThenBody($b.bodyRet);}
     es = elseStatement {$ifStatementRet.setElseBody($es.elseStatementRet);}));

//todo
elseStatement returns[Statement elseStatementRet]:
     NEWLINE* ELSE l = loopCondBody {$elseStatementRet = $l.loopCondBodyRet;};

//todo
loopStatement returns[LoopStmt loopStatementRet]:
    w = whileLoopStatement {$loopStatementRet = $w.whileLoopStatementRet;} |
    d = doWhileLoopStatement {$loopStatementRet = $d.doWhileLoopStatementRet;};

//todo
whileLoopStatement returns[LoopStmt whileLoopStatementRet]:
    {$whileLoopStatementRet = new LoopStmt();}
    w = WHILE
    {int line = $w.getLine();
     $whileLoopStatementRet.setLine(line);}
    e = expression {$whileLoopStatementRet.setCondition($e.expressionRet);}
    l = loopCondBody {$whileLoopStatementRet.setBody($l.loopCondBodyRet);};

//todo falure is probably
doWhileLoopStatement returns[LoopStmt doWhileLoopStatementRet]:
    {$doWhileLoopStatementRet = new LoopStmt();}
    d = DO
    {int line = $d.getLine();
     $doWhileLoopStatementRet.setLine(line);}
    b = body {$doWhileLoopStatementRet.setBody($b.bodyRet);} NEWLINE* WHILE
    e = expression {$doWhileLoopStatementRet.setCondition($e.expressionRet);};

//todo
displayStatement returns[DisplayStmt displayStatementRet] :
    d = DISPLAY LPAR e = expression
    {$displayStatementRet = new DisplayStmt($e.expressionRet);
     int line = $d.getLine();
     $displayStatementRet.setLine(line);} RPAR;

//todo problem for orexpr
assignmentStatement returns[AssignmentStmt assignmentStatementRet]:/// get line from assign
    o = orExpression a = ASSIGN e = expression
    {$assignmentStatementRet = new AssignmentStmt($o.orExpressionRet, $e.expressionRet);
     int line = $a.getLine();
     $assignmentStatementRet.setLine(line);};

//todo
singleStatement returns[Statement singleStatementRet]:
    i = ifStatement {$singleStatementRet = $i.ifStatementRet;} |
    d = displayStatement {$singleStatementRet = $d.displayStatementRet;} |
    f = functionCallStmt {$singleStatementRet = $f.functionCallStmtRet;} |
    r = returnStatement {$singleStatementRet = $r.returnStatementRet;} |
    a = assignmentStatement {$singleStatementRet = $a.assignmentStatementRet;} |
    v = varDecStatement {$singleStatementRet = $v.varDecStatementRet;} |
    l = loopStatement {$singleStatementRet = $l.loopStatementRet;} |
    app = append
    {int line = $app.appendRet.getLine();
     $singleStatementRet = new ListAppendStmt($app.appendRet);
     $singleStatementRet.setLine(line);} |
    s = size
    {int line = $s.sizeRet.getLine();
     $singleStatementRet = new ListSizeStmt($s.sizeRet);
     $singleStatementRet.setLine(line);};

//todo
expression returns[Expression expressionRet]:
    o = orExpression {$expressionRet = $o.orExpressionRet;}
    (op = ASSIGN e = expression
    {Expression operand1 = $expressionRet;
     Expression operand2 = $e.expressionRet;
     BinaryOperator operator = BinaryOperator.assign;
     $expressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $expressionRet.setLine(line);})? ;

//todo
orExpression returns[Expression orExpressionRet]:
    o1 = andExpression {$orExpressionRet = $o1.andExpressionRet;}
    (op = OR {BinaryOperator operator = BinaryOperator.or;} o2 = andExpression
    {Expression operand1 = $orExpressionRet;
     Expression operand2 = $o2.andExpressionRet;
     $orExpressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $orExpressionRet.setLine(line);})*;

//todo
andExpression returns[Expression andExpressionRet]:
    o1 = equalityExpression {$andExpressionRet = $o1.equalityExpressionRet;}
    (op = AND {BinaryOperator operator = BinaryOperator.and;} o2 = equalityExpression
    {Expression operand1 = $andExpressionRet;
     Expression operand2 = $o2.equalityExpressionRet;
     $andExpressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $andExpressionRet.setLine(line);})*;

//todo
equalityExpression returns[Expression equalityExpressionRet]:
    o1 = relationalExpression {$equalityExpressionRet = $o1.relationalExpressionRet;}
    (op = EQUAL {BinaryOperator operator = BinaryOperator.eq;} o2 = relationalExpression 
    {Expression operand1 = $equalityExpressionRet;
     Expression operand2 = $o2.relationalExpressionRet;
     $equalityExpressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $equalityExpressionRet.setLine(line);})*;

//todo
relationalExpression returns[Expression relationalExpressionRet]:
    o1 = additiveExpression {$relationalExpressionRet = $o1.additiveExpressionRet;}
    ( {BinaryOperator operator;}
    (op = GREATER_THAN {operator = BinaryOperator.gt;} | op = LESS_THAN {operator = BinaryOperator.lt;})
     o2 = additiveExpression 
    {Expression operand1 = $relationalExpressionRet;
     Expression operand2 = $o2.additiveExpressionRet;
     $relationalExpressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $relationalExpressionRet.setLine(line);})*;

//todo
additiveExpression returns[Expression additiveExpressionRet]:
    o1 = multiplicativeExpression {$additiveExpressionRet = $o1.multiplicativeExpressionRet;}
    ( {BinaryOperator operator;}
    (op = PLUS {operator = BinaryOperator.add;} | op = MINUS {operator = BinaryOperator.sub;})
     o2 = multiplicativeExpression 
    {Expression operand1 = $additiveExpressionRet;
     Expression operand2 = $o2.multiplicativeExpressionRet;
     
     $additiveExpressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $additiveExpressionRet.setLine(line);})*;

//todo
multiplicativeExpression returns[Expression multiplicativeExpressionRet]:
    o1 = preUnaryExpression {$multiplicativeExpressionRet = $o1.preUnaryExpressionRet;}
    ( {BinaryOperator operator;}
    (op = MULT {operator = BinaryOperator.mult;} | op = DIVIDE {operator = BinaryOperator.div;})
     o2 = preUnaryExpression 
    {Expression operand1 = $multiplicativeExpressionRet;
     Expression operand2 = $o2.preUnaryExpressionRet;
     $multiplicativeExpressionRet = new BinaryExpression(operand1, operand2, operator);
     int line = $op.getLine();
     $multiplicativeExpressionRet.setLine(line);})*;

//todo
preUnaryExpression returns[Expression preUnaryExpressionRet]:
    ( {UnaryOperator operator;}
    (op = NOT {operator = UnaryOperator.not;} | op = MINUS {operator = UnaryOperator.minus;}) p = preUnaryExpression 
    {Expression operand = $p.preUnaryExpressionRet;
     $preUnaryExpressionRet = new UnaryExpression(operand, operator);
     int line = $op.getLine();
     $preUnaryExpressionRet.setLine(line);}) |
     (a = accessExpression {$preUnaryExpressionRet = $a.accessExpressionRet;});

//todo
accessExpression returns[Expression accessExpressionRet]:
    o = otherExpression {$accessExpressionRet = $o.otherExpressionRet;}

    ((lp = LPAR f = functionArguments RPAR)
    {Expression instance = $accessExpressionRet;
     ArrayList<Expression> args = $f.args;
     $accessExpressionRet = new FunctionCall(instance, args);
     int line = $lp.getLine();
     $accessExpressionRet.setLine(line);} |
     d1 = DOT i1 = identifier
    {Expression instance = $accessExpressionRet;
     Identifier element = $i1.identifierRet;
     $accessExpressionRet = new StructAccess(instance, args);
     int line = $d1.getLine();
     $accessExpressionRet.setLine(line);})*
    
    ((lb = LBRACK e = expression RBRACK)
    {Expression instance = $accessExpressionRet;
     Expression index = $e.expressionRet;
     $accessExpressionRet = new ListAccessByIndex(instance, index);
     int line = $lb.getLine();
     $accessExpressionRet.setLine(line);} |
    (d2 = DOT i2 = identifier)
    {Expression instance = $accessExpressionRet;
     Identifier element = $i2.identifierRet;
     $accessExpressionRet = new StructAccess(instance, args);
     int line = $d2.getLine();
     $accessExpressionRet.setLine(line);})*;

//todo
otherExpression returns[Expression otherExpressionRet]:
    v = value {$otherExpressionRet = $v.valueRet;} |
    i = identifier {$otherExpressionRet = $i.identifierRet;} |
    l = LPAR (f = functionArguments) RPAR
    {$otherExpressionRet = new ExprInPar($f.args);
     int line = $l.getLine();
     $otherExpressionRet.setLine(line);} |
    s = size {$otherExpressionRet = $s.sizeRet;} |
    a = append {$otherExpressionRet = $a.appendRet;};

//todo
size returns[ListSize sizeRet]:
    s = SIZE LPAR e = expression RPAR
    {$sizeRet = new ListSize($e.expressionRet);
     int line = $s.getLine();
     $sizeRet.setLine(line);};

//todo
append returns[ListAppend appendRet]:
    a = APPEND LPAR e1 = expression COMMA e2 = expression RPAR
    {$appendRet = new ListAppend($e1.expressionRet, $e2.expressionRet);
     int line = $a.getLine();
     $appendRet.setLine(line);};

//todo
value returns[Value valueRet]:
    b = boolValue
    {$valueRet = $b.boolValueRet;} |
    i = INT_VALUE
    {$valueRet = new IntValue(Integer.parseInt($i.text));};

//todo
boolValue returns[BoolValue boolValueRet]:
    t = TRUE
    {$boolValueRet = new BoolValue(true);
    int line = $t.getLine();
    $boolValueRet.setLine(line);} |
    f = FALSE
    {$boolValueRet = new BoolValue(false);
    int line = $f.getLine();
    $boolValueRet.setLine(line);};

//todo
identifier returns[Identifier identifierRet]:
    i = IDENTIFIER
    {$identifierRet = new Identifier($i.text);
     int line = $i.getLine();
     $identifierRet.setLine(line);};

//todo
type returns[Type typeRet]:
    INT {$typeRet = new IntType();} |
    BOOL {$typeRet = new BoolType();} |
    LIST SHARP t = type {$typeRet = new ListType($t.typeRet);} |
    STRUCT i = identifier {$typeRet = new StructType($i.identifierRet);} |
    f = fptrType {$typeRet = $f.fptrTypeRet;};

//todo
fptrType returns[FptrType fptrTypeRet]:
    {ArrayList<Type> argsTypes;
     Type returnType;}
    FPTR LESS_THAN
    (VOID | (t1 = type {argsTypes.add($t1.typeRet);} (COMMA t2 = type {argsTypes.add($t2.typeRet);})*))
    ARROW (t3 = type {returnType = $t3.typeRet;} | VOID {returnType = new VoidType();}) GREATER_THAN
    {$fptrTypeRet = new FptrType(argsTypes, returnType);};

MAIN: 'main';
RETURN: 'return';
VOID: 'void';

SIZE: 'size';
DISPLAY: 'display';
APPEND: 'append';

IF: 'if';
ELSE: 'else';

PLUS: '+';
MINUS: '-';
MULT: '*';
DIVIDE: '/';


EQUAL: '==';
ARROW: '->';
GREATER_THAN: '>';
LESS_THAN: '<';


AND: '&';
OR: '|';
NOT: '~';

TRUE: 'true';
FALSE: 'false';

BEGIN: 'begin';
END: 'end';

INT: 'int';
BOOL: 'bool';
LIST: 'list';
STRUCT: 'struct';
FPTR: 'fptr';
GET: 'get';
SET: 'set';
WHILE: 'while';
DO: 'do';

ASSIGN: '=';
SHARP: '#';
LPAR: '(';
RPAR: ')';
LBRACK: '[';
RBRACK: ']';

COMMA: ',';
DOT: '.';
SEMICOLON: ';';
NEWLINE: '\n';

INT_VALUE: '0' | [1-9][0-9]*;
IDENTIFIER: [a-zA-Z_][A-Za-z0-9_]*;


COMMENT: ('/*' .*? '*/') -> skip;
WS: ([ \t\r]) -> skip;