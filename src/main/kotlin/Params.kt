package com.swift
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.io.File
import java.lang.System

class Params(json: String?) {
    @SerializedName("project")
    val project: String
    @SerializedName("access_token")
    val accessToken: String
    @SerializedName("access_secure")
    val accessSecure: String
    @SerializedName("success")
    val success: Boolean
    @SerializedName("action_id")
    val actionId: String
    @SerializedName("commit_sha")
    val commitSha: String?
    @SerializedName("commit_auth")
    val commitAuth: String?
    @SerializedName("commit_info")
    val commitInfo: String?
    @SerializedName("action_ref")
    val actionRef: String?
    @SerializedName("pull_request")
    val pullRequest: String?
    @SerializedName("release")
    val release: String?
    @SerializedName("release_info")
    var releaseInfo: String?
    @SerializedName("files")
    val files: Array<String>?
    @SerializedName("users")
    val users: Array<String>?
    @SerializedName("oss_api_key")
    val ossKey: String?
    @SerializedName("oss_api_sec")
    val ossSec: String?
    @SerializedName("output_password")
    val outputPassword: String?

    init {
        if (json.isNullOrEmpty()) {
            project = System.getenv("GITHUB_REPOSITORY")
            actionId = System.getenv("GITHUB_RUN_ID")
            actionRef = System.getenv("GITHUB_REF")
            accessToken = System.getenv("DING_ACCESS_TOKEN")
            accessSecure = System.getenv("DING_ACCESS_SECRET")
            success = System.getenv().containsKey("BUILD_SUCCESS") && System.getenv("BUILD_SUCCESS").equals("true")
            commitSha = System.getenv("GITHUB_SHA")
            var commitInfoJson = System.getenv("COMMIT_INFO")
            if (commitInfoJson.isNullOrEmpty()) {
                val file = File("COMMIT_INFO.txt")
                if (file.exists()) {
                    commitInfoJson = file.readText()
                }
            }
            if (!commitInfoJson.isNullOrEmpty()) {
                val gson = Gson()
                val jsonObject = gson.fromJson(commitInfoJson, JsonObject::class.java)
                val commit = jsonObject.getAsJsonObject("commit")
                commitInfo = commit.get("message").asString
                commitAuth = commit.getAsJsonObject("author").get("name").asString
            } else {
                commitInfo = null
                commitAuth = null
            }

            val prNumber = System.getenv("PR_NUMBER")

            if (!prNumber.isNullOrEmpty()) {
                var prCommits: String? = "null"
                var prCommitsJson = System.getenv("PR_COMMITS")
                if (prCommitsJson.isNullOrEmpty()) {
                    val file = File("PR_COMMITS.txt")
                    if (file.exists()) {
                        prCommitsJson = file.readText()
                    }
                }
                prCommitsJson?.let {
                    val gson = Gson()
                    try {
                        val jsonArray = gson.fromJson(commitInfoJson, JsonArray::class.java)
                        var index = 1
                        val content = StringBuilder()
                        for (jsonElement in jsonArray) {
                            val commit = jsonElement.getAsJsonObject().getAsJsonObject("commit")
                            val info = commit.get("message").asString
                            val url = jsonElement.getAsJsonObject().get("html_url").asString
                            content.append("    - $index. [$info]($url)")
                            index++
                        }
                        prCommits = content.toString()
                    } catch (e : Exception) {
                        prCommits = "暂不支持"
                    }
                }

                val prTitle = System.getenv("PR_TITLE")
                val prAuthor = System.getenv("PR_AUTHOR")
                var prBody = System.getenv("PR_BODY")
                if (prBody.isNullOrEmpty()) {
                    val file = File("PR_BODY.txt")
                    if (file.exists()) {
                        prBody = file.readText()
                    }
                }
                pullRequest = "- PR [#${prNumber}](https://github.com/$project/pull/$prNumber) [@${prAuthor}](https://github.com/$prAuthor)\n " +
                        "- PR 标题：$prTitle\n" +
                        "- PR 内容：$prBody\n" +
                        "- 提交记录：\n" +
                        "   - $prCommits\n"
            } else {
                pullRequest = null
            }

            release = System.getenv("RELEASE_TAG")
            releaseInfo = System.getenv("RELEASE_BODY")
            if (releaseInfo.isNullOrEmpty()) {
                val file = File("RELEASE_BODY.txt")
                if (file.exists()) {
                    releaseInfo = file.readText()
                }
            }

            files = System.getenv("BUILD_OUTPUT_FILES")?.split(",".toRegex())?.toTypedArray()
            users = System.getenv("DING_USERS")?.split(",".toRegex())?.toTypedArray()
            ossKey = System.getenv("OSS_API_KEY")
            ossSec = System.getenv("OSS_API_SEC")
            outputPassword = System.getenv("OUTPUT_PASSWORD")
        } else {
            val gson = Gson()
            val params = gson.fromJson(json, Params::class.java)
            project = params.project
            accessToken = params.accessToken
            accessSecure = params.accessSecure
            success = params.success
            actionId = params.actionId
            actionRef = params.actionRef
            commitSha = params.commitSha
            commitInfo = params.commitInfo
            commitAuth = params.commitAuth
            pullRequest = params.pullRequest
            release = params.release
            releaseInfo = params.releaseInfo
            files = params.files
            users = params.users
            ossKey = params.ossKey
            ossSec = params.ossSec
            outputPassword = params.outputPassword
        }
    }
}