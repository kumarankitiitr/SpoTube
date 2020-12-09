package `in`.programy.spotube.ui.fragment

import `in`.programy.spotube.R
import `in`.programy.spotube.exoplayer.AudioDownloadService
import `in`.programy.spotube.exoplayer.MusicService
import `in`.programy.spotube.repository.SpoTubeRepository
import `in`.programy.spotube.ui.MainActivity
import `in`.programy.spotube.ui.SpoTubeViewModel
import `in`.programy.spotube.util.Constants.CATEGORY_DOWNLOAD
import `in`.programy.spotube.util.Constants.FROM_DOWNLOAD
import `in`.programy.spotube.util.Constants.FROM_HOME
import `in`.programy.spotube.util.Resource
import `in`.programy.spotube.util.Static
import `in`.programy.spotube.util.Static.currentTag
import `in`.programy.spotube.util.Static.isServiceStarted
import `in`.programy.spotube.util.Static.tempDownloadList
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.ProgressiveDownloader
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_song.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song){
    lateinit var viewModel: SpoTubeViewModel
    private val args: SongFragmentArgs by navArgs()
    var isConnected = false

    @Inject
    lateinit var spoTubeRepository: SpoTubeRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("abcdefSongFragemtn","oncreate")

        viewModel = (activity as MainActivity).viewModel

        val mBound = (activity as MainActivity).mBound

        var tag: Int? = null

        try {
            tag = args.tag
            if(tag == FROM_HOME){
                GlobalScope.launch(Dispatchers.Main ){
                    try {
                        val item = args.item
                        val url = viewModel.getAuidoLink(item!!.id.videoId,requireContext())
                        if(url != "error"){

                            Intent(activity, MusicService::class.java).also { intent ->
                                intent.putExtra("item",item)
                                intent.putExtra("url",url)
                                intent.putExtra("tag",tag)

                                Util.startForegroundService((activity as MainActivity).applicationContext,intent)
                                (activity as MainActivity).bindService(intent, (activity as MainActivity).connection, Context.BIND_AUTO_CREATE)
                            }
                        }
                        else{
                            Toast.makeText(requireContext(),"Song Not available",Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_songFragment_to_homeFragment)
                        }
                    }
                    catch (t: Throwable){
                        Toast.makeText(requireContext(),"Song Not available",Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_songFragment_to_homeFragment)
                        Log.e("abcdeferror","from Song Fragment")
                        t.printStackTrace()
                    }
                }
            }
            else if(tag == FROM_DOWNLOAD){
                Intent(activity,MusicService::class.java).also { intent ->
                    intent.putExtra("tag",tag)
                    intent.putExtra("index",args.index)
                    Util.startForegroundService((activity as MainActivity).applicationContext,intent)
                    (activity as MainActivity).bindService(intent,(activity as MainActivity).connection,Context.BIND_AUTO_CREATE)
                }
            }
        }
        catch (t: Throwable ){
            t.printStackTrace()
        }

        mBound.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success ->{
                    isConnected = true
                    configureBackdrop()
                    fab?.visibility = View.VISIBLE
                    setStartingWindow((activity as MainActivity).musicService.exoPlayer.currentWindowIndex)
                    (activity as MainActivity).musicService.exoPlayer.playWhenReady = true
                }
                is Resource.Error -> {
                    Toast.makeText(context,"Unable to Connect",Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {

                }
            }
        })


        Static.isPlaying.observe(viewLifecycleOwner, Observer {
            Log.e("abcdefisPlaying",it.toString())
            if(isConnected){
                if(it){
                    ivPlayPauseDetail?.setImageDrawable(ContextCompat.getDrawable((activity as MainActivity).applicationContext,R.drawable.ic_baseline_pause_24))
                }
                else{
                    ivPlayPauseDetail?.setImageDrawable(ContextCompat.getDrawable((activity as MainActivity).applicationContext,R.drawable.ic_baseline_play_arrow_24))
                }
            }

        })


        Static.currentItemIndex.observe(viewLifecycleOwner, Observer {
            fabDownload.visibility = View.VISIBLE
            pbPlay.visibility = View.GONE
            if(isConnected){
                try {
                    setStartingWindow(it)
                }
                catch (t: Throwable){
                    t.printStackTrace()
                }

            }
        })

        Static.currentPlayingPos.observe(viewLifecycleOwner, Observer {
            if(isConnected){
                val min = TimeUnit.MILLISECONDS.toMinutes(it)
                val sec = TimeUnit.MILLISECONDS.toSeconds(it) - min*60
                val time = String.format("%02d:%02d",min,sec)
                tvCurrentPos?.text = time
            }
        })


        ivPlayPauseDetail.setOnClickListener {
            if(isConnected){
                if((activity as MainActivity).musicService.exoPlayer.isPlaying){
                    (activity as MainActivity).musicService.exoPlayer.pause()
                    ivPlayPauseDetail.setImageDrawable(ContextCompat.getDrawable((activity as MainActivity).applicationContext,R.drawable.ic_baseline_play_arrow_24))
                }
                else{
                    (activity as MainActivity).musicService.exoPlayer.play()
                    ivPlayPauseDetail.setImageDrawable(ContextCompat.getDrawable((activity as MainActivity).applicationContext,R.drawable.ic_baseline_pause_24))
                }
            }
        }

        ivSkip.setOnClickListener {
            if(isConnected){
                if((activity as MainActivity).musicService.exoPlayer.hasNext()){
                    (activity as MainActivity).musicService.exoPlayer.next()
                }
                else{
                    Toast.makeText(context,"Not Available",Toast.LENGTH_SHORT).show()
                }
            }
        }

        ivSkipPrevious.setOnClickListener {
            if(isConnected){
                if((activity as MainActivity).musicService.exoPlayer.hasPrevious()){
                    (activity as MainActivity).musicService.exoPlayer.previous()
                }
                else{
                    Toast.makeText(context,"Not Available",Toast.LENGTH_SHORT).show()
                }
            }
        }

        ivForward.setOnClickListener {
            if(isConnected){
                (activity as MainActivity).musicService.exoPlayer.seekTo(
                        (activity as MainActivity).musicService.exoPlayer.currentPosition+10000L)
            }
        }

        ivReplay.setOnClickListener {
            if(isConnected){
                val afterTime = (activity as MainActivity).musicService.exoPlayer.currentPosition-10000L
                if(afterTime>0){
                    (activity as MainActivity).musicService.exoPlayer.seekTo(afterTime)
                }
                else{
                    (activity as MainActivity).musicService.exoPlayer.seekTo(0L)
                }
            }
        }

        fabDownload.setOnClickListener {
            if(tag!=null && tag== FROM_DOWNLOAD){
                Toast.makeText(context,"Already Downloaded",Toast.LENGTH_SHORT).show()
            }
            if(isConnected && activity!=null){
                val id = (activity as MainActivity).musicService.tempSongs[(activity as MainActivity).musicService.exoPlayer.currentWindowIndex].
                id.videoId
                val downloadRequest = (activity as MainActivity).musicService.exoPlayer.currentMediaItem?.mediaId?.toUri()?.let { it1 ->
                    DownloadRequest.Builder(id,
                            it1
                    ).build()
                }

                if (downloadRequest != null) {
                    DownloadService.sendAddDownload(
                            requireContext(),
                            AudioDownloadService::class.java,
                            downloadRequest,
                            false)
                }



                val tempItem = (activity as MainActivity).musicService.tempSongs[(activity as MainActivity).musicService.exoPlayer.currentWindowIndex]
                tempItem.category = CATEGORY_DOWNLOAD
                tempItem.url = (activity as MainActivity).musicService.exoPlayer.currentMediaItem?.mediaId!!

                tempDownloadList.add(tempItem)
            }
        }


        //Log.e("received",item.toString())
    }

    override fun onPause() {
        super.onPause()
        Log.e("abcdefSongFragment","OnPause")
        (activity as MainActivity).bottomNav?.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        Log.e("abcdefSongFragment","OnStop")
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).bottomNav?.visibility = View.GONE
        Log.e("abcdefSongFragment","on Start")
    }

    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

    private fun configureBackdrop() {
        // Get the fragment reference
        val fragment = childFragmentManager.findFragmentById(R.id.filter_fragment)

        fragment?.let {
            // Get the BottomSheetBehavior from the fragment view
            BottomSheetBehavior.from(it.requireView()).let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                // Set the trigger that will expand your view
                fab.setOnClickListener { bsb.state = BottomSheetBehavior.STATE_EXPANDED }

                // Set the reference into class attribute (will be used latter)
                mBottomSheetBehavior = bsb
            }
        }
    }

    private fun setStartingWindow(it: Int){
        Log.e("abcdefitemIndex",it.toString())
        //setCurrentPos()
        if(activity != null && isConnected){
            tvSongName?.text = (activity as MainActivity).musicService.tempSongs[it].snippet.title

            Glide.with(this).asBitmap()
                    .load((activity as MainActivity).musicService.tempSongs[it].snippet.thumbnails.medium.url)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                        ) {
                            ivSongImage.setImageBitmap(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) = Unit
                    })
        }
    }
}