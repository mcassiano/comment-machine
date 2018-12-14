package me.cassiano.ktlint.bbs.sources

import me.cassiano.ktlint.bbs.core.Comment
import me.cassiano.ktlint.bbs.core.CommentSource
import me.cassiano.ktlint.bbs.core.DiffFile
import me.cassiano.ktlint.bbs.core.Issue

class BitBucketServerCommentSource : CommentSource {

    override fun getComment(issue: Issue): Comment {
//        val pikachu = "[![46482723-914713205554017-4832741937838555136-n-cke.jpg](attachment:896/a960a6b976%2F46482723-914713205554017-4832741937838555136-n-cke.jpg)](attachment:896/a960a6b976%2F46482723-914713205554017-4832741937838555136-n-cke.jpg)"
        return Comment("(${issue.line}, ${issue.column}): ${issue.message}", issue.line, issue.path)
    }

    override fun shouldComment(file: DiffFile, issue: Issue) = issue.line in file.addedSegments

}