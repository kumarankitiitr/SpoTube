package `in`.programy.spotube.model

import `in`.programy.spotube.model.Item
import `in`.programy.spotube.model.PageInfo

data class VideoResponse(
        val etag: String,
        val items: MutableList<Item>,
        val kind: String,
        val nextPageToken: String,
        val prevPageToken: String?,
        val pageInfo: PageInfo,
        val regionCode: String
)