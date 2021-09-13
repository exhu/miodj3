/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package org.miod.parser

import org.miod.parser.ast.CompilationError
import org.miod.parser.ast.Location
import org.miod.parser.ast.TextPosition
import org.miod.parser.ast.UndefinedIdentifier
import org.miod.parser.parser.AstBuilder
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryTest {
    @Test fun UndefinedIdentifierErrorClassTest() {
        val ce = UndefinedIdentifier(Location(
            Path.of("a", "sub"), TextPosition(1,1),
            TextPosition(2,5)
        ), "abc")
        assertEquals("error: a/sub: (1:1)..(2:5): abc is undefined", ce.toString())
    }

    @Test fun unitHeaderTest() {
        println(System.getProperty("user.dir"))
        val builder = AstBuilder(Path.of("test-sources", "empty_unit.miod"))
        val result = builder.parse()
        println(builder.errors.errors)
        assertTrue(result)
    }
}
