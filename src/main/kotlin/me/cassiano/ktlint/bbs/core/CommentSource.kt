package me.cassiano.ktlint.bbs.core

interface CommentSource {
    fun getComment(issue: Issue): Comment
    fun shouldComment(file: DiffFile, issue: Issue): Boolean
}