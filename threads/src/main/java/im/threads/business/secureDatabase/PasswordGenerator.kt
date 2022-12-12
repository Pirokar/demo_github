package im.threads.business.secureDatabase

import java.security.SecureRandom

/**
 * Генератор паролей
 */
class PasswordGenerator {
    private val letters = "abcdefghijklmnopqrstuvwxyz"
    private val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers = "0123456789"

    /**
     * Генерирует и возвращает пароль, состоящий из прописных и строчных букв + цифры
     */
    fun generate(): String {
        val result = "$letters$uppercaseLetters$numbers"
        var i = 0
        val length = 16

        val rnd = SecureRandom.getInstance("SHA1PRNG")
        val sb = StringBuilder(length)

        while (i < length) {
            val randomInt = rnd.nextInt(result.length)
            sb.append(result[randomInt])
            i++
        }

        return sb.toString()
    }
}
