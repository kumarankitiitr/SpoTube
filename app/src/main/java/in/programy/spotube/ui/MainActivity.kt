package `in`.programy.spotube.ui

import `in`.programy.spotube.R
import `in`.programy.spotube.exoplayer.MusicService
import `in`.programy.spotube.ui.fragment.SongFragment
import `in`.programy.spotube.util.Resource
import `in`.programy.spotube.util.Static.currentItemIndex
import `in`.programy.spotube.util.Static.isPlaying
import `in`.programy.spotube.util.Static.isServiceStarted
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: SpoTubeViewModel

    lateinit var musicService: MusicService
    var mBound: MutableLiveData<Resource<Boolean>> = MutableLiveData()

    val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            mBound.postValue(Resource.Success(true))
            isServiceStarted = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound.postValue(Resource.Error("Unable to Connect"))
            isServiceStarted = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        mBound.postValue(Resource.Loading())

        bottomNav.setupWithNavController(spotubeNavHostFragment.findNavController())
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService.apply {
            stopForeground(true)
            stopSelf()
        }

    }

}