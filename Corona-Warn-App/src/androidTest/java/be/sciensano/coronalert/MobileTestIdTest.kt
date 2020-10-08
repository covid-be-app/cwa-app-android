package be.sciensano.coronalert

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger
import java.util.Calendar
import java.util.Date
import javax.crypto.spec.SecretKeySpec

/**
 * Require at least android P: https://github.com/mockk/mockk/issues/182
 */
@RunWith(AndroidJUnit4::class)
class MobileTestIdTest {

    private lateinit var mobileTestIdFactory: MobileTestId.Companion

    @Before
    fun setUp() {
        mobileTestIdFactory = spyk(MobileTestId, recordPrivateCalls = true)
    }

    @Test
    fun testR1Calculation() {
        every { mobileTestIdFactory["generateK"]() } returns getSecretKeyForB64("enaeEaH/7zxo8/4RUFtidQ==")
        every {
            mobileTestIdFactory["makeInfo"](
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns "ryinAKH0AoVXXLwM2020-09-08TEST REQUEST"

        Assert.assertEquals(
            mobileTestIdFactory.generate(newDate(2020, 9, 8)).toString(),
            "3748-3803-3537-3893-8"
        )
    }

    @Test
    fun testModulo() {
        Assert.assertEquals(modulo97("20071082443971088220685"), 0)

        val mobileTestId = MobileTestId.generate(newDate(2020, 1, 1))
        Assert.assertEquals(
            modulo97(
                "${MobileTestId.compactServerDate(mobileTestId.t0)}${mobileTestId.toString()
                    .replace("-", "")}"
            )
            , 0
        )
    }

    @Test
    fun testGenerateId() {
        val mobileTestId = MobileTestId.generate(Date())

        Assert.assertEquals(
            mobileTestId.r1.length, 15
        )
        Assert.assertEquals(
            mobileTestId.registrationToken().split("|")[0], mobileTestId.r1
        )
        Assert.assertEquals(
            mobileTestId.registrationToken().split("|")[1], Date().toServerFormat()
        )
    }

    private fun getSecretKeyForB64(value: String): SecretKeySpec {
        val testDecodedK = Base64.decode(value, Base64.DEFAULT)
        return SecretKeySpec(testDecodedK, 0, testDecodedK.size, "AES")
    }

    private fun modulo97(value: String): Int {
        return BigInteger(value).mod(BigInteger("97")).toInt()
    }

    private fun newDate(year: Int, month: Int, dayOfMonth: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        // january is 0 in Calendar.MONTH
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calendar.time
    }
}
