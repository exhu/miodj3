// semantic ast with the important information, suitable to generate code
// and docs
package org.miod.parser.ast

import java.nio.file.Path

abstract class SourceElement(val location: Location)

/// location marks unit name
class CompUnit(
    location: Location, val name: String, val comment: Comment?,
    val doc: DocComment?, val imports: Array<Import>, val globals: Array<GlobalStatement>
) : SourceElement(location)

data class TextPosition(val line: Int, val column: Int) {
    override fun toString(): String {
        return "($line:$column)"
    }
}

class Location(val path: Path, val start: TextPosition, val end: TextPosition) {
    override fun toString(): String {
        if (start == end) {
            return "$path: $start"
        }
        return "$path: $start..$end"
    }
}

class Comment(location: Location, val text: String) : SourceElement(location)
class DocComment(location: Location, val text: String) : SourceElement(location)

/// location marks unit name
class Import(
    location: Location, val name: String, val all: Boolean,
    val annotations: Array<Annotation>
) : SourceElement(location)

abstract class LiteralValue(location: Location) : SourceElement(location)
class LiteralIntegerValue(location: Location, val value: Long) : LiteralValue(location)
class LiteralBooleanValue(location: Location, val value: Boolean) : LiteralValue(location)
class LiteralFloatValue(location: Location, val value: Double) : LiteralValue(location)
class LiteralStringValue(location: Location, val value: String) : LiteralValue(location)
class LiteralStringFromIdValue(location: Location, val value: String) : LiteralValue(location)

class Annotation(location: Location, val name: String, val values: Map<String, LiteralValue>) :
    SourceElement(location)

abstract class GlobalStatement(location: Location) : SourceElement(location)
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
