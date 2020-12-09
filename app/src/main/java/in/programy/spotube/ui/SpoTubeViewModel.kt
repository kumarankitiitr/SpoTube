package `in`.programy.spotube.ui

import `in`.programy.spotube.model.Item
import `in`.programy.spotube.model.VideoResponse
import `in`.programy.spotube.repository.SpoTubeRepository
import `in`.programy.spotube.util.Constants.API_INDEX
import `in`.programy.spotube.util.Resource
import `in`.programy.spotube.util.SpoTubeApplication
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Provides
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject


class SpoTubeViewModel @Inject constructor(
        var repository: SpoTubeRepository
) : ViewModel(){


    val suggestions: MutableLiveData<Resource<MutableList<String>>> = MutableLiveData()

    val relatedVideoList: MutableLiveData<Resource<VideoResponse>> = MutableLiveData()
    var relatedVideoResponse : VideoResponse? = null

    val keywordVideoList: MutableLiveData<Resource<VideoResponse>> = MutableLiveData()
    var keywordVideoResponse: VideoResponse? = null

    init {
        getVideosByKeyword("TSeries new",null)
    }

    fun getSuggestions(query: String){
        viewModelScope.launch {
            suggestions.postValue(Resource.Loading())
            try {
                if(true){
                    val response = repository.getSuggestions(query)
                    if(response.isNotEmpty()){
                        suggestions.postValue(Resource.Success(response))
                    }
                    else{
                        Log.e("error","no suggestions")
                    }
                }
                else{
                    suggestions.postValue(Resource.Error("No internet connection"))
                }
            }
            catch (t: Throwable){
                when(t) {
                    is IOException -> suggestions.postValue(Resource.Error("Network Failure"))
                    else -> suggestions.postValue(Resource.Error("Conversion Error"))
                }
                t.printStackTrace()
            }
        }
    }

    fun getVideosByKeyword(keyword: String, pageToken: String?){
        viewModelScope.launch {
            keywordVideoList.postValue(Resource.Loading())
            try {
                if(true){
                    val response = repository.getVideosByKeyword(keyword,pageToken)
                    keywordVideoList.postValue(handleKeyWordVideoResponse(response))
                }
                else{
                    keywordVideoList.postValue(Resource.Error("No Internet"))
                }
            }
            catch (t: Throwable){
                when(t) {
                    is IOException -> keywordVideoList.postValue(Resource.Error("Network Failure"))
                    else -> keywordVideoList.postValue(Resource.Error("Conversion Error"))
                }
                t.printStackTrace()
            }
        }
    }

    fun getRelatedVideos(videoId: String, pageToken: String?){
        viewModelScope.launch {
            relatedVideoList.postValue(Resource.Loading())
            try {
                if(true){
                    val response = repository.getRelatedVideos(videoId,pageToken)
                    relatedVideoList.postValue(handleRelatedVideoResponse(response))
                }
                else{
                    relatedVideoList.postValue(Resource.Error("No Internet"))
                }
            }
            catch (t: Throwable){
                when(t) {
                    is IOException -> keywordVideoList.postValue(Resource.Error("Network Failure"))
                    else -> keywordVideoList.postValue(Resource.Error("Conversion Error"))
                }
                t.printStackTrace()
            }
        }
    }

    fun handleKeyWordVideoResponse(response: Response<VideoResponse>): Resource<VideoResponse>{
        if(response.isSuccessful){
            if(response.code() == 403){
                API_INDEX++
                return Resource.Error("try again")
            }
            response.body()?.let { resultResponse ->
                if(keywordVideoResponse == null){
                    keywordVideoResponse = resultResponse
                }
                else{
                    val oldResponse = keywordVideoResponse?.items
                    val newResponse = resultResponse.items
                    oldResponse?.addAll(newResponse)
                }
                return Resource.Success(keywordVideoResponse?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun handleRelatedVideoResponse(response: Response<VideoResponse>): Resource<VideoResponse>{
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->
                if(relatedVideoResponse == null){
                    relatedVideoResponse = resultResponse
                }
                else{
                    val oldResponse = relatedVideoResponse?.items
                    val newResponse = resultResponse.items
                    oldResponse?.addAll(newResponse)
                }
                return Resource.Success(relatedVideoResponse?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    suspend fun getAuidoLink(videoId: String, context: Context): String{
        return repository.getAudioLink(videoId,context)
    }

    fun deleteItem(item: Item){
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun insertItem(item: Item){
        viewModelScope.launch {
            repository.insert(item)
        }
    }

    fun readDownloads() = repository.readDownloads()

    fun readFavourites() = repository.readFavourites()
}