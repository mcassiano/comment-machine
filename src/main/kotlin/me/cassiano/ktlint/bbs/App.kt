package me.cassiano.ktlint.bbs

import com.beust.klaxon.Klaxon
import me.cassiano.ktlint.bbs.core.CommentMachine
import me.cassiano.ktlint.bbs.sources.AndroidLintIssueSource
import me.cassiano.ktlint.bbs.sources.BitBucketKotlinPullRequestSource
import me.cassiano.ktlint.bbs.sources.BitBucketServerCommentSource
import me.cassiano.ktlint.bbs.sources.KtlintIssueSource
import okhttp3.*

class BasicAuthentication(
        private val username: String,
        private val password: String
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request = response
            .request()
            .newBuilder()
            .header("Authorization", Credentials.basic(username, password))
            .build()
}

fun main(args: Array<String>) {

    if (args.size < 5) {
        println("Usage: comment-machine <pull-request-id> <base-url> <repository-dir> <username> <password>")
        return
    }

    val klaxon = Klaxon()
    val pullRequestId = args[0].toInt()
    val baseUrl = args[1]
    val workingDir = args[2]
    val username = args[3]
    val password = args[4]

    val httpClient = OkHttpClient
            .Builder()
            .authenticator(BasicAuthentication(username, password))
            .build()

    val pullRequestSource = BitBucketKotlinPullRequestSource(
            httpClient,
            klaxon,
            baseUrl,
            pullRequestId
    )

    val androidLint = AndroidLintIssueSource(workingDir)
    val ktLint = KtlintIssueSource(klaxon, workingDir)

    val commentSource = BitBucketServerCommentSource()

    val machine = CommentMachine(pullRequestSource, listOf(androidLint, ktLint), commentSource)
    machine.run()

    println("All done. Yay!")
}

