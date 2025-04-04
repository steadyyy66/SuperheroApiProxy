package com.kody.com.kody.utils

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object DigestUtils {

    fun Md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun Decrypte(algorithm: String, key: String, iv: String, encryptValue:String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decodedBytes = Base64.getDecoder().decode(encryptValue)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}