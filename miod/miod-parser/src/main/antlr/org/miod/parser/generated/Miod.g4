grammar Miod;

// grammar is simplified for matching instead of optimized for listener
// e.g. 'unit' keyword can be used only once but the grammar allows for duplication
compUnit: globals+ EOF;

globals: emptyLine
    | doc
    | comment
    | unit
    | annotation
    | unitContents
    ;

comment: COMMENT;

emptyLine: NEWLINE;

doc: DOC_COMMENT;

unit: UNIT name=ID NEWLINE;

// moved unit body statements to separate rule
// to trigger an error in the listener if those statements
// are met before 'unit' declaration
unitContents: importDecl
    | PUBLIC? unitDeclarations;

unitDeclarations: constDecl
    | alias
    | struct
    | closure
    | variant
    | enumDecl
    //| flags
    | cproc
    | proc
    ;

alias: ALIAS ID genericArgs? ASSIGN typeNameWithArgs NEWLINE;

struct: STRUCT typeNameWithArgs NEWLINE (doc|comment|emptyLine|field)* END_STRUCT NEWLINE;

callableArgsAndReturn: OPEN_PAREN procArgsDecl? CLOSE_PAREN (COLON typeNameWithArgs)?;

closure: CLOSURE typeNameWithArgs callableArgsAndReturn NEWLINE;

variant: VARIANT name=typeNameWithArgs NEWLINE (comment | doc | emptyLine | variantName)* END_VARIANT NEWLINE;
variantName: annotation* typeNameWithArgs NEWLINE;

enumDecl: ENUM name=ID NEWLINE (comment | doc | emptyLine | enumValue)* END_ENUM;
enumValue: annotation* name=ID NEWLINE;

//flags: FLAGS name=ID NEWLINE (comment | doc | emptyLine | enumValue)* END_FLAGS;

cproc: CPROC procHeader;

procHeader: name=typeNameWithArgs callableArgsAndReturn NEWLINE;

proc: PROC procHeader statement* END_PROC;

statement:
    expr NEWLINE
    | varDecl NEWLINE
    | comment
    | doc
    | annotation
    | emptyLine
    | forLoop NEWLINE
    | BREAK NEWLINE
    | CONTINUE NEWLINE
    ;

forLoop: FOR first=ID (COMMA second=ID)? IN expr NEWLINE statement* END_FOR;

expr:
    recursiveReversed
    | newStruct
    | retainExpr
    | SELF
    | literal
    ;

recursiveReversed:
    namespacedId (assign | (exprChain* (fieldAccessOp assign)?))
    ;

exprChain:
    callOp
    | fieldAccessOp
    ;

callOp: OPEN_PAREN callArgs? CLOSE_PAREN;

callArgs: expr (COMMA NEWLINE? expr)*;

fieldAccessOp: DOT NEWLINE? ID;

retainExpr: RETAIN ID;

varDecl: LET MUT? name=ID assign;

assign: ASSIGN NEWLINE? expr;

newStruct: typeNameWithArgs OPEN_CURLY (expr | fieldsInit)? CLOSE_CURLY;

fieldsInit: fieldInit (COMMA NEWLINE? fieldInit)*;
fieldInit: ID COLON expr;


procArgsDecl: (SELF | idTypePair) (COMMA NEWLINE? idTypePair)*;

idTypePair: name=ID COLON typeNameWithArgs;

field: annotation* PUBLIC? MUT? idTypePair setterOrGetter* NEWLINE;
setterOrGetter: COMMA NEWLINE? (SETTER|GETTER) name=ID;

typeNameWithArgs: SHARED? namespacedId genericArgs?;

genericArgs: typeArgsOpen typeNameWithArgs (COMMA NEWLINE? typeNameWithArgs)*  typeArgsClose;

typeArgsOpen: TYPE_ARGS_OPEN;
typeArgsClose: CLOSE_BRACKET;

namespacedId: root=ID (NAMESPACE_SEP subName=ID)*;

importDecl: importUnit | importAllFromUnit;

// IMPORT imports units, so that public symbols can be addressed as myunit.procName
importUnit: IMPORT unitName=ID NEWLINE;

// IMPORT_ALL imports unit public symbols into global namespace
importAllFromUnit: IMPORT_ALL unitName=ID NEWLINE;

constDecl: CONST name=ID (COLON type=ID)? ASSIGN NEWLINE? literal NEWLINE;

// annotations
annotation: annotationWithData | annotationEmpty;
annotationWithData: ANNOTATE name=ID structInitLiteralNoExpr NEWLINE;
annotationEmpty: ANNOTATE name=ID NEWLINE;

// no expressions in initialization
structInitLiteralNoExpr: OPEN_CURLY NEWLINE? (literal | initNamedMembersNoExpr) CLOSE_CURLY;

initNamedMembersNoExpr: initNamedMemberNoExpr (COMMA NEWLINE? initNamedMemberNoExpr)*;
initNamedMemberNoExpr: ID COLON literal;

// literals
literal: stringLiteral | numericLiteral | boolLiteral | strFromId;

boolLiteral: TRUE | FALSE;

strFromId: STR_FROM_ID name=ID;

stringLiteral: STRING;

floatLiteral: FLOAT_OR_DOUBLE;

decimalLiteral: INTEGER;

hexadecimalLiteral: INT_HEX;

binaryLiteral: INT_BIN;

octalLiteral: INT_OCTAL;

integerLiteral: decimalLiteral | hexadecimalLiteral | binaryLiteral | octalLiteral;

numericLiteral: integerLiteral | floatLiteral;

//// lexer --------------
fragment NL: ('\r'? '\n');

NEWLINE: NL;

// comments
DOC_COMMENT: '##' .*? (NL|EOF);
COMMENT: '#' .*? (NL|EOF);

//
WS: (' ' | '\t')+ -> skip;

// keywords
UNIT: 'unit';
SELF: 'self';
CONST: 'const';
PUBLIC: 'pub';
MUT: 'mut';
LET: 'let';
PROC: 'proc';
CPROC: 'cproc';
END_PROC: 'endproc';
IMPORT: 'import';
IMPORT_ALL: 'importall';
VARIANT: 'variant';
END_VARIANT: 'endvariant';
MATCH: 'match';
END_MATCH: 'endmatch';
IF: 'if';
THEN: 'then';
ELSE: 'else';
ELIF: 'elif';
END_IF: 'endif';
ASSIGN: '=';
TRUE: 'true';
FALSE: 'false';
ALIAS: 'alias';
STRUCT: 'struct';
END_STRUCT: 'endstruct';
ANNOTATE: '@';
OPEN_CURLY: '{';
CLOSE_CURLY: '}';
COLON: ':';
COMMA: ',';
OPEN_PAREN: '(';
CLOSE_PAREN: ')';
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
//FLAGS: 'flags';
//END_FLAGS: 'endflags';
SETTER: 'setter';
GETTER: 'getter';
// Map$[String, Integer] -- integer map generic type
TYPE_ARGS_OPEN: '$[';
STR_FROM_ID: 'str_from_id';
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

INT_OCTAL: '-'? '0o' OCTAL+ ('_' OCTAL+)*?;
INT_HEX: '-'? '0x' HEX+ ('_' HEX+)*?;
INT_BIN: '-'? '0b' BIN+ ('_' HEX+)*?;
FLOAT_OR_DOUBLE: '-'? ((DIGIT+ ('_' DIGIT+)* '.' DIGIT*) | ('.' DIGIT+)) ([eE][+\-]DIGIT+)?;
INTEGER: '-'? DIGIT+ ('_' DIGIT+)*;


//////

