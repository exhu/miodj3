package org.miod.parser.ast

import java.nio.file.Path

abstract class CompilationError(val location: Location?, val msg: String) {
    override fun toString(): String {
        if (location != null) {
            return "error: $location: $msg"
        }
        return "error: $msg"
    }
}

class FileReadError(path: Path) : CompilationError(null, "cannot read '$path'")

class ParserError(location: Location, msg: String) : CompilationError(location, msg)
class SyntaxError(location: Location, msg: String) : CompilationError(location, msg)

class UndefinedIdentifier(location: Location, name: String) :
    CompilationError(location, "$name is undefined")

class Redefinition(location: Location, previous: Location, name: String) :
    CompilationError(location, "$name is redefined, previously declared here: $previous")

class UnitRedeclaration(location: Location, first: Location) :
    CompilationError(location, "unit redeclaration, first declared at $first")

class UnitDeclarationExpected(location: Location) :
    CompilationError(location, "unit declaration expected")
