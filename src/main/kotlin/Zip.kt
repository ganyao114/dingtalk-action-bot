package com.swift

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File

fun zipFiles(paths: Array<String>, password : String): String? {
    try {
        // 创建一个 ZipFile 对象
        val zipFile = ZipFile("compressed_$password.zip", password.toCharArray())

        // 创建一个 ZipParameters 对象，设置压缩方法和加密方法
        val parameters = ZipParameters()
        parameters.compressionMethod = CompressionMethod.DEFLATE
        parameters.isEncryptFiles = true
        parameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD

        // 添加文件到压缩文件
        for (path in paths) {
            val file = File(path)
            if (file.isDirectory) {
                zipFile.addFolder(file, parameters)
            } else {
                zipFile.addFile(file, parameters)
            }
        }
        zipFile.close()
        return zipFile.file.absolutePath
    } catch (e : Exception) {
        e.printStackTrace()
        return null
    }
}