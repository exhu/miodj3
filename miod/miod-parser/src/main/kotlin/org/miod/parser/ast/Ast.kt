package org.miod.parser.ast

class Location(path: java.nio.file.Path, line: Int, column: Int)
class Comment(location: Location, text: String)
class DocComment(location: Location, text: String)
class Import(location: Location, name: String, all: Boolean)
class Unit(location: Location, name: String, comment: Comment, doc: DocComment, imports: Array<Import>)

abstract class LiteralValue(location: Location)
class LiteralIntegerValue (location: Location, value: Long) : LiteralValue(location)
class LiteralBooleanValue (location: Location, value: Boolean) : LiteralValue(location)
class LiteralFloatValue (location: Location, value: Double) : LiteralValue(location)
class LiteralStringValue (location: Location, value: String) : LiteralValue(location)
class LiteralStringFromIdValue(location: Location, value: String) : LiteralValue(location)

class Annotation(location: Location, name: String, values: Map<String, LiteralValue>)

class Const(location: Location, name: String, value: LiteralValue)