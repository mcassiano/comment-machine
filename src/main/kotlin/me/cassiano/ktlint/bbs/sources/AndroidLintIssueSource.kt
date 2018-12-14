package me.cassiano.ktlint.bbs.sources

import me.cassiano.ktlint.bbs.core.FilePathAndIssues
import me.cassiano.ktlint.bbs.core.Issue
import me.cassiano.ktlint.bbs.core.IssueSource
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AndroidLintIssueSource(private val workDir: String) : IssueSource {

    override fun getIssues(paths: List<String>): List<FilePathAndIssues> {
        val map = mutableMapOf<String, MutableList<Issue>>()

        val filePath = "$workDir/app/build/reports/lint-results-debug.xml"
        val xmlFile = File(filePath)
        val xmlDoc = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(xmlFile)
                .also { it.documentElement.normalize() }

        val issues = xmlDoc.getElementsByTagName("issue")

        for (issue in 0 until issues.length) {
            val node = issues.item(issue) as? Element

            if (node != null) {
                val message = node.getAttribute("message")
                val ruleId = node.getAttribute("id")

                val locations = node.getElementsByTagName("location")

                for (location in 0 until (locations?.length ?: 0)) {
                    val nodeLocation = locations?.item(location) as? Element

                    if (nodeLocation != null) {
                        val path = "app/" + nodeLocation.getAttribute("file")
                        val line = nodeLocation.getAttribute("line").toIntOrNull() ?: 1
                        val column = nodeLocation.getAttribute("column").toIntOrNull() ?: 1

                        val newIssue = Issue(message, ruleId, path, line, column)
                        map[path] = map[path]?.add(newIssue)?.let { map[path] } ?: mutableListOf(newIssue)
                    }
                }
            }
        }

        return map.map { FilePathAndIssues(it.key, it.value) }

    }

}