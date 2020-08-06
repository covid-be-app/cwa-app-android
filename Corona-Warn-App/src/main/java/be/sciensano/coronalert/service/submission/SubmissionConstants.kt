package be.sciensano.coronalert.service.submission

import de.rki.coronawarnapp.service.submission.SubmissionConstants as DeSubmissionConstants

object SubmissionConstants {
    val TEST_RESULT_URL = "${DeSubmissionConstants.TEST_RESULT_URL}/poll"
    val TEST_RESULT_ACK_URL = "${DeSubmissionConstants.TEST_RESULT_URL}/ack"

}