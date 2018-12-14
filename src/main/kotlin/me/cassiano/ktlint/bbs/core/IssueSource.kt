package me.cassiano.ktlint.bbs.core

interface IssueSource {

    fun getIssues(paths: List<String>): List<FilePathAndIssues>
}