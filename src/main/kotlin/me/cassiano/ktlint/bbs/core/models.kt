package me.cassiano.ktlint.bbs.core

typealias FilePathAndIssues = Pair<String, List<Issue>>

data class Comment(val text: String, val line: Int, val path: String)

data class DiffFile(
        val filePath: String,
        val addedSegments: List<Int>
)

data class Issue(
        val message: String,
        val rule: String,
        val path: String,
        val line: Int,
        val column: Int
)