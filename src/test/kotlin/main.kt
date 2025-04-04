fun main() {
    val original = "Hello, Kotlin Backend!"
    val encrypted = AESCipher.encrypt(original)
    val decrypted = AESCipher.decrypt(encrypted)

    println("原始: $original")
    println("加密后: $encrypted")
    println("解密后: $decrypted")
}
