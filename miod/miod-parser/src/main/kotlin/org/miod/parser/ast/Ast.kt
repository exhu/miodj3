package org.miod.parser.ast

import java.nio.file.Path

class Location(val path: Path, val line: Int, val column: Int)
class Comment(val location: Location, val text: String)
class DocComment(val location: Location, val text: String)
class Import(val location: Location, val name: String, val all: Boolean,
             val annotations: Array<Annotation>)
class Unit(val location: Location, val name: String, val comment: Comment,
           val doc: DocComment, val imports: Array<Import>)

abstract class LiteralValue(val location: Location)
class LiteralIntegerValue (location: Location, val value: Long) : LiteralValue(location)
class LiteralBooleanValue (location: Location, val value: Boolean) : LiteralValue(location)
class LiteralFloatValue (location: Location, val value: Double) : LiteralValue(location)
class LiteralStringValue (location: Location, val value: String) : LiteralValue(location)
class LiteralStringFromIdValue(location: Location, val value: String) : LiteralValue(location)

class Annotation(val location: Location, val name: String, val values: Map<String, LiteralValue>)

class Const(val location: Location, val name: String, val value: LiteralValue,
            val annotations: Array<Annotation>)