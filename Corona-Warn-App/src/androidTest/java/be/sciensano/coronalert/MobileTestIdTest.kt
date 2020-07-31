package be.sciensano.coronalert

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.spyk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.spec.SecretKeySpec

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
                any<String>()
            )
        } returns "RNJ7XO0sP88xextu2020-07-21TEST REQUEST"
        Assert.assertEquals(
            mobileTestIdFactory.generate().id, "402570780892356"
        )
        Assert.assertEquals(
            mobileTestIdFactory.generate().checksum, "96"
        )

        every { mobileTestIdFactory["generateK"]() } returns getSecretKeyForB64("8FQZ4I4BT66ClgTmnM1Alw==")
        every {
            mobileTestIdFactory["makeInfo"](
                any<String>(),
                any<String>()
            )
        } returns "2nii5Uwaga2GAsiJ2020-07-21TEST REQUEST"
        Assert.assertEquals(
            mobileTestIdFactory.generate().id, "310169445554293"
        )
        Assert.assertEquals(
            mobileTestIdFactory.generate().checksum, "22"
        )

        every { mobileTestIdFactory["generateK"]() } returns getSecretKeyForB64("j9EWWBZYt9CWsGtTpPNUrg==")
        every {
            mobileTestIdFactory["makeInfo"](
                any<String>(),
                any<String>()
            )
        } returns "tlA1nDLx0PE0QlVN2020-07-21TEST REQUEST"
        Assert.assertEquals(
            mobileTestIdFactory.generate().id, "989250150432575"
        )
        Assert.assertEquals(
            mobileTestIdFactory.generate().checksum, "84"
        )
    }

    private fun getSecretKeyForB64(value: String): SecretKeySpec {
        val testDecodedK = Base64.decode(value, Base64.DEFAULT)
        return SecretKeySpec(testDecodedK, 0, testDecodedK.size, "AES")
    }

}