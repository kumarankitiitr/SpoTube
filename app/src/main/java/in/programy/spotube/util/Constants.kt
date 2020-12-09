package `in`.programy.spotube.util

object Constants {
    const val BASE_URL = "https://www.googleapis.com/youtube/"
 //   const val API_KEY = "AIzaSyBiXGbFPtA40CpTJKSPq454tZxpg5Wc8lA"
 //   const val API_KEY = "AIzaSyDZ3SoJNgla83Vrs1C1QFmIwcYhth6HH28"

    val list = listOf("AIzaSyBiXGbFPtA40CpTJKSPq454tZxpg5Wc8lA","AIzaSyBYQYL7lRBEG2cDLVZw3JCHLcX_MVor6mg")
    var API_INDEX = 0
    var API_KEY = list[API_INDEX%2]
    //kumarankitiitr
 //   const val API_KEY = "AIzaSyBYQYL7lRBEG2cDLVZw3JCHLcX_MVor6mg"


    const val NOTIFICATION_CHANNEL_ID = "music"
    const val NOTIFICATION_ID = 1
    const val NETWORK_ERROR = "NETWORK ERROR"
    const val MEDIA_ROOT_ID = "root_id"
    const val DOWNLOAD_NOTIFICATION_ID = 2
    const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download"


    const val CATEGORY_DOWNLOAD = "download"
    const val CATEGORY_FAVOURITE = "favourite"

    const val FROM_HOME = 1
    const val FROM_DOWNLOAD = 2
    const val FROM_FAVOURITE = 3
    const val FROM_NONE = -3
}