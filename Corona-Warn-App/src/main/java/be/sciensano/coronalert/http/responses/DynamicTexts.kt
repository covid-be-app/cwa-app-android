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
    val thankYou: ThankYou,
    val participatingCountries: ParticipatingCountries
)

data class Standard(val preventiveMeasures: List<PreventiveMeasure>)
data class HighRisk(val preventiveMeasures: List<PreventiveMeasure>)
data class PositiveTestResultCard(val explanation: List<PositiveTestResultCardExplanation>)
data class PositiveTestResult(val explanation: List<Explanation>)
data class NegativeTestResult(val explanation: List<Explanation>)
data class ThankYou(val pleaseNote: List<PleaseNote>, val otherInformation: List<OtherInformation>)
data class ParticipatingCountries(val list: List<Country>)

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

data class Country(val icon: String, val text: String)

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

val countryIconToResMap = hashMapOf(
    "flag.at" to R.drawable.ic_country_at,
    "flag.be" to R.drawable.ic_country_be,
    "flag.bg" to R.drawable.ic_country_bg,
    "flag.ch" to R.drawable.ic_country_ch,
    "flag.cy" to R.drawable.ic_country_cy,
    "flag.cz" to R.drawable.ic_country_cz,
    "flag.de" to R.drawable.ic_country_de,
    "flag.dk" to R.drawable.ic_country_dk,
    "flag.ee" to R.drawable.ic_country_ee,
    "flag.es" to R.drawable.ic_country_es,
    "flag.eu" to R.drawable.ic_country_eu,
    "flag.fi" to R.drawable.ic_country_fi,
    "flag.fr" to R.drawable.ic_country_fr,
    "flag.gr" to R.drawable.ic_country_gr,
    "flag.hr" to R.drawable.ic_country_hr,
    "flag.hu" to R.drawable.ic_country_hu,
    "flag.ie" to R.drawable.ic_country_ie,
    "flag.is" to R.drawable.ic_country_is,
    "flag.it" to R.drawable.ic_country_it,
    "flag.li" to R.drawable.ic_country_li,
    "flag.lt" to R.drawable.ic_country_lt,
    "flag.lu" to R.drawable.ic_country_lu,
    "flag.lv" to R.drawable.ic_country_lv,
    "flag.mt" to R.drawable.ic_country_mt,
    "flag.nl" to R.drawable.ic_country_nl,
    "flag.no" to R.drawable.ic_country_no,
    "flag.pl" to R.drawable.ic_country_pl,
    "flag.pt" to R.drawable.ic_country_pt,
    "flag.ro" to R.drawable.ic_country_ro,
    "flag.se" to R.drawable.ic_country_se,
    "flag.si" to R.drawable.ic_country_si,
    "flag.sk" to R.drawable.ic_country_sk,
    "flag.uk" to R.drawable.ic_country_uk,
)
