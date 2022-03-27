package org.miod.parser.ast

import org.miod.parser.ast.CompUnit
import java.nio.file.Path

class ImportResolver() {
    /// parsed and partially parsed units
    val units: MutableMap<String, CompUnit> = mutableMapOf()
    val namePathMap: MutableMap<String, Path> = mutableMapOf()
    /// units which imports are being processed
    val importStack: MutableSet<CompUnit> = mutableSetOf()
    val searchPaths: MutableSet<Path> = mutableSetOf()
}

/*
Stages:
1) syntax parsing;
    - recursive imports detection
2) semantic parsing
    - symbol tables population

optional stages:
- documentation generation
- translation
- reformatting
- go to definition
- refactoring
- symbol completion hints

Syntax parsing:
Stop at first syntax error, no file found etc.
1) syntax parsing of the program file;
2) repeat over imported units, abort if recursive imports;
3) semantic parsing: start with the program file,
 */
