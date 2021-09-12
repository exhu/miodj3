grammar Miod;

compUnit: comments? unitHeader unitBody EOF;

unitHeader: unitDocs? unit NEWLINE imports?;

unitDocs: docs;

unit: UNIT unitName=ID;

comments: COMMENT+;
docs: DOC_COMMENT+;

unitBody: globalStatements?;

imports: importDecl+;
importDecl: importUnit | importAllFromUnit;

// IMPORT imports units, so that public symbols can be addressed as myunit.procName
importUnit: IMPORT unitName=ID NEWLINE;

// IMPORT_ALL imports unit public symbols into global namespace
importAllFromUnit: IMPORT_ALL unitName=ID NEWLINE;

globalStmt:
    constDecl
    | comments
    | NEWLINE
    ;

globalStatements: globalStmt+;

boolExpr: TRUE | FALSE;

constDecl: docs? annotations? PUBLIC? CONST name=ID (COLON type=typeSpec)? ASSIGN NEWLINE? literal NEWLINE;

annotations: annotation+;

annotation: ANNOTATE name=ID structInitLiteralNoExpr? NEWLINE;

// no expressions in initialization
structInitLiteralNoExpr: OPEN_CURLY NEWLINE?
    initNamedMembersNoExpr | initAnonymousMembersNoExpr
    CLOSE_CURLY;

initNamedMembersNoExpr: initNamedMemberNoExpr (COMMA initNamedMemberNoExpr)*;
initNamedMemberNoExpr: ID COLON literal;

initAnonymousMembersNoExpr: initAnonymousMemberNoExpr (COMMA initAnonymousMemberNoExpr)*;
initAnonymousMemberNoExpr: literal;


literal: STRING | INTEGER | FLOAT;

argDecl: OPEN_PAREN CLOSE_PAREN;

typeSpec: ID;

expr: literal
    | ID;

procBody: ;


//// lexer --------------

fragment NL: ('\r'? '\n');

NEWLINE: NL;
// comments

DOC_COMMENT: '##' .*? NL;
COMMENT: '#' .*? NL;

//
WS: (' ' | '\t')+ -> skip;

// keywords
UNIT: 'unit';
CONST: 'const';
PUBLIC: 'pub';
MUT: 'mut';
LET: 'let';
VAR: 'var';
PROC: 'proc';
CPROC: 'cproc';
END_PROC: 'end';
IMPORT: 'import';
IMPORT_ALL: 'importall';
IF: 'if';
THEN: 'then';
ELSE: 'else';
ELIF: 'elif';
END_IF: 'endif';
PLUS: '+';
MINUS: '-';
DIV: '/';
MUL: '*';
MOD: '%';
BNOT: '~';
BOR: '|';
BAND: '&';
NOT: 'not';
OR: 'or';
AND: 'and';
XOR: '^'; // only binary
EQUALS: '==';
NOT_EQ: '!=';
LESS: '<';
GREATER: '>';
LESS_EQ: '<=';
GREATER_EQ: '>=';
ASSIGN: '=';
SHL: 'shl';
SHR: 'shr';
TRUE: 'true';
FALSE: 'false';
ALIAS: 'alias';
FINALLY: 'finally'; // code block run at leaving scope
END_FINALLY: 'endfinally';
STRUCT: 'struct';
END_STRUCT: 'endstruct';
ANNOTATE: '@';
OPEN_CURLY: '{';
CLOSE_CURLY: '}';
COLON: ':';
SEMICOLON: ';';
COMMA: ',';
OPEN_PAREN: '(';
CLOSE_PAREN: ')';
ARRAY: 'array';
OPEN_BRACKET: '[';
CLOSE_BRACKET: ']';
DOT: '.';
NAMESPACE_SEP: '::';
FOR: 'for';
IN: 'in';
END_FOR: 'endfor';
WHILE: 'while';
END_WHILE: 'endwhile';
BREAK: 'break';
CONTINUE: 'continue';
ENUM: 'enum';
END_ENUM: 'endenum';
FLAGS: 'flags';
END_FLAGS: 'endflags';
WEAK: 'weak';
SETTER: 'setter';
GETTER: 'getter';
// Map$<String, Integer> -- integer map generic type
TYPE_ARGS_OPEN: '$<';
LITERAL: 'literal';
RETAIN: 'retain';
SHARED: 'shared'; // for pointers shared between threads
CLOSURE: 'closure';
END_CLOSURE: 'endclosure';

// literals
fragment ESC: '\\"' | '\\\\';
fragment ESC_CHAR: '\\\'' | '\\\\';
STRING: '"' (ESC|~('\r'|'\n'))*? '"';
RAW_STRING: '"""' .*? '"""';
// only 32-127 ASCII char can be specified
CHAR_STR: '\'' (ESC_CHAR|[ -\u007F])*? '\'';

fragment HEX: [a-fA-F0-9_];
fragment DIGIT: [0-9_];
fragment OCTAL: [0-7_];
fragment BIN: [01_];
ID: [a-zA-Z_]+[0-9a-zA-Z]*;

INT_OCTAL: '-'? '0o' OCTAL+ ('_' OCTAL+)* 'U'?;
INT_HEX: '-'? '0x' HEX+ ('_' HEX+)* 'U'?;
INT_BIN: '-'? '0b' BIN+ ('_' HEX+)* 'U'?;
FLOAT: '-'? ((DIGIT+ ('_' DIGIT+)* '.' DIGIT*) | ('.' DIGIT+)) ([eE][+\-]DIGIT+)? 'f'?;
INTEGER: '-'? DIGIT+ ('_' DIGIT+)* 'U'?;


//////

