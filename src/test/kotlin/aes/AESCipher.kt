package aes

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCipher {
    private const val algorithm = "AES/CBC/PKCS5Padding"
    private const val key = "1234567890abcdef" // 16字符密钥 (128-bit)
    private const val iv = "abcdef1234567890" // 16字符 IV

    private val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
    private val ivParameterSpec = IvParameterSpec(iv.toByteArray())

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(encryptedText: String): String {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decodedBytes = Base64.getDecoder().decode(encryptedText)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
