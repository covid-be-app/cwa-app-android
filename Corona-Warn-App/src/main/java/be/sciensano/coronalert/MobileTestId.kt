package be.sciensano.coronalert

import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import timber.log.Timber
import java.util.Date
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

class MobileTestId(
    val r0: String,
    val t0: String,
    val r1: String,
    val k: String
) {

    override fun toString(): String {
        return "$r1${checksum(r1.toLong())}".chunked(4)
            .joinToString("-")
    }

    fun registrationToken(): String {
        return "$r1|$t0"
    }


    private fun checksum(value: Long): String {
        return String.format("%02d", 97 - (value * 100 % 97))
    }

    private fun compactT0(): String {
        return t0.replace("-", "").slice(2..7)
    }

    companion object {
        fun generate(
            date: Date, infoSuffix: String = ""
        ): MobileTestId {
            val t0 = date.toServerFormat()
            Timber.d("t0: %s", t0)

            var r1: String? = null
            var r0: String? = null
            var kEncoded: String? = null
            while (r1 == null) {
                val k = generateK()
                kEncoded = Base64.encodeToString(k.encoded, Base64.NO_WRAP)
                Timber.d("k: %s", kEncoded)
                r0 = generateR0()
                Timber.d("r0: %s", r0)
                val info = makeInfo(r0, t0, infoSuffix)
                Timber.d("info: %s", info)
                r1 = calculateR1(info, k)
                Timber.d("r1: %s", r1)

            }

            return MobileTestId(r0!!, t0, r1, kEncoded!!)
        }

        private fun generateK(): SecretKey {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
            generator.init(128)

            return generator.generateKey()
        }

        private fun generateR0(): String {
            return List(16) { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        }

        private fun makeInfo(r0: String, t0: String, suffix: String = ""): String {
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
