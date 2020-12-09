package `in`.programy.spotube.util

import `in`.programy.spotube.model.Item
import `in`.programy.spotube.util.Constants.FROM_NONE
import androidx.lifecycle.MutableLiveData

object Static {
    var isServiceStarted = false
    var trackMediaAdd = MutableLiveData<Boolean>()
    var isPlaying = MutableLiveData<Boolean>()
    var isLoading = MutableLiveData<Boolean>()
    var currentItemIndex = MutableLiveData<Int>()
    var currentPlayingPos = MutableLiveData<Long>()

    var tempDownloadList = mutableListOf<Item>()
    var currentTag = FROM_NONE
}