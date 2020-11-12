package be.sciensano.coronalert.http.responses

import de.rki.coronawarnapp.R

data class DynamicTexts(
    val structure: Structure,
    val texts: Map<String, Map<String, String>>
) {

    companion object {
        fun getText(
            id: String,
            texts: Map<String, Map<String, String>>,
            lang: String
        ): String {
            return (texts[lang] ?: texts["en"])?.get(id) ?: ""
        }
    }
}

data class Structure(
    val standard: Standard,
    val highRisk: HighRisk,
    val positiveTestResult: PositiveTestResult,
    val positiveTestResultCard: PositiveTestResultCard,
    val negativeTestResult: NegativeTestResult,
    val thankYou: ThankYou
)

data class Standard(val preventiveMeasures: List<PreventiveMeasure>)
data class HighRisk(val preventiveMeasures: List<PreventiveMeasure>)
data class PositiveTestResultCard(val explanation: List<PositiveTestResultCardExplanation>)
data class PositiveTestResult(val explanation: List<Explanation>)
data class NegativeTestResult(val explanation: List<Explanation>)
data class ThankYou(val pleaseNote: List<PleaseNote>, val otherInformation: List<OtherInformation>)
data class PreventiveMeasure(val icon: String, val text: String, val paragraphs: List<String>?)

data class Explanation(
    val icon: String,
    val title: String,
    val text: String,
    val paragraphs: List<String>?
)

data class PositiveTestResultCardExplanation(
    val icon: String,
    val text: String
)

data class PleaseNote(val icon: String, val text: String)

data class OtherInformation(val paragraphs: List<String>)

val iconToResMap = hashMapOf(
    "dt_check" to R.drawable.ic_test_result_done,
    "dt_cloud" to R.drawable.ic_test_result_warning,
    "dt_delete" to R.drawable.ic_risk_warning,
    "dt_distance" to R.drawable.ic_risk_details_distance,
    "dt_empty" to R.drawable.ic_risk_warning,
    "dt_exclamation" to R.drawable.ic_risk_warning,
    "dt_home" to R.drawable.ic_risk_details_home,
    "dt_mouthMask" to R.drawable.ic_risk_details_mask,
    "dt_phone" to R.drawable.ic_risk_details_contact,
    "dt_sneeze" to R.drawable.ic_risk_details_sneeze,
    "dt_virus" to R.drawable.ic_risk_card_contact,
    "dt_washHands" to R.drawable.ic_risk_details_wash
)
