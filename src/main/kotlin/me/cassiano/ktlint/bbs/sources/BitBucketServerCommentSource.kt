package me.cassiano.ktlint.bbs.sources

import me.cassiano.ktlint.bbs.core.Comment
import me.cassiano.ktlint.bbs.core.CommentSource
import me.cassiano.ktlint.bbs.core.DiffFile
import me.cassiano.ktlint.bbs.core.Issue

class BitBucketServerCommentSource : CommentSource {

    override fun getComment(issue: Issue): Comment {
        return Comment("(${issue.line}, ${issue.column}): ${issue.message}", issue.line, issue.path)
    }

    override fun shouldComment(file: DiffFile, issue: Issue) = issue.line in file.addedSegments

}