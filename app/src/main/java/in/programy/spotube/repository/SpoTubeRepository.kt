package `in`.programy.spotube.repository

import `in`.programy.spotube.model.Item
import `in`.programy.spotube.room.ItemDatabase
import `in`.programy.spotube.yt.RetrofitInstance.api
import `in`.programy.spotube.yt.Suggestions
import `in`.programy.spotube.yt.ytdl
import android.content.Context
import javax.inject.Inject


class SpoTubeRepository  @Inject constructor(
        val database: ItemDatabase
) {
    suspend fun getVideosByKeyword(keyword: String,pageToken:String?) = api.getVideosByKeyword(keyword,pageToken)

    suspend fun getRelatedVideos(videoId: String,pageToken: String?) = api.getRelatedVideos(videoId,pageToken)

    suspend fun getSuggestions(query: String) = Suggestions().getSuggestions(query)

    suspend fun getAudioLink(videoId: String,context: Context) = ytdl().getAudioLink(videoId,context)

    suspend fun insert(item: Item) = database.getItemDao().upsert(item)

    suspend fun delete(item: Item) = database.getItemDao().delete(item)

    fun readDownloads() = database.getItemDao().readDownloads()

    fun readFavourites() = database.getItemDao().readDownloads()

    fun getDownloadsList() = database.getItemDao().getDownloadsList()
}