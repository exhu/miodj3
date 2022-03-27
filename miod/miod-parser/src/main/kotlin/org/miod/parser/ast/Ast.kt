// semantic ast with the important information, suitable to generate code
// and docs
package org.miod.parser.ast

import java.nio.file.Path

/// line 1..n, column 0..n
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

val EMPTY_LOCATION = Location(Path.of(""), TextPosition(0,0), TextPosition(0,0))

abstract class SourceElement()

/// Eof or end of block, used for orphaned comments, docs etc. which do not have semantic element below.
class EmptyAnchor() : SourceElement()

/// location marks unit name
class CompUnit(
    val name: String,
    val imports: List<Import> = listOf(),
    val globals: List<GlobalStatement> = listOf(),
    val annotations: List<Annotation> = listOf(),
    val docs: List<DocComment> = listOf(),
    val comments: List<Comment> = listOf(),
    val sourceLocations: Map<SourceElement, Location> = mapOf(),
    val symbols: SymbolTable? = null
) : SourceElement()

/// location marks unit name
class Import(
    val name: String, val all: Boolean
) : SourceElement()

abstract class GlobalStatement() : SourceElement()
class Const(
    val name: String, val value: LiteralValue
) : GlobalStatement()

abstract class LiteralValue() : SourceElement()
class LiteralIntegerValue(val value: Long) : LiteralValue()
class LiteralBooleanValue(val value: Boolean) : LiteralValue()
class LiteralFloatValue(val value: Double) : LiteralValue()
class LiteralStringValue(val value: String) : LiteralValue()
class LiteralStringFromIdValue(val value: String) : LiteralValue()

class Annotation(anchor: SourceElement, val name: String, val values: Map<String, LiteralValue>) :
    SourceElement()

class Comment(val anchor: SourceElement, val text: String) : SourceElement()
class DocComment(val anchor: SourceElement, val text: String) : SourceElement()

/// semantic
abstract class Symbol(val name: String, val node: SourceElement)
class UnitDefinitionSymbol(val unitNode: CompUnit) : Symbol(unitNode.name, unitNode)
class UnitImportSymbol(val importNode: Import, val importedSymbol: UnitDefinitionSymbol) :
    Symbol(importNode.name, importNode)

// for each public symbol imported through "importall"
class UnitImportedSymbol(val importNode: Import, val importedSymbol: Symbol) :
    Symbol(importedSymbol.name, importNode)

class ConstSymbol(val constNode: Const) : Symbol(constNode.name, constNode)

/// Symbol table per unit/proc/block
class SymbolTable(
    name: String,
    node: SourceElement,
    val parent: SymbolTable?,
    val symbols: Map<String, Symbol>,
): Symbol(name, node) {
    fun resolve(name: String): Symbol? {
        // TODO split by namespace separator
        // if root matches symbol.name or matches in symbols, go deeper
        // else parent.resolve(name)

        return null;
    }
}
