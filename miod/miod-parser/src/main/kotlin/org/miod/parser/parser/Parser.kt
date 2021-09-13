package org.miod.parser.parser

import org.antlr.v4.runtime.Token
import org.miod.parser.ast.CompUnit
import org.miod.parser.ast.CompilationError
import org.miod.parser.generated.MiodBaseListener
import org.miod.parser.generated.MiodParser
import java.nio.file.Path
import org.miod.parser.ast.Location
import org.miod.parser.ast.TextPosition

class ParsingErrors {
    val errors: List<CompilationError> = ArrayList<CompilationError>()
}

private fun tokenPosition(token: Token): TextPosition {
    return TextPosition(token.line, token.charPositionInLine)
}

/// Builds AST for a single compilation unit.
class AstBuilder(val filePath: Path) : MiodBaseListener() {
    var compUnit: CompUnit? = null
    val errors = ParsingErrors()

    // TODO write actual tree loading/parsing and parsing test
    override fun enterUnitHeader(ctx: MiodParser.UnitHeaderContext?) {
        if (ctx != null) {
            compUnit = CompUnit(Location(filePath,tokenPosition(ctx.start), tokenPosition(ctx.end)),
                ctx.unit().unitName.text,
                null,
                null,
                arrayOf(),
                arrayOf()
            )
        }
    }
}