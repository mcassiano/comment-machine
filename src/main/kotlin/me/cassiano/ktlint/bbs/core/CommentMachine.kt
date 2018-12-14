package me.cassiano.ktlint.bbs.core

class CommentMachine(
        private val pullRequestSource: PullRequestSource,
        private val issueSources: List<IssueSource>,
        private val commentSource: CommentSource
) {

    fun run() {
        val diffFiles = pullRequestSource.getDiff()
        val issues = issueSources.fold(emptyList<FilePathAndIssues>()) { acc, issueSource ->
            acc + issueSource.getIssues(diffFiles.keys.toList())
        }

        println("Found ${issues.size} issue(s) in ${diffFiles.size} files.")

        for (pathAndIssues: FilePathAndIssues in issues) {
            val path = pathAndIssues.first
            val file = diffFiles[path]

            if (file != null) {
                val fileIssues = pathAndIssues.second

                for (issue: Issue in fileIssues) {
                    if (commentSource.shouldComment(file, issue)) {
                        println("Adding a comment for file ${file.filePath}.")
                        pullRequestSource.comment(commentSource.getComment(issue))
                    } else {
                        println("Ignoring violation because it was not added in this pull request.")
                    }
                }
            }
        }

    }
}