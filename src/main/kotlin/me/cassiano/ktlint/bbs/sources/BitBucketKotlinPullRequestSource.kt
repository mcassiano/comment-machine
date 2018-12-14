package me.cassiano.ktlint.bbs.sources

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.lookup
import me.cassiano.ktlint.bbs.core.Comment
import me.cassiano.ktlint.bbs.core.DiffFile
import me.cassiano.ktlint.bbs.core.PullRequestSource
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.StringReader

data class BBSAnchor(
        val line: Int,
        val lineType: String = "ADDED",
        val fileType: String = "TO",
        val path: String
)

data class BBSComment(
        val text: String,
        val anchor: BBSAnchor
)


class BitBucketKotlinPullRequestSource(
        private val okHttpClient: OkHttpClient,
        private val klaxon: Klaxon,
        private val baseUrl: String,
        private val pullRequestId: Int
) : PullRequestSource {

    override fun getDiff(): Map<String, DiffFile> {
        val request = Request.Builder()
                .url(getDiffUrl())
                .get()
                .build()

        val response = okHttpClient.newCall(request).execute()

        if (response.isSuccessful) {
            val jsonResponse = klaxon.parser().parse(StringReader(response.body()?.string() ?: "")) as JsonObject
            val diffs = jsonResponse.array<JsonObject>("diffs")
            val map = mutableMapOf<String, DiffFile>()

            diffs?.value?.forEach { file ->
                val hunks = file.array<JsonObject>("hunks")?.value ?: emptyList<JsonObject>()
                val destination = file
                        .obj("destination")
                        ?.string("toString")

                if (destination != null) {
                    val addedSegmentsList = hunks
                            .map { hunk ->
                                hunk.array<JsonObject>("segments")
                                        ?.filter { segment -> segment.string("type") == "ADDED" }
                            }

                    val finalList = addedSegmentsList.fold(mutableListOf<Int>()) { acc, list ->
                        list?.forEach { acc.addAll(it.lookup<Int>("lines.destination").value) }
                        acc
                    }

                    map[destination] = DiffFile(destination, finalList)
                }
            }

            return map
        }

        throw IllegalStateException("Could not load pull request info: Status code ${response.code()}")
    }

    override fun comment(comment: Comment) {

        val bbsComment = BBSComment(comment.text, BBSAnchor(line = comment.line, path = comment.path))

        val request = Request.Builder()
                .url(getCommentsUrl())
                .post(RequestBody.create(MediaType.get("application/json"), klaxon.toJsonString(bbsComment)))
                .build()

        okHttpClient.newCall(request).execute()
    }


    private fun getDiffUrl() = "$baseUrl/pull-requests/$pullRequestId/diff"
    private fun getCommentsUrl() = "$baseUrl/pull-requests/$pullRequestId/comments"
}