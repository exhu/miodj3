package org.miod.parser.parser

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.miod.parser.ast.*
import org.miod.parser.generated.MiodBaseListener
import org.miod.parser.generated.MiodLexer
import org.miod.parser.generated.MiodParser
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.path.inputStream


class ParsingErrors {
    private val errorsImpl = ArrayList<CompilationError>()
    val errors: List<CompilationError> get() = errorsImpl

    fun append(error: CompilationError) {
        errorsImpl.add(error)
    }

    fun success(): Boolean {
        return errorsImpl.isEmpty()
    }
}

private fun tokenStartPosition(token: Token?): TextPosition {
    if (token != null) {
        return TextPosition(token.line, token.charPositionInLine)
    }

    return TextPosition(0,0)
}

private fun tokenEndPosition(token: Token?): TextPosition {
    if (token != null) {
        return TextPosition(token.line, token.charPositionInLine + token.text.length-1)
    }

    return TextPosition(0,0)
}

/// Builds AST for a single compilation unit.
class AstBuilder(val filePath: Path) : MiodBaseListener(), ANTLRErrorListener {
    var compUnit: CompUnit? = null
    val errors = ParsingErrors()
    var tree: ParseTree? = null

    fun parse(): Boolean {
        if (filePath.toFile().canRead()) {
            val stream = filePath.inputStream()
            val input: CharStream = CharStreams.fromStream(stream)
            val lexer = MiodLexer(input)
            lexer.removeErrorListeners()
            lexer.addErrorListener(this)
            val tokens = CommonTokenStream(lexer)
            val parser = MiodParser(tokens)
            parser.removeErrorListeners()
            parser.addErrorListener(this)
            // run parser
            tree = parser.compUnit()
        } else {
            errors.append(FileReadError(filePath))
        }

        return errors.success()
    }

    fun buildAst(): Boolean {
        ParseTreeWalker.DEFAULT.walk(this, tree);
        return errors.success()
    }

    private fun zeroLocation(): Location {
        return Location(filePath, TextPosition(0,0),
            TextPosition(0,0))
    }
    override fun reportAmbiguity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
        errors.append(ParserError(zeroLocation(), "grammar ambiguity $ambigAlts"))
    }

    override fun reportContextSensitivity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet?
    ) {
        errors.append(ParserError(zeroLocation(), "grammar context sensitivity, ${recognizer?.context?.toInfoString(recognizer)}"))
    }

    private fun locationAt(line: Int, column: Int): Location {
        return Location(filePath, TextPosition(line, column),
            TextPosition(line, column))
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        errors.append(SyntaxError(locationAt(line, charPositionInLine), msg ?: ""))
    }

    override fun reportAttemptingFullContext(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
        errors.append(ParserError(zeroLocation(), "grammar attempting full context"))
    }

    private fun locationFillToken(token: Token): Location {
        return Location(filePath, tokenStartPosition(token), tokenEndPosition(token))
    }

    override fun enterUnit(ctx: MiodParser.UnitContext?) {
        assert(ctx != null)
        if (ctx != null) {
            val location = locationFillToken(ctx.name)
            if (compUnit != null) {
                errors.append(UnitRedeclaration(location, compUnit!!.location))
                return
            }

            // TODO grab and attach currently accumulated annotations and docs
            val unitName = ctx.name?.text ?: ""
            println("unitName=$unitName")
            compUnit = CompUnit(
                location,
                unitName,
                arrayOf(),
                arrayOf(),
                arrayOf(),
                arrayOf(),
                arrayOf()
            )
        }
    }

    private fun locationFillTokens(start: Token, stop: Token): Location {
        return Location(filePath, tokenStartPosition(start), tokenEndPosition(stop))
    }

    override fun enterUnitContents(ctx: MiodParser.UnitContentsContext?) {
        assert(ctx != null)
        if (ctx != null && compUnit == null) {
            errors.append(UnitDeclarationExpected(locationFillToken(ctx.start)))
        }
    }
}