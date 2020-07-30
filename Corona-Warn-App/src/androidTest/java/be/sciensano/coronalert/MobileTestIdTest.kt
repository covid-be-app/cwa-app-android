package be.sciensano.coronalert

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.spec.SecretKeySpec

@RunWith(AndroidJUnit4::class)
class MobileTestIdTest {

    @Test
    fun testR1Calculation() {
        Assert.assertEquals(
            MobileTestId.calculateR1(
                "RNJ7XO0sP88xextu2020-07-21TEST REQUEST",
                getSecretKeyForB64("bJxx/UPRKwPwdadwJTs76w==")
            ), "402570780892356"
        )

        Assert.assertEquals(
            MobileTestId.calculateR1(
                "2nii5Uwaga2GAsiJ2020-07-21TEST REQUEST",
                getSecretKeyForB64("8FQZ4I4BT66ClgTmnM1Alw==")
            ), "310169445554293"
        )

        Assert.assertEquals(
            MobileTestId.calculateR1(
                "tlA1nDLx0PE0QlVN2020-07-21TEST REQUEST",
                getSecretKeyForB64("j9EWWBZYt9CWsGtTpPNUrg==")
            ), "989250150432575"
        )
    }

    private fun getSecretKeyForB64(value: String): SecretKeySpec {
        val testDecodedK = Base64.decode(value, Base64.DEFAULT)
        return SecretKeySpec(testDecodedK, 0, testDecodedK.size, "AES")
    }

}