package com.mkz.tessavar.shared.adapters.architecture

data class Comment(
    val line: Int,
    val text: String,
)

object CommentScanner {
    private val markers = setOf("// Given", "// When", "// Then", "// When / Then")

    fun comments(text: String): List<Comment> = KotlinSourceReader(text).readComments()

    fun isMarker(comment: Comment): Boolean = comment.text.trim() in markers
}

private class KotlinSourceReader(
    private val text: String,
) {
    private val comments = mutableListOf<Comment>()
    private var position = 0
    private var line = 1

    fun readComments(): List<Comment> {
        readCode(insideTemplate = false)
        return comments
    }

    private fun readCode(insideTemplate: Boolean) {
        var openBraces = 0
        while (position < text.length) {
            when {
                startsWith("//") -> readLineComment()
                startsWith("/*") -> readBlockComment()
                startsWith("\"\"\"") -> readRawString()
                startsWith("\"") -> readString()
                startsWith("'") -> readChar()
                startsWith("`") -> readBacktickName()
                startsWith("{") -> {
                    openBraces++
                    advance(1)
                }
                startsWith("}") && insideTemplate && openBraces == 0 -> {
                    advance(1)
                    return
                }
                startsWith("}") -> {
                    openBraces--
                    advance(1)
                }
                else -> advance(1)
            }
        }
    }

    private fun readLineComment() {
        val end = text.indexOf('\n', position).takeIf { it >= 0 } ?: text.length
        comments += Comment(line, text.substring(position, end))
        advance(end - position)
    }

    private fun readBlockComment() {
        val start = position
        val startLine = line
        var depth = 0
        do {
            when {
                startsWith("/*") -> {
                    depth++
                    advance(2)
                }
                startsWith("*/") -> {
                    depth--
                    advance(2)
                }
                else -> advance(1)
            }
        } while (depth > 0 && position < text.length)
        comments += Comment(startLine, text.substring(start, position))
    }

    private fun readString() {
        advance(1)
        while (position < text.length) {
            when {
                startsWith("\\") -> advance(2)
                startsWith("\${") -> readTemplate()
                startsWith("\"") || startsWith("\n") -> {
                    advance(1)
                    return
                }
                else -> advance(1)
            }
        }
    }

    private fun readRawString() {
        advance(3)
        while (position < text.length) {
            when {
                startsWith("\${") -> readTemplate()
                startsWith("\"\"\"") && !text.startsWith("\"", position + 3) -> {
                    advance(3)
                    return
                }
                else -> advance(1)
            }
        }
    }

    private fun readTemplate() {
        advance(2)
        readCode(insideTemplate = true)
    }

    private fun readChar() {
        advance(1)
        advance(if (startsWith("\\")) 2 else 1)
        if (startsWith("'")) advance(1)
    }

    private fun readBacktickName() {
        val end = text.indexOf('`', position + 1).takeIf { it >= 0 } ?: (text.length - 1)
        advance(end + 1 - position)
    }

    private fun startsWith(prefix: String): Boolean = text.startsWith(prefix, position)

    private fun advance(count: Int) {
        val end = minOf(position + count, text.length)
        line += (position until end).count { index -> text[index] == '\n' }
        position = end
    }
}
