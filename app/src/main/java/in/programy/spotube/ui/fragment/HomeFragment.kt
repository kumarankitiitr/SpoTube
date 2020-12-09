package `in`.programy.spotube.ui.fragment

import `in`.programy.spotube.R
import `in`.programy.spotube.exoplayer.MusicService
import `in`.programy.spotube.ui.MainActivity
import `in`.programy.spotube.ui.SpoTubeViewModel
import `in`.programy.spotube.util.Constants.FROM_HOME
import `in`.programy.spotube.util.Resource
import `in`.programy.spotube.util.Static
import `in`.programy.spotube.util.VideoListAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    lateinit var viewModel: SpoTubeViewModel
    private lateinit var videoListAdapter: VideoListAdapter
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var suggestionList: MutableList<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        if(Static.isServiceStarted){
            (activity as MainActivity).bindService(
                    Intent(activity, MusicService::class.java), (activity as MainActivity).connection, Context.BIND_AUTO_CREATE)
        }

        // For list View--------------------------------------------------------------------------
        suggestionList = mutableListOf()
        arrayAdapter = ArrayAdapter(requireContext(),R.layout.suggestion_item, suggestionList)
        lvSuggestion.adapter = arrayAdapter

        lvSuggestion.setOnItemClickListener { parent, view, position, id ->
            try{
                viewModel.getVideosByKeyword(suggestionList[position],null)
                clearSuggestions()
            }
            catch (t: Throwable){
                t.printStackTrace()
            }
        }

        viewModel.suggestions.observe(activity as MainActivity, Observer {
            when(it){
                is Resource.Success -> {
                    it.data?.let {list ->
                        suggestionList.clear()
                        suggestionList.addAll(list)
                    }
                    arrayAdapter.notifyDataSetChanged()
                }
                is Resource.Error -> {
                    it.message?.let { errorMessage ->
                        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    //nothing to do
                }
            }
        })


        // For Search View------------------------------------------------------------------------
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query != null && query.isNotEmpty()){
                    clearSuggestions()
                    viewModel.getVideosByKeyword(query,null)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null && newText.isNotEmpty()){
                    Log.e("abc",newText)
                    viewModel.getSuggestions(newText)
                }
                return true
            }

        })

        searchView.setOnCloseListener {
            viewModel.suggestions.postValue(Resource.Success(mutableListOf()))
            true
        }

        // For recycler View--------------------------------------------------------------------
        setUpRecyclerView()

        videoListAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("item",it)
                putInt("tag",FROM_HOME)
            }
            findNavController().navigate(R.id.action_homeFragment_to_songFragment,bundle)

//            Intent(context,SongActivity::class.java).also { intent ->
//                intent.putExtra("item",it)
//                startActivity(intent)
//            }

        }

        viewModel.keywordVideoList.observe(activity as MainActivity, Observer {
            when(it){
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let { videoResponse ->
                        videoListAdapter.differ.submitList(videoResponse.items.toList())
//                        val totalPages = newsResponse.totalResults?.div(QUERY_PAGE_SIZE)?.plus(2)
//                        isLastPage = viewModel.breakingNewsPage == totalPages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    it.message?.let { errorMessage ->
                        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })


        fabSong.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_songFragment)
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
    private fun setUpRecyclerView(){
        videoListAdapter = VideoListAdapter()
        rvHome.apply {
            adapter = videoListAdapter
            layoutManager = LinearLayoutManager(activity)
            //addOnScrollListener(scrollListener)
        }
    }

    private fun hideProgressBar(){
        pbHome?.visibility = View.GONE
    }

    private fun showProgressBar(){
        pbHome?.visibility = View.VISIBLE
    }

    private fun clearSuggestions(){
        searchView.setQuery("",false)
        viewModel.keywordVideoResponse?.items?.clear()
        viewModel.suggestions.postValue(Resource.Success(mutableListOf()))
        arrayAdapter.notifyDataSetChanged()
        val inputManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken,0)
    }
}