package `in`.programy.spotube.yt

import android.media.MediaDrm
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Suggestions {
        suspend fun getSuggestions(query: String): MutableList<String> {
            try {
                val resList = mutableListOf<String>()
                val job = GlobalScope.async {
                    var result = ""
                    val url = URL("http://suggestqueries.google.com/complete/search?client=youtube&ds=yt&q=$query")
                    val httpsURLConnection = url.openConnection() as HttpURLConnection
                    val inputStream = httpsURLConnection.inputStream
                    val reader = InputStreamReader(inputStream)
                    var data = reader.read()

                    while (data != -1){
                        val current = data.toChar()
                        result += current
                        data = reader.read()
                    }

                    var after = result.substring(result.indexOf('(')+1,result.length-1)

                    var count = 0
                    for(i in after.split('"')){
                        count++
                        if (count%2 == 0 && count >2 && resList.size<7){
                            resList.add(i)
                            //Log.e("item",i)
                        }
                    }
                }
                job.await()
                return resList
            }
            catch (e: Exception){
                e.printStackTrace()
                return mutableListOf()
            }
        }

}