package be.sciensano.coronalert.risk

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import timber.log.Timber

class ExposureWindowRiskCalculationConfigMapper :
    ExposureWindowRiskCalculationConfig.Mapper {

    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): ExposureWindowRiskCalculationConfig {
        if (!rawConfig.hasRiskCalculationParameters()) {
            throw ApplicationConfigurationInvalidException(
                message = "Risk Calculation Parameters are missing"
            )
        }

        if (!rawConfig.hasDiagnosisKeysDataMapping()) {
            throw ApplicationConfigurationInvalidException(
                message = "Diagnosis Keys Data Mapping is missing"
            )
        }

        val riskCalculationParameters = rawConfig.riskCalculationParameters

        if (riskCalculationParameters.transmissionRiskValueMappingList.isEmpty()) {
            val msg =
                "Transmission Risk Value Mapping List is empty which indicates an outdated app config"
            Timber.w(msg)
            throw ApplicationConfigurationInvalidException(
                message = msg
            )
        }

        return ExposureWindowRiskCalculationContainer(
            minutesAtAttenuationFilters = riskCalculationParameters.minutesAtAttenuationFiltersList,
            minutesAtAttenuationWeights = riskCalculationParameters.minutesAtAttenuationWeightsList,
            transmissionRiskLevelEncoding = riskCalculationParameters.trlEncoding,
            transmissionRiskLevelFilters = riskCalculationParameters.trlFiltersList,
            normalizedTimePerExposureWindowToRiskLevelMapping =
            riskCalculationParameters.normalizedTimePerEWToRiskLevelMappingList,
            normalizedTimePerDayToRiskLevelMappingList =
            riskCalculationParameters.normalizedTimePerDayToRiskLevelMappingList,
            transmissionRiskValueMapping = riskCalculationParameters.transmissionRiskValueMappingList,
            diagnosisKeysDataMapping = rawConfig.diagnosisKeysDataMapping()
        )
    }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.diagnosisKeysDataMapping():
            DiagnosisKeysDataMapping {
        val diagnosisKeyDataMapping = this.diagnosisKeysDataMapping
        return DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder()
            .apply {
                setDaysSinceOnsetToInfectiousness(diagnosisKeyDataMapping.daysSinceOnsetToInfectiousnessMap)
                setInfectiousnessWhenDaysSinceOnsetMissing(
                    diagnosisKeysDataMapping.infectiousnessWhenDaysSinceOnsetMissing
                )
                setReportTypeWhenMissing(diagnosisKeysDataMapping.reportTypeWhenMissing)
            }.build()
    }

    data class ExposureWindowRiskCalculationContainer(
        override val minutesAtAttenuationFilters:
        List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>,
        override val minutesAtAttenuationWeights:
        List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>,
        override val transmissionRiskLevelEncoding:
        RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding,
        override val transmissionRiskLevelFilters:
        List<RiskCalculationParametersOuterClass.TrlFilter>,
        override val normalizedTimePerExposureWindowToRiskLevelMapping:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
        override val normalizedTimePerDayToRiskLevelMappingList:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
        override val transmissionRiskValueMapping:
        List<RiskCalculationParametersOuterClass.TransmissionRiskValueMapping>,
        override val diagnosisKeysDataMapping: DiagnosisKeysDataMapping
    ) : ExposureWindowRiskCalculationConfig
}
