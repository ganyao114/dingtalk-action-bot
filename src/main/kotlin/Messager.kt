package com.swift

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.common.auth.CredentialsProviderFactory
import com.dingtalk.api.DefaultDingTalkClient
import com.dingtalk.api.DingTalkClient
import com.dingtalk.api.request.OapiRobotSendRequest
import com.dingtalk.api.response.OapiRobotSendResponse
import java.io.File
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Messager(private val params: Params) {

    fun send() : Boolean {
        if (params.success) {
            return sendSuccess()
        } else {
            return sendFailure()
        }
    }

    private fun sendSuccess(): Boolean {
        var uploadedFile : String? = null
        /**
         * 压缩上传产物
         */
        val filePassword = generateRandomPassword(16)
        params.files?.let {
            zipFiles(it, filePassword)?.let {
                uploadedFile = uploadFile(it)
            }
        }

        /**
         * 发送消息
         */
        val req = OapiRobotSendRequest()
        //定义文本内容
        val content: StringBuilder = StringBuilder("## [${params.project}](https://github.com/${params.project})\n")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formatted = LocalDateTime.now().format(formatter)
        content.append("### 编译信息\n")
        content.append("- **<font color=#00C000 size=4>构建成功</font>**\n")
        content.append("- 构建日期：$formatted\n")
        params.actionRef?.let {
            content.append("- 触发条件：${params.actionRef}\n")
        }
        content.append("- Action 链接：[#${params.actionId}](https://github.com/${params.project}/actions/runs/${params.actionId})\n")

        if (!params.release.isNullOrEmpty()) {
            content.append("### 正式发布\n")
            content.append("- 版本：${params.release}\n")
            content.append("- ${params.releaseInfo}\n")
        } else if (!params.pullRequest.isNullOrEmpty()) {
            content.append("### PR 信息\n")
            content.append("${params.pullRequest}\n")
        } else {
            content.append("### Commit 信息\n")
            content.append("- [提交链接](https://github.com/${params.project}/commit/${params.commitSha})\n")
            content.append("- 提交者：${params.commitAuth}\n")
            content.append("- 提交信息：\n")
            content.append("    - ${params.commitInfo}\n")
        }

        uploadedFile?.let {
            content.append("### 生成产物\n");
            content.append("- [下载链接](${uploadedFile})\n");
            content.append("- 解压密码：${filePassword}\n");
        }

        params.users?.let {
            //定义 @ 对象
            val at = OapiRobotSendRequest.At()
            at.atUserIds = params.users.toList()
            //设置消息类型
            req.setAt(at)
        }

        val text = OapiRobotSendRequest.Markdown()
        text.title = "编译结果"
        text.text = content.toString()
        req.msgtype = "markdown"
        req.setMarkdown(text)
        val rsp = getDingTalkClient().execute(req, params.accessToken)
        return rsp.isSuccess
    }

    private fun sendFailure(): Boolean {
        /**
         * 发送消息
         */
        val req = OapiRobotSendRequest()
        //定义文本内容
        val content: StringBuilder = StringBuilder("## [${params.project}](https://github.com/${params.project})\n")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formatted = LocalDateTime.now().format(formatter)
        content.append("### 编译信息\n")
        content.append("- **<font color=#FF0000 size=4>构建失败</font>**\n")
        content.append("- 构建日期：$formatted\n")
        params.actionRef?.let {
            content.append("- 触发条件：${params.actionRef}\n")
        }
        content.append("- Action 链接：[#${params.actionId}](https://github.com/${params.project}/actions/runs/${params.actionId})\n")

        if (!params.release.isNullOrEmpty()) {
            content.append("### 正式发布\n")
            content.append("- 版本：${params.release}\n")
            content.append("${params.releaseInfo}\n")
        } else if (!params.pullRequest.isNullOrEmpty()) {
            content.append("### PR 信息\n")
            content.append("${params.pullRequest}\n")
        } else {
            content.append("### Commit 信息\n")
            content.append("- [提交链接](https://github.com/${params.project}/commit/${params.commitSha})\n")
            content.append("- 提交者：${params.commitAuth}\n")
            content.append("- 提交信息：\n")
            content.append("    - ${params.commitInfo}\n")
        }

        params.users?.let {
            //定义 @ 对象
            val at = OapiRobotSendRequest.At()
            at.atUserIds = params.users.toList()
            //设置消息类型
            req.setAt(at)
        }

        val text = OapiRobotSendRequest.Markdown()
        text.title = "编译结果"
        text.text = content.toString()
        req.msgtype = "markdown"
        req.setMarkdown(text)
        val rsp = getDingTalkClient().execute(req, params.accessToken)
        return rsp.isSuccess
    }

    private fun getDingTalkClient(): DingTalkClient {
        val timestamp = System.currentTimeMillis()
        val stringToSign = """
         $timestamp
         ${params.accessSecure}
         """.trimIndent()
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(params.accessSecure.toByteArray(charset("UTF-8")), "HmacSHA256"))
        val signData = mac.doFinal(stringToSign.toByteArray(charset("UTF-8")))
        val sign = URLEncoder.encode(String(Base64.getEncoder().encode(signData)), "UTF-8")

        return DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?sign=$sign&timestamp=$timestamp")
    }

    private fun uploadFile(path : String?) : String? {
        if (path.isNullOrEmpty()) {
            return null
        }
        val credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(
            params.ossKey,
            params.ossSec
        )

        val endpoint = "oss-cn-guangzhou.aliyuncs.com"
        val bucketName = "oss-ci-ci"

        // 创建OSSClient实例。
        val ossClient = OSSClientBuilder().build(endpoint, credentialsProvider)
        // 上传文件

        val fileName = "build_${params.project}_${params.actionId}.zip".replace("/", "_")
        ossClient.putObject(bucketName, fileName, File(path))
        // 获取文件的公开下载 URL
        val url = ossClient.generatePresignedUrl(bucketName, fileName, Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 30)).toString()
        ossClient.shutdown();
        
        return url
    }

    private fun generateRandomPassword(length: Int): String {
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + "!@#$%^&*()-_=+<>".toList()
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}