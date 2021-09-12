package org.miod.parser.ast

import java.nio.file.Path

/// location marks unit name
class CompUnit(
    val location: Location, val name: String, val comment: Comment,
    val doc: DocComment, val imports: Array<Import>, val globals: Array<GlobalStatement>
)

class TextPosition(val line: Int, val column: Int)
class Location(val path: Path, val start: TextPosition, val end: TextPosition)

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
abstract class Symbol()
class UnitDefinitionSymbol(val unitNode: CompUnit) : Symbol()
class UnitImportSymbol(val importNode: Import, val importedSymbol: UnitDefinitionSymbol) : Symbol()
class ConstSymbol(val constNode: Const) : Symbol()