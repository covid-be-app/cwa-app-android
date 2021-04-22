package be.sciensano.coronalert.risk

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

data class RiskResult(
    val transmissionRiskLevel: Int,
    val normalizedTime: Double,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
)
