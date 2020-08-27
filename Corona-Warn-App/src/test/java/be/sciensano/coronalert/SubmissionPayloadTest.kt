package be.sciensano.coronalert

import KeyExportFormat
import be.sciensano.coronalert.util.PaddingUtil.getPadding
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import org.junit.Assert
import org.junit.Test
import java.util.Date

class SubmissionPayloadTest {

    private val key1 = byteArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    )

    @Test
    fun testPayloadSizesAllEquals() {
        val key = KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(ByteString.copyFrom(key1))
            .setRollingStartIntervalNumber((Date().time / 60 / 10 / 1000).toInt())
            .setRollingPeriod(144)
            .setTransmissionRiskLevel(4)
            .build()

        val sizes = (0..SubmissionConstants.minKeyCountForSubmission).map { index ->
            KeyExportFormat.SubmissionPayload.newBuilder()
                .addAllKeys(List(index) { key })
                .addAllCountries(List(index) { "BEL" })
                .setPadding(getPadding(index))
                .build().serializedSize

        }

        Assert.assertEquals(sizes.distinct().size, 1)
    }

}
