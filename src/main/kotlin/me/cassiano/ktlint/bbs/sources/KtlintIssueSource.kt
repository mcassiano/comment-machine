package me.cassiano.ktlint.bbs.sources

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import me.cassiano.ktlint.bbs.core.FilePathAndIssues
import me.cassiano.ktlint.bbs.core.Issue
import me.cassiano.ktlint.bbs.core.IssueSource
import me.cassiano.ktlint.bbs.core.run
import java.io.File
import java.io.StringReader

class KtlintIssueSource(
        private val klaxon: Klaxon,
        private val workingDirectory: String
) : IssueSource {

    override fun getIssues(paths: List<String>): List<FilePathAndIssues> {
        val fileArgs = paths.filter { it.endsWith(".kt") }.joinToString(" ")
        val workingDir = File(workingDirectory)
        val cmd = "./ktlint $fileArgs --reporter=json --relative"
        val ktLintReport = cmd.run(workingDir)
        val jsonReport = klaxon.parseJsonArray(StringReader(ktLintReport))
        val result = mutableListOf<FilePathAndIssues>()

        jsonReport.forEach { item ->
            val reportItem = item as JsonObject
            val fileName = reportItem.string("file")!!


            val issues = reportItem.array<JsonObject>("errors")
                    ?.value
                    ?.map { error ->
                        val line = error.int("line")!!
                        val column = error.int("column")!!
                        val message = error.string("message")!!
                        val rule = error.string("rule")!!
                        Issue(message, rule, fileName, line, column)
                    } ?: emptyList()

            result.add(FilePathAndIssues(fileName, issues))
        }

        return result
    }
}