package me.cassiano.ktlint.bbs.core

interface PullRequestSource {

    fun getDiff(): Map<String, DiffFile>

    fun comment(comment: Comment)
}