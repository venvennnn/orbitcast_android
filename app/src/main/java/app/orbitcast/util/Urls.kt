package app.orbitcast.util

object Urls {
    const val DEFAULT_API = "https://api-13d-8000.sea1.zerops.app"

    fun normalizeBase(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        return trimmed.ifBlank { DEFAULT_API }
    }

    fun rss(apiBase: String, slug: String): String = "${normalizeBase(apiBase)}/feed/$slug.xml"

    fun httpUrlsIn(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        val regex = Regex("""https?://[^\s)>\]]+""")
        return regex.findAll(text)
            .map { it.value.trimEnd('.', ',', ';', '"', '\'') }
            .distinct()
            .toList()
    }
}
