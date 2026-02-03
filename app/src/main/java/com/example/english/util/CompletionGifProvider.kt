/*
 * MIT License
 * Copyright (c) 2025 sun6 (Rainshower258)
 * See LICENSE file in the project root for full license information.
 */
package com.example.english.util

import android.content.Context
import kotlin.random.Random

/**
 * 提供完成任务时的随机GIF图片
 */
object CompletionGifProvider {

    private val gifFiles = listOf(
        "240b34a85edf8db1c1fd9e554f23dd54574e749f.gif",
        "2e259258d109b3de8e6c06598abf6c81810a4c9f.gif",
        "43ac9f3df8dcd10095511a10348b4710b8122f99.gif",
        "9db6e71190ef76c6501e191adb16fdfaae51679b.gif",
        "aa700ef41bd5ad6e9f3486dbc7cb39dbb7fd3c99.gif",
        "e756f8dcd100baa1fdffa58a0110b912c9fc2e9f.gif"
    )

    /**
     * 获取随机的完成GIF文件名
     */
    fun getRandomGif(): String {
        return gifFiles[Random.nextInt(gifFiles.size)]
    }

    /**
     * 获取assets文件夹中的GIF文件URI路径
     */
    fun getGifAssetPath(fileName: String): String {
        return "file:///android_asset/$fileName"
    }

    /**
     * 获取随机的完成GIF的完整路径
     */
    fun getRandomGifPath(): String {
        return getGifAssetPath(getRandomGif())
    }
}

