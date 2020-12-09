package `in`.programy.spotube.yt

import `in`.programy.spotube.model.VideoResponse
import `in`.programy.spotube.util.Constants.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ytAPI {
    @GET("v3/search")
    suspend fun getVideosByKeyword(
            @Query("q") keyWord: String,
            @Query("pageToken") pageToken: String?,
            @Query("part") part: String = "snippet",
            @Query("maxResults") maxResult: String = "5",
            @Query("type") type: String = "video",
            @Query("key") key: String = API_KEY
    ): Response<VideoResponse>

    @GET("v3/search")
    suspend fun getRelatedVideos(
            @Query("relatedToVideoId") relatedToVideoId: String,
            @Query("pageToken") pageToken: String?,
            @Query("part") part: String = "snippet",
            @Query("maxResults") maxResult: String = "5",
            @Query("type") type: String = "video",
            @Query("key") key: String = API_KEY
    ): Response<VideoResponse>

}