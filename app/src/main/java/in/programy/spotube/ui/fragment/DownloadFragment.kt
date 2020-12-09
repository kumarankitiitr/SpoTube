package `in`.programy.spotube.ui.fragment

import `in`.programy.spotube.R
import `in`.programy.spotube.exoplayer.MusicService
import `in`.programy.spotube.ui.MainActivity
import `in`.programy.spotube.ui.SpoTubeViewModel
import `in`.programy.spotube.util.Constants.FROM_DOWNLOAD
import `in`.programy.spotube.util.Resource
import `in`.programy.spotube.util.Static
import `in`.programy.spotube.util.Static.currentTag
import `in`.programy.spotube.util.VideoListAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_download.*

class DownloadFragment : Fragment(R.layout.fragment_download) {
    lateinit var viewModel: SpoTubeViewModel
    lateinit var videoListAdapter: VideoListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        setupRecyclerView()

        if(Static.isServiceStarted){
            (activity as MainActivity).bindService(
                    Intent(activity, MusicService::class.java), (activity as MainActivity).connection, Context.BIND_AUTO_CREATE)
        }


        viewModel.readDownloads().observe(viewLifecycleOwner, Observer {
            if(it != null && it.isNotEmpty()){
                videoListAdapter.differ.submitList(it)
            }
        })

        videoListAdapter.setOnItemClickListener {
            val index = videoListAdapter.differ.currentList.indexOf(it)
            val bundle = Bundle().apply {
                putInt("index",index)
                putInt("tag", FROM_DOWNLOAD)
                putSerializable("item",null)
            }
            findNavController().navigate(R.id.action_downloadFragment_to_songFragment,bundle)
        }


        fabSong.setOnClickListener {
            findNavController().navigate(R.id.action_downloadFragment_to_songFragment)
        }

        (activity as MainActivity).mBound.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success -> {
                    fabSong.visibility = View.VISIBLE
                }
                else -> {
                    Log.e("info","service Disabled")
                }
            }
        })
    }

    private fun setupRecyclerView(){
        videoListAdapter = VideoListAdapter()
        rvDownload.apply {
            adapter = videoListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

}