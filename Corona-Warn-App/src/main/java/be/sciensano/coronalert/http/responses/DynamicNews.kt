package be.sciensano.coronalert.http.responses

data class DynamicNews(
    val structure: StructureNews,
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

data class StructureNews(
    val news: News,
)

data class News(
    val explanation: List<ExplanationNews>
)

data class ExplanationNews(
    val title: String,
    val text: String,
)
