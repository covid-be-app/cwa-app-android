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
        every { mobileTestIdFactory["generateK"]() } returns getSecretKeyForB64("bJxx/UPRKwPwdadwJTs76w==")
        every {
            mobileTestIdFactory["makeInfo"](
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns "RNJ7XO0sP88xextu2020-07-21TEST REQUEST"

        Assert.assertEquals(
            mobileTestIdFactory.generate(newDate(2020, 7, 21)).toString(),
            "40257-07808-92356-96"
        )

        every { mobileTestIdFactory["generateK"]() } returns getSecretKeyForB64("8FQZ4I4BT66ClgTmnM1Alw==")
        every {
            mobileTestIdFactory["makeInfo"](
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns "2nii5Uwaga2GAsiJ2020-07-21TEST REQUEST"

        Assert.assertEquals(
            mobileTestIdFactory.generate(newDate(2020, 7, 21)).toString(),
            "31016-94455-54293-22"
        )

        every { mobileTestIdFactory["generateK"]() } returns getSecretKeyForB64("j9EWWBZYt9CWsGtTpPNUrg==")
        every {
            mobileTestIdFactory["makeInfo"](
                any<String>(),
                any<String>(),
                any<String>()
            )
        } returns "tlA1nDLx0PE0QlVN2020-07-21TEST REQUEST"

        Assert.assertEquals(
            mobileTestIdFactory.generate(newDate(2020, 7, 21)).toString(),
            "98925-01504-32575-84"
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

    private fun newDate(year: Int, month: Int, dayOfMonth: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        // january is 0 in Calendar.MONTH
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calendar.time
    }
}
