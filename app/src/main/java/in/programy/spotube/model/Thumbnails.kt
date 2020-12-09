package `in`.programy.spotube.model

import `in`.programy.spotube.model.Default
import `in`.programy.spotube.model.High
import `in`.programy.spotube.model.Medium
import java.io.Serializable

data class Thumbnails(
        val default: Default,
        val high: High,
        val medium: Medium
): Serializable