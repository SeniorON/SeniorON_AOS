package com.example.senior_on.ui.parent.photo

/** S3 object paths contain immutable upload UUIDs. Keep non-signature query parameters. */
internal fun parentPhotoCacheKey(url: String): String {
    val base = url.substringBefore('?')
    val query = url.substringAfter('?', "")
    val parameters = query.split('&').filter(String::isNotEmpty)
    if (parameters.none { it.substringBefore('=').equals("X-Amz-Signature", ignoreCase = true) }) {
        return "parent-family-photo:$url"
    }
    val remaining = parameters.filterNot {
        it.substringBefore('=').startsWith("X-Amz-", ignoreCase = true)
    }.joinToString("&")
    return "parent-family-photo:$base" + if (remaining.isEmpty()) "" else "?$remaining"
}
