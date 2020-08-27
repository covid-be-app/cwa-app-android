package be.sciensano.coronalert.util

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import kotlin.math.max

object PaddingUtil {

    fun getPadding(forKeysCount: Int): ByteString {
        val fakeKeyCount =
            max(
                SubmissionConstants.minKeyCountForSubmission - forKeysCount,
                0
            )
        val paddingCount = (35 * fakeKeyCount).let { if (it > 128) it - 1 else it }

        return ByteString.copyFromUtf8(requestPadding(paddingCount))
    }

    fun requestPadding(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
