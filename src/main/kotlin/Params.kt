package com.swift
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
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
    val releaseInfo: String?
    @SerializedName("files")
    val files: Array<String>?
    @SerializedName("users")
    val users: Array<String>?
    @SerializedName("oss_api_key")
    val ossKey: String?
    @SerializedName("oss_api_sec")
    val ossSec: String?

    init {
        if (json.isNullOrEmpty()) {
            project = System.getenv("GITHUB_REPOSITORY")
            actionId = System.getenv("GITHUB_RUN_ID")
            actionRef = System.getenv("GITHUB_REF")
            accessToken = System.getenv("DING_ACCESS_TOKEN")
            accessSecure = System.getenv("DING_ACCESS_SECRET")
            success = System.getenv().containsKey("BUILD_SUCCESS") && System.getenv("BUILD_SUCCESS").equals("1")
            commitSha = System.getenv("GITHUB_SHA")
            val commitInfoJson = System.getenv("COMMIT_INFO")

            if (!commitInfoJson.isNullOrEmpty()) {
                val gson = Gson()
                val jsonObject = gson.fromJson(commitInfoJson, JsonObject::class.java)
                val commit = jsonObject.getAsJsonObject("commit")
                commitInfo = commit.get("message").asString
                commitAuth = commit.getAsJsonObject("committer").get("name").asString
            } else {
                commitInfo = null
                commitAuth = null
            }

            val prNumber = System.getenv("PR_NUMBER")

            if (!prNumber.isNullOrEmpty()) {
                val prTitle = System.getenv("PR_TITLE")
                val prAuthor = System.getenv("PR_AUTHOR")
                val prCommits = System.getenv("PR_COMMITS")
                val prBody = System.getenv("PR_BODY")
                pullRequest = "- PR [#${prNumber}](https://github.com/$project/pull/$prNumber) [@${prAuthor}](https://github.com/$prAuthor)\n " +
                        "- PR 标题：$prTitle\n" +
                        "- PR 内容：$prBody\n" +
                        "- 提交记录：\n" +
                        "$prCommits\n"
            } else {
                pullRequest = null
            }

            release = System.getenv("RELEASE_TAG")
            releaseInfo = System.getenv("RELEASE_BODY")

            files = System.getenv("BUILD_OUTPUT_FILES")?.split(",".toRegex())?.toTypedArray()
            users = System.getenv("DING_USERS")?.split(",".toRegex())?.toTypedArray()
            ossKey = System.getenv("OSS_API_KEY")
            ossSec = System.getenv("OSS_API_SEC")
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
        }
    }
}