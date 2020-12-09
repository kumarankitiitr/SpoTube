package `in`.programy.spotube.yt

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLOptions
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class ytdl{
    suspend fun getAudioLink(videoId: String,context: Context): String{
        try {
            var url = ""
            val job = GlobalScope.async(Dispatchers.IO) {
                YoutubeDL.getInstance().init(context)
                val request = YoutubeDLRequest("https://www.youtube.com/watch?v=$videoId")

                request.addOption("-f", "bestaudio/bestaudio[ext=m4a]/bestaudio[ext=mp4]/bestaudio[ext=wav]/bestaudio[ext=aac]/bestaudio[ext=flv]/bestaudio[ext=webm]/bestaudio[ext=mp3]")

                YoutubeDL.getInstance().updateYoutubeDL(context)
                val stramInfo = YoutubeDL.getInstance().getInfo(request)

                url = stramInfo.url
                Log.e("abcdefblahblah",url)
            }
            job.await()
            return url
        }
        catch (e: Exception){
            e.printStackTrace()
            return "error"
        }
    }
}