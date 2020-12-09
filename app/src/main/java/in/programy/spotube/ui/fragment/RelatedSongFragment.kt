package `in`.programy.spotube.ui.fragment

import `in`.programy.spotube.R
import `in`.programy.spotube.ui.MainActivity
import `in`.programy.spotube.util.Static.isServiceStarted
import `in`.programy.spotube.util.Static.trackMediaAdd
import `in`.programy.spotube.util.VideoListAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_related_song.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RelatedSongFragment : Fragment(R.layout.fragment_related_song) {
    private lateinit var videoListAdapter: VideoListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        //Log.e("related","related song Fragment Started")


        trackMediaAdd.observe(viewLifecycleOwner, Observer {
            hideProgressBar()
            GlobalScope.launch(Dispatchers.Main) {
                delay(1000)
                if(activity != null && isServiceStarted){
                    videoListAdapter.differ.submitList(
                        (activity as MainActivity).musicService.tempSongs.toList())
                }
            }

        })


        videoListAdapter.setOnItemClickListener {
            val index = videoListAdapter.differ.currentList.indexOf(it)
                (activity as MainActivity).musicService.exoPlayer.seekTo(index,0L)
                (activity as MainActivity).musicService.exoPlayer.playWhenReady = true
        }

//        (activity as SongActivity).mBound.observe(activity as SongActivity, Observer {
//            if(it){
//                (activity as SongActivity).musicService.songs.observe(activity as SongActivity, Observer {list->
//                    when(list){
//                        is Resource.Success ->{
//                            videoListAdapter.differ.submitList(list.data)
//                        }
//                        else -> Log.e("info","something else")
//                    }
//                })
//            }
//            else{
//
//            }
  //      })
//        if(true){
//            (activity as SongActivity).musicService.songs.observe(activity as SongActivity, Observer {
//                when(it){
//                    is Resource.Success ->{
//                        hideProgressBar()
//                        videoListAdapter.differ.submitList(it.data)
//                    }
//                    is Resource.Loading -> {
//                        showProgressBar()
//                    }
//                    is Resource.Error -> {
//                        hideProgressBar()
//                        Toast.makeText(context,"Some Error Occurred",Toast.LENGTH_SHORT).show()
//                    }
//                }
//            })
//        }
    }

        private fun setupRecyclerView(){
        videoListAdapter = VideoListAdapter()
        rvRelated.apply {
            adapter = videoListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showProgressBar(){
        pbRelated?.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        pbRelated?.visibility = View.GONE
    }
}