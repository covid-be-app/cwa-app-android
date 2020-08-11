package be.sciensano.coronalert.service.diagnosiskey

/**
 * The Diagnosis Key constants are used inside the DiagnosisKeyService
 *
 * @see DiagnosisKeyService
 */
object DiagnosisKeyConstants {
    /** version resource variable for REST-like Service Calls */
    private const val VERSION = "version"

    /** diagnosis keys resource variable for REST-like Service Calls */
    private const val DIAGNOSIS_KEYS = "diagnosis-keys"

    /** resource variables but non-static context */
    private var CURRENT_VERSION = "v1"

    /** Submission URL built from CDN URL's and REST resources */
    private var VERSIONED_SUBMISSION_CDN_URL = "$VERSION/$CURRENT_VERSION"

    /** Diagnosis key Submission URL built from CDN URL's and REST resources */
    val DIAGNOSIS_KEYS_SUBMISSION_URL = "$VERSIONED_SUBMISSION_CDN_URL/$DIAGNOSIS_KEYS"

}
