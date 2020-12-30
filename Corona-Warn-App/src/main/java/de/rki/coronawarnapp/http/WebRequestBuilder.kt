/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.http

import KeyExportFormat
import be.sciensano.coronalert.MobileTestId
import be.sciensano.coronalert.http.requests.TestResultRequest
import be.sciensano.coronalert.http.responses.TestResultResponse
import be.sciensano.coronalert.http.service.DynamicTextsService
import be.sciensano.coronalert.http.service.StatisticsService
import be.sciensano.coronalert.util.PaddingUtil.getPadding
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.http.requests.RegistrationRequest
import de.rki.coronawarnapp.http.requests.RegistrationTokenRequest
import de.rki.coronawarnapp.http.requests.TanRequestBody
import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import de.rki.coronawarnapp.util.ZipHelper.unzip
import de.rki.coronawarnapp.util.security.HashHelper
import de.rki.coronawarnapp.util.security.VerificationKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.math.max
import be.sciensano.coronalert.http.service.SubmissionService as BeSubmissionService
import be.sciensano.coronalert.http.service.VerificationService as BeVerificationService
import be.sciensano.coronalert.service.diagnosiskey.DiagnosisKeyConstants as BeDiagnosisKeyConstants
import be.sciensano.coronalert.service.submission.SubmissionConstants as BeSubmissionConstants

class WebRequestBuilder(
    private val distributionService: DistributionService,
    private val verificationService: VerificationService,
    private val beVerificationService: BeVerificationService,
    private val submissionService: SubmissionService,
    private val beSubmissionService: BeSubmissionService,
    private val statisticsService: StatisticsService,
    private val dynamicTextsService: DynamicTextsService,
    private val verificationKeys: VerificationKeys
) {
    companion object {
        private val TAG: String? = WebRequestBuilder::class.simpleName
        private const val EXPORT_BINARY_FILE_NAME = "export.bin"
        private const val EXPORT_SIGNATURE_FILE_NAME = "export.sig"

        @Volatile
        private var instance: WebRequestBuilder? = null

        fun getInstance(): WebRequestBuilder {
            return instance ?: synchronized(this) {
                instance ?: buildWebRequestBuilder().also { instance = it }
            }
        }

        private fun buildWebRequestBuilder(): WebRequestBuilder {
            val serviceFactory = ServiceFactory()
            return WebRequestBuilder(
                serviceFactory.distributionService(),
                serviceFactory.verificationService(),
                serviceFactory.beVerificationService(),
                serviceFactory.submissionService(),
                serviceFactory.beSubmissionService(),
                serviceFactory.statisticsService(),
                serviceFactory.dynamicTextsService(),
                VerificationKeys()
            )
        }
    }

    suspend fun asyncGetRegionIndex(): List<String> = withContext(Dispatchers.IO) {
        return@withContext distributionService
            .getRegionIndex(DiagnosisKeyConstants.AVAILABLE_REGION_URL).toList()
    }

    suspend fun asyncGetDateIndex(region: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext distributionService
            .getDateIndex(DiagnosisKeyConstants.availableDatesForRegionUrl(region)).toList()
    }

    suspend fun asyncGetHourIndex(region: String, day: Date): List<String> =
        withContext(Dispatchers.IO) {
            return@withContext distributionService
                .getHourIndex(
                    DiagnosisKeyConstants.AVAILABLE_DATES_URL +
                            "/${day.toServerFormat()}/${DiagnosisKeyConstants.HOUR}"
                )
                .toList()
        }

    /**
     * Retrieves Key Files from the Server based on a URL
     *
     * @param url the given URL
     */
    suspend fun asyncGetKeyFilesFromServer(
        url: String
    ): File = withContext(Dispatchers.IO) {
        val fileName = "${UUID.nameUUIDFromBytes(url.toByteArray())}.zip"
        val file = File(FileStorageHelper.keyExportDirectory, fileName)
        file.outputStream().use {
            Timber.v("Added $url to queue.")
            distributionService.getKeyFiles(url).byteStream().copyTo(it, DEFAULT_BUFFER_SIZE)
            Timber.v("key file request successful.")
        }
        return@withContext file
    }

    suspend fun asyncGetApplicationConfigurationFromServer(): ApplicationConfiguration =
        withContext(Dispatchers.IO) {
            var exportBinary: ByteArray? = null
            var exportSignature: ByteArray? = null

            distributionService.getApplicationConfiguration(
                DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL
            ).byteStream().unzip { entry, entryContent ->
                if (entry.name == EXPORT_BINARY_FILE_NAME) exportBinary = entryContent.copyOf()
                if (entry.name == EXPORT_SIGNATURE_FILE_NAME) exportSignature =
                    entryContent.copyOf()
            }
            if (exportBinary == null || exportSignature == null) {
                throw ApplicationConfigurationInvalidException()
            }

            if (verificationKeys.hasInvalidSignature(exportBinary, exportSignature)) {
                throw ApplicationConfigurationCorruptException()
            }

            try {
                return@withContext ApplicationConfiguration.parseFrom(exportBinary)
            } catch (e: InvalidProtocolBufferException) {
                throw ApplicationConfigurationInvalidException()
            }
        }

    suspend fun asyncGetRegistrationToken(
        key: String,
        keyType: KeyType
    ): String = withContext(Dispatchers.IO) {
        val keyStr = if (keyType == KeyType.GUID) {
            HashHelper.hash256(key)
        } else {
            key
        }

        val paddingLength = when (keyType) {
            KeyType.GUID -> SubmissionConstants.PADDING_LENGTH_BODY_REGISTRATION_TOKEN_GUID
            KeyType.TELETAN -> SubmissionConstants.PADDING_LENGTH_BODY_REGISTRATION_TOKEN_TELETAN
        }

        verificationService.getRegistrationToken(
            SubmissionConstants.REGISTRATION_TOKEN_URL,
            "0",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_REGISTRATION_TOKEN),
            RegistrationTokenRequest(keyType.name, keyStr, requestPadding(paddingLength))
        ).registrationToken
    }

    suspend fun asyncGetTestResult(
        registrationToken: String
    ): Int = withContext(Dispatchers.IO) {
        verificationService.getTestResult(
            SubmissionConstants.TEST_RESULT_URL,
            "0",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TEST_RESULT),
            RegistrationRequest(
                registrationToken,
                requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TEST_RESULT)
            )
        ).testResult
    }

    suspend fun asyncGetTan(
        registrationToken: String
    ): String = withContext(Dispatchers.IO) {
        verificationService.getTAN(
            SubmissionConstants.TAN_REQUEST_URL,
            "0",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TAN),
            TanRequestBody(
                registrationToken,
                requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TAN)
            )
        ).tan
    }

    suspend fun asyncFakeVerification() = withContext(Dispatchers.IO) {
        verificationService.getTAN(
            SubmissionConstants.TAN_REQUEST_URL,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_TAN),
            TanRequestBody(
                registrationToken = SubmissionConstants.DUMMY_REGISTRATION_TOKEN,
                requestPadding = requestPadding(SubmissionConstants.PADDING_LENGTH_BODY_TAN_FAKE)
            )
        )
    }

    suspend fun asyncSubmitKeysToServer(
        authCode: String,
        keyList: List<KeyExportFormat.TemporaryExposureKey>
    ) = withContext(Dispatchers.IO) {
        Timber.d("Writing ${keyList.size} Keys to the Submission Payload.")

        val randomAdditions = 0 // prepare for random addition of keys
        val fakeKeyCount =
            max(SubmissionConstants.minKeyCountForSubmission + randomAdditions - keyList.size, 0)
        val fakeKeyPadding = requestPadding(SubmissionConstants.fakeKeySize * fakeKeyCount)

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addAllKeys(keyList)
            .setRequestPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .build()
        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            authCode,
            "0",
            SubmissionConstants.EMPTY_HEADER,
            submissionPayload
        )
        return@withContext
    }

    /**
     * Belgium web requests
     */
    suspend fun beAsyncGetTestResult(
        pollingToken: String
    ): TestResultResponse = withContext(Dispatchers.IO) {
        beVerificationService.getTestResult(
            BeSubmissionConstants.TEST_RESULT_URL,
            TestResultRequest(pollingToken)
        )
    }

    suspend fun beAsyncAckTestResult(
        pollingToken: String
    ): Unit = withContext(Dispatchers.IO) {
        beVerificationService.ackResult(
            BeSubmissionConstants.TEST_RESULT_ACK_URL,
            TestResultRequest(pollingToken)
        )
        return@withContext
    }

    suspend fun beAsyncSubmitKeysToServer(
        k: String,
        r0: String,
        t0: String,
        t3: String,
        resultChannel: Int,
        onsetSymptomsDate: String?,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) = withContext(Dispatchers.IO) {
        Timber.d("Writing ${keys.size} Keys to the Submission Payload.")

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addAllKeys(keys)
            .setRequestPadding(getPadding(keys.size))
            .setOrigin("BE")
            .setConsentToFederation(true)
            .build()

        Timber.d(submissionPayload.toString())

        beSubmissionService.submitKeys(
            BeDiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            k, r0, t0, t3, resultChannel, onsetSymptomsDate,
            submissionPayload
        )
        return@withContext
    }

    suspend fun beAsyncDummySubmitKeysToServer() = withContext(Dispatchers.IO) {
        val fakeKeyCount = SubmissionConstants.minKeyCountForSubmission - 1
        Timber.d("Writing $fakeKeyCount Dummy Keys to the Submission Payload.")

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)

        val key = KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setKeyData(
                ByteString.copyFrom(
                    byteArrayOf(
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
                    )
                )
            )
            .setRollingStartIntervalNumber((cal.time.time / 60 / 1000 / 10).toInt())
            .setRollingPeriod(144)
            .setTransmissionRiskLevel(0)
            .build()

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .addKeys(key)
            .setRequestPadding(getPadding(1))
            .setOrigin("BE")
            .setConsentToFederation(true)
            .build()

        val fakeTestId = MobileTestId.generate(Date())

        beSubmissionService.submitKeys(
            BeDiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            fakeTestId.k,
            fakeTestId.r0,
            fakeTestId.t0,
            fakeTestId.t0,
            0,
            fakeTestId.onsetSymptomsDate,
            submissionPayload
        )
    }

    suspend fun getStatistics() = withContext(Dispatchers.IO) {
        statisticsService.getStatistics()
    }

    suspend fun getDynamicTexts() = withContext(Dispatchers.IO) {
        dynamicTextsService.getDynamicTexts()
    }

    suspend fun asyncFakeSubmission() = withContext(Dispatchers.IO) {

        val randomAdditions = 0 // prepare for random addition of keys
        val fakeKeyCount = SubmissionConstants.minKeyCountForSubmission + randomAdditions

        val fakeKeyPadding =
            requestPadding(SubmissionConstants.fakeKeySize * fakeKeyCount)

        val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
            .setRequestPadding(ByteString.copyFromUtf8(fakeKeyPadding))
            .build()

        submissionService.submitKeys(
            DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL,
            SubmissionConstants.EMPTY_HEADER,
            "1",
            requestPadding(SubmissionConstants.PADDING_LENGTH_HEADER_SUBMISSION_FAKE),
            submissionPayload
        )
    }

    private fun requestPadding(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
