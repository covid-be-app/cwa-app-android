package be.sciensano.coronalert

import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import timber.log.Timber
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Calendar
import java.util.Date
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

class MobileTestId(
    val r0: String,
    val t0: String,
    val r1: String,
    val k: String,
    val onsetSymptomsDate: String?,
) {

    override fun toString(): String {
        return uiCode(registrationToken())
    }

    fun registrationToken(): String {
        return "$r1|$t0"
    }

    companion object {
        private const val INFECTIOUS_DAYS_MINUS = 2

        fun generate(
            symptoms: Date? = null, infoSuffix: String = "TEST REQUEST"
        ): MobileTestId {

            val t0 = calculatet0(symptoms ?: Date())
            Timber.d("t0: %s", t0)

            var r1: String? = null
            var r0: String? = null
            var kEncoded: String? = null
            while (r1 == null) {
                val k = generateK()
                kEncoded = Base64.encodeToString(k.encoded, Base64.NO_WRAP)
                r0 = generateR0()
                val info = makeInfo(r0, t0, infoSuffix)
                r1 = calculateR1(info, k)
            }

            return MobileTestId(r0!!, t0, r1, kEncoded!!, symptoms?.toServerFormat())
        }

        private fun calculatet0(date: Date): String {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, -INFECTIOUS_DAYS_MINUS)
            return (cal.time).toServerFormat()
        }

        fun compactServerDate(date: String): String {
            return date.replace("-", "").slice(2..7)
        }

        fun uiCode(registrationToken: String): String {
            val r1 = registrationToken.split("|")[0]
            val t0 = registrationToken.split("|")[1]

            return uiCode(t0, r1)
        }

        private fun uiCode(t0: String, r1: String): String {
            return "$r1${checksum("${compactServerDate(t0)}$r1")}".chunked(4)
                .joinToString("-")
        }

        private fun checksum(value: String): String {
            return String.format(
                "%02d", BigInteger("97").minus(
                    BigInteger(value).multiply(
                        BigInteger("100")
                    ).mod(BigInteger("97"))
                )
            )
        }

        private fun generateK(): SecretKey {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
            generator.init(128)

            return generator.generateKey()
        }

        private fun generateR0(): String {
            val secureRandom = SecureRandom()
            return List(16) {
                (('a'..'z') + ('A'..'Z') + ('0'..'9'))[secureRandom.nextInt(16)]
            }.joinToString(
                ""
            )
        }

        private fun makeInfo(r0: String, t0: String, suffix: String): String {
            return r0 + t0 + suffix
        }

        private fun calculateR1(info: String, k: SecretKey): String? {
            val hmacSHA256: Mac = Mac.getInstance("hmacSHA256")
            hmacSHA256.init(k)
            val hash = hmacSHA256.doFinal(info.toByteArray(Charsets.UTF_8))

            val b7 = hash.copyOfRange(hash.size - 7, hash.size).toUByteArray()

            val n1 = b7[0].toULong() + (b7[1].toULong() shl 8) + ((b7[2].toULong() and 15u) shl 16)
            val n2 = (b7[2].toULong() shr 4) + (b7[3].toULong() shl 4) + (b7[4].toULong() shl 12)
            val n3 = b7[5].toULong() + ((b7[6].toULong() and 3u) shl 8)

            val n1Mod = n1.toLong() % 1000000L
            val n2Mod = n2.toLong() % 1000000L
            val n3Mod = n3.toLong() % 1000L

            val r1 = String.format("%06d%06d%03d", n1Mod, n2Mod, n3Mod)
            if (r1.toLong() == 0L) {
                return null
            }
            return r1
        }

    }
}
