package `in`.programy.spotube.exoplayer

import `in`.programy.spotube.exoplayer.callbacks.MusicPlayerNotificationListener
import `in`.programy.spotube.model.Item
import `in`.programy.spotube.repository.SpoTubeRepository
import `in`.programy.spotube.util.Constants.FROM_DOWNLOAD
import `in`.programy.spotube.util.Constants.FROM_HOME
import `in`.programy.spotube.util.Resource
import `in`.programy.spotube.util.Static.currentItemIndex
import `in`.programy.spotube.util.Static.currentPlayingPos
import `in`.programy.spotube.util.Static.currentTag
import `in`.programy.spotube.util.Static.isLoading
import `in`.programy.spotube.util.Static.isPlaying
import `in`.programy.spotube.util.Static.trackMediaAdd
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.IOException

import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService: Service() {

    @Inject
    lateinit var spoTubeRepository: SpoTubeRepository

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var progressiveMediaSource: ProgressiveMediaSource.Factory

    private val binder = LocalBinder()

    val songs = MutableLiveData<Resource<MutableList<Item>>>()
    val tempSongs = mutableListOf<Item>()

   private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main+serviceJob)

    override fun onCreate() {
        super.onCreate()

        exoPlayer.addListener(object : Player.EventListener{
            override fun onPlayerError(error: ExoPlaybackException) {
                when(error.type){
                    ExoPlaybackException.TYPE_SOURCE -> {
                        try {
                            tempSongs.removeAt(exoPlayer.currentWindowIndex)
                            songs.postValue(Resource.Success(tempSongs))
                            exoPlayer.removeMediaItem(exoPlayer.currentWindowIndex)

                            if(exoPlayer.hasPrevious()){
                                exoPlayer.previous()
                            }
                            else if (exoPlayer.hasNext()){
                                exoPlayer.next()
                            }
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                        catch (t: Throwable){
                            t.printStackTrace()
                        }
                    }
                    ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {
                        error.printStackTrace()

                    }
                    ExoPlaybackException.TYPE_REMOTE -> {
                        error.printStackTrace()

                    }
                    ExoPlaybackException.TYPE_RENDERER -> {
                        error.printStackTrace()

                    }
                    ExoPlaybackException.TYPE_TIMEOUT -> {
                        error.printStackTrace()

                    }
                    ExoPlaybackException.TYPE_UNEXPECTED -> {
                        error.printStackTrace()
                    }
                }
                Toast.makeText(applicationContext,"Some Error Occurred",Toast.LENGTH_SHORT).show()
            }
        })


        exoPlayer.addAnalyticsListener(object : AnalyticsListener{
            override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlayingMusic: Boolean) {
                isPlaying.postValue(isPlayingMusic)
            }

            override fun onIsLoadingChanged(eventTime: AnalyticsListener.EventTime, isLoadingMedia: Boolean) {
                isLoading.postValue(isLoadingMedia)
            }

            override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: ExoPlaybackException) {
                error.printStackTrace()
                Toast.makeText(applicationContext,"Error Occurred",Toast.LENGTH_SHORT).show()
            }

            override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
                currentItemIndex.postValue(eventTime.currentWindowIndex)
                var runnable: java.lang.Runnable? = null
                val handler = Handler()
                runnable = Runnable {
                    currentPlayingPos.postValue(exoPlayer.currentPosition)
                        handler.postDelayed(runnable!!, 1000)
                }
                handler.postDelayed(runnable, 0)

            }

            override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
                currentPlayingPos.postValue(eventTime.currentPlaybackPositionMs)
            }



        })


        musicNotificationManager = MusicNotificationManager(this,MusicPlayerNotificationListener(this),tempSongs)

        musicNotificationManager.showNotification(exoPlayer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("info","on command")
        val tag = intent?.getIntExtra("tag",-2)

        if(tag == -2) return START_STICKY

        if(tag == FROM_HOME){
            Log.e("info","from Home")
            val item = intent.getSerializableExtra("item")
            (item as Item).let {
                serviceScope.launch{
                    try {
                        if(true){
                            Log.e("abcdeftempsongs",tempSongs.toString())
                            if(tempSongs.isEmpty()){
                                addMediaItem(it,intent.getStringExtra("url")!!,-1)
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            }
                            else{

                                exoPlayer.clearMediaItems()
                                tempSongs.clear()

                                addMediaItem(it,intent.getStringExtra("url")!!,0)
                                exoPlayer.prepare()
                                exoPlayer.seekTo(0,0L)
                                exoPlayer.playWhenReady= true
                            }

                            val response = spoTubeRepository.getRelatedVideos(it.id.videoId,null)
                            if(response.isSuccessful){
                                Log.e("info","successful")
                                response.body()?.items?.let { list->
                                    for (x in list){
                                        val u = spoTubeRepository.getAudioLink(x.id.videoId,applicationContext)
                                        if(u != "error"){
                                            addMediaItem(x,u,-1)
                                        }
                                    }
                                    exoPlayer.prepare()
//                                exoPlayer.playWhenReady = true
                                }
                            }
                            else {
                                Log.e("info","response unsuccessful")
                            }
                        }
                        else{
                            Log.e("info","no Internet")
                            songs.postValue(Resource.Error("No Internet"))
                        }
                    }
                    catch (t: Throwable){
                        when(t) {
                            is IOException -> songs.postValue(Resource.Error("Network Failure"))
                            else -> songs.postValue(Resource.Error("Conversion Error"))
                        }
                        t.printStackTrace()
                    }
                }
            }
            currentTag = FROM_HOME
        }
        else if (tag == FROM_DOWNLOAD){
            val index = intent.getIntExtra("index",0)
            val list = spoTubeRepository.getDownloadsList()

            if(currentTag== FROM_HOME || currentTag == FROM_DOWNLOAD){
                exoPlayer.clearMediaItems()
                tempSongs.clear()
            }
            for(i in list){
                val mediaSource: ProgressiveMediaSource = progressiveMediaSource.createMediaSource(
                        MediaItem.fromUri(i.url)
                )
                Log.e("abcdefinfo",i.toString())
                tempSongs.add(i)
                exoPlayer.addMediaSource(mediaSource)

                val check : Boolean= trackMediaAdd.value ?: true
                trackMediaAdd.postValue(!check)
            }
            exoPlayer.prepare()
            Log.e("info",exoPlayer.mediaItemCount.toString())
            exoPlayer.seekTo(index,0L)
            exoPlayer.playWhenReady = true

            currentTag = FROM_DOWNLOAD
        }

        return START_STICKY
    }
    private fun addMediaItem(item: Item,url: String,index: Int){
        try {
            if(index != -1){
                tempSongs.add(index,item)
                exoPlayer.addMediaItem(index,MediaItem.fromUri(url))
            }
            else{
                tempSongs.add(item)
                exoPlayer.addMediaItem(MediaItem.fromUri(url))
            }

            val check : Boolean= trackMediaAdd.value ?: true
            trackMediaAdd.postValue(!check)
            Log.e("value", trackMediaAdd.value.toString())

        }
        catch (t: Throwable){
            Log.e("abcdeferror","from Music service")
            t.printStackTrace()
        }

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("info","Binder")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        //exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }
}