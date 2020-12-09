package `in`.programy.spotube.model

import java.io.Serializable

data class Id(
    val kind: String,
    val videoId: String
): Serializable