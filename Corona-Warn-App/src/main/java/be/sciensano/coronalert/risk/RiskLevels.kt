package be.sciensano.coronalert.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow

interface RiskLevels {

    fun calculateRisk(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindow: ExposureWindow
    ): RiskResult?

    fun aggregateResults(
        appConfig: ExposureWindowRiskCalculationConfig,
        exposureWindowResultMap: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskResult
}
