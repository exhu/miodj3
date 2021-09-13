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

private fun tokenPosition(token: Token): TextPosition {
    return TextPosition(token.line, token.charPositionInLine)
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
            //parser.removeErrorListeners()
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

    override fun reportAmbiguity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
        errors.append(ParserError(Location(filePath, TextPosition(0,0),
            TextPosition(0,0)), "grammar ambiguity ${ambigAlts}"))
    }

    override fun reportContextSensitivity(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet?
    ) {
        errors.append(ParserError(Location(filePath, TextPosition(0,0),
            TextPosition(0,0)), "grammar context sensitivity"))
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        errors.append(SyntaxError(Location(filePath, TextPosition(line, charPositionInLine),
            TextPosition(line, charPositionInLine)), msg ?: ""))
    }

    override fun reportAttemptingFullContext(
        recognizer: Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet?,
        configs: ATNConfigSet?
    ) {
        errors.append(ParserError(Location(filePath, TextPosition(0,0),
            TextPosition(0,0)), "grammar attempting full context"))
    }

    override fun enterUnitHeader(ctx: MiodParser.UnitHeaderContext?) {
        if (ctx != null) {
            val unitName = ctx.unit().unitName?.text ?: ""
            println("unitName=$unitName")
            compUnit = CompUnit(
                Location(filePath, tokenPosition(ctx.start), tokenPosition(ctx.stop)),
                unitName,
                null,
                null,
                arrayOf(),
                arrayOf()
            )
        }
    }
}