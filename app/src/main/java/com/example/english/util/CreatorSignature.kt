package com.example.english.util

import android.util.Base64
import java.security.MessageDigest

/**
 * 创作者签名工具类
 * 包含多种编码方式的创作者标识
 *
 * 🥚 Easter Egg #1: 加密的创作者签名
 *
 * @author sun6
 * @since 2025-11-13
 */
object CreatorSignature {

    // Base64编码: "made_by_sun6"
    private const val SIGNATURE_BASE64 = "bWFkZV9ieV9zdW42"

    // ROT13编码: "made by sun6"
    private const val SIGNATURE_ROT13 = "znqr ol fha6"

    // 反转 + Base64: "6nus_yb_edam" (反向的 "made_by_sun6")
    private const val SIGNATURE_REVERSED_BASE64 = "NnVudV95Yl9lZGFt"

    // 十六进制: "made_by_sun6"
    private const val SIGNATURE_HEX = "6d6164655f62795f73756e36"

    // 创作者GitHub用户名 (Base64)
    private const val GITHUB_USERNAME_BASE64 = "UmFpbnNob3dlcjI1OA=="

    // 项目创建时间戳 (2025-11-13的毫秒时间戳)
    private const val PROJECT_BIRTH_TIMESTAMP = 1731456000000L // 2025-11-13 00:00:00 UTC

    /**
     * 解码Base64签名
     */
    fun decodeBase64Signature(): String {
        return String(Base64.decode(SIGNATURE_BASE64, Base64.DEFAULT))
    }

    /**
     * 解码ROT13签名
     */
    fun decodeROT13Signature(): String {
        return rot13Decode(SIGNATURE_ROT13)
    }

    /**
     * 解码反转的Base64签名
     */
    fun decodeReversedBase64Signature(): String {
        val decoded = String(Base64.decode(SIGNATURE_REVERSED_BASE64, Base64.DEFAULT))
        return decoded.reversed()
    }

    /**
     * 解码十六进制签名
     */
    fun decodeHexSignature(): String {
        return hexDecode(SIGNATURE_HEX)
    }

    /**
     * 获取GitHub用户名
     */
    fun getGitHubUsername(): String {
        return String(Base64.decode(GITHUB_USERNAME_BASE64, Base64.DEFAULT))
    }

    /**
     * 获取项目创建日期
     */
    fun getProjectBirthDate(): Long {
        return PROJECT_BIRTH_TIMESTAMP
    }

    /**
     * 验证创作者签名
     * @return true 如果所有签名都正确解码
     */
    fun verify(): Boolean {
        val expectedSignature = "made_by_sun6"
        val expectedGitHub = "Rainshower258"

        return decodeBase64Signature() == expectedSignature &&
                decodeROT13Signature().replace(" ", "_") == expectedSignature &&
                decodeReversedBase64Signature() == expectedSignature &&
                decodeHexSignature() == expectedSignature &&
                getGitHubUsername() == expectedGitHub
    }

    /**
     * 获取完整的创作者信息
     */
    fun getCreatorInfo(): CreatorInfo {
        return CreatorInfo(
            name = "sun6",
            github = getGitHubUsername(),
            signature = decodeBase64Signature(),
            projectBirthDate = PROJECT_BIRTH_TIMESTAMP,
            version = "1.0"
        )
    }

    /**
     * 生成创作者指纹（用于验证）
     */
    fun generateFingerprint(): String {
        val input = "${decodeBase64Signature()}_${getGitHubUsername()}_$PROJECT_BIRTH_TIMESTAMP"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    // ROT13 解码函数
    private fun rot13Decode(input: String): String {
        return input.map { char ->
            when {
                char in 'a'..'z' -> ((char - 'a' + 13) % 26 + 'a'.code).toChar()
                char in 'A'..'Z' -> ((char - 'A' + 13) % 26 + 'A'.code).toChar()
                else -> char
            }
        }.joinToString("")
    }

    // 十六进制解码函数
    private fun hexDecode(hex: String): String {
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
            .toString(Charsets.UTF_8)
    }
}

/**
 * 创作者信息数据类
 */
data class CreatorInfo(
    val name: String,
    val github: String,
    val signature: String,
    val projectBirthDate: Long,
    val version: String
)

