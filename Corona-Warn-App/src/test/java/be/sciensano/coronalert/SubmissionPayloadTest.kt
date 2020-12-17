package be.sciensano.coronalert

import KeyExportFormat
import be.sciensano.coronalert.util.PaddingUtil.getPadding
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import org.junit.Assert
import org.junit.Test
import java.util.Calendar

class SubmissionPayloadTest {

    private val key1 = byteArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    )

    @Test
    fun testPayloadSizesAllEquals() {

        val datesRange = (2020 until 2100)

        val sizes = (0 until SubmissionConstants.minKeyCountForSubmission).flatMap { index ->
            datesRange.map { year ->
                val cal = Calendar.getInstance()
                cal.set(year, 1, 1, 0, 0)

                val key = KeyExportFormat.TemporaryExposureKey.newBuilder()
                    .setKeyData(ByteString.copyFrom(key1))
                    .setRollingStartIntervalNumber((cal.time.time / 60 / 10 / 1000).toInt())
                    .setRollingPeriod(144)
                    .setTransmissionRiskLevel(4)
                    .build()

                KeyExportFormat.SubmissionPayload.newBuilder()
                    .addAllKeys(List(index) { key })
                    .addAllVisitedCountries(List(index) { "BE" })
                    .setPadding(getPadding(index))
                    .build().serializedSize
            }
        }

        Assert.assertEquals(
            sizes,
            List(SubmissionConstants.minKeyCountForSubmission * datesRange.count()) { 478 })
    }
}
