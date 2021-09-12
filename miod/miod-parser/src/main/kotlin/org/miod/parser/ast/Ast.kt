package org.miod.parser.ast

import java.nio.file.Path

/// location marks unit name
class CompUnit(
    val location: Location, val name: String, val comment: Comment,
    val doc: DocComment, val imports: Array<Import>, val globals: Array<GlobalStatement>
)

class TextPosition(val line: Int, val column: Int) {
    override fun toString(): String {
        return "($line:$column)"
    }
}

class Location(val path: Path, val start: TextPosition, val end: TextPosition) {
    override fun toString(): String {
        return "$path: $start..$end"
    }
}

class Comment(val location: Location, val text: String)
class DocComment(val location: Location, val text: String)

/// location marks unit name
class Import(
    val location: Location, val name: String, val all: Boolean,
    val annotations: Array<Annotation>
)

abstract class LiteralValue(val location: Location)
class LiteralIntegerValue(location: Location, val value: Long) : LiteralValue(location)
class LiteralBooleanValue(location: Location, val value: Boolean) : LiteralValue(location)
class LiteralFloatValue(location: Location, val value: Double) : LiteralValue(location)
class LiteralStringValue(location: Location, val value: String) : LiteralValue(location)
class LiteralStringFromIdValue(location: Location, val value: String) : LiteralValue(location)

class Annotation(val location: Location, val name: String, val values: Map<String, LiteralValue>)

abstract class GlobalStatement(val location: Location)
class Const(
    location: Location, val name: String, val value: LiteralValue,
    val annotations: Array<Annotation>
) : GlobalStatement(location)


/// semantic
abstract class Symbol(val location: Location, val name: String)
class UnitDefinitionSymbol(val unitNode: CompUnit) : Symbol(unitNode.location, unitNode.name)
class UnitImportSymbol(val importNode: Import, val importedSymbol: UnitDefinitionSymbol) :
    Symbol(importNode.location, importNode.name)

// for each public symbol imported through "importall"
class UnitImportedSymbol(val importNode: Import, val importedSymbol: Symbol) :
    Symbol(importNode.location, importedSymbol.name)

class ConstSymbol(val constNode: Const) : Symbol(constNode.location, constNode.name)

// contextLocation marks start of proc till the end, or case/for etc. blocks
class SymbolTable(
    val contextLocation: Location, val parent: SymbolTable,
    val symbols: Map<String, Symbol>
)

abstract class CompilationError(val location: Location, val msg: String) {
    override fun toString(): String {
        return "error: $location: $msg"
    }
}

class SyntaxError(location: Location, msg: String) : CompilationError(location, msg)
class UndefinedIdentifier(location: Location, name: String) :
    CompilationError(location, "$name is undefined")

class Redefinition(location: Location, previous: Location, name: String) :
    CompilationError(location, "$name is redefined, previously declared here: $previous")