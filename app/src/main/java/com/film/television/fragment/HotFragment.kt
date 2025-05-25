package com.film.television.fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.film.television.R
import com.film.television.adapter.VideoAdapter
import com.film.television.databinding.FragmentHotBinding
import com.film.television.databinding.ItemHotBinding
import com.film.television.model.HomepageVideoResp
import com.film.television.utils.AdUtil
import com.film.television.utils.TokenUtil
import com.film.television.utils.UIUtil
import kotlinx.coroutines.launch
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import com.film.television.utils.DataStoreUtil
import com.film.television.viewmodel.HomeViewModel
import com.film.television.viewmodel.MainViewModel

class HotFragment : BaseFragment<FragmentHotBinding>() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var mRespData: HomepageVideoResp.Data? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHotBinding {
        return FragmentHotBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.root.setColorSchemeResources(R.color.theme_color)
        binding.root.setOnRefreshListener {
            getData()
        }
    }

    override fun initData() {
        binding.root.isRefreshing = true
        getData()
    }

    private fun getData() {
        viewLifecycleOwner.lifecycleScope.launch {
            TokenUtil.getToken()?.let {
                val resp = homeViewModel.queryHomepageVideo(it)
                if (resp.code == 200) {
                    val respData = resp.data
                    if (respData != mRespData) {
                        binding.content.removeAllViews()
                        setContent(respData)
                        mRespData = respData
                    }
                }
            }
            binding.root.isRefreshing = false
        }
    }

    private suspend fun setContent(data: HomepageVideoResp.Data) {
        var counter = 1
        if (data.tv.isNotEmpty()) {
            val tvBinding = ItemHotBinding.inflate(layoutInflater)
            tvBinding.title.text = getString(R.string.hot_tv)
            tvBinding.more.setOnClickListener {
                mainViewModel.setHotCategory("TV")
            }
            tvBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            val tvData = data.tv.takeIf { it.size <= 6 } ?: data.tv.subList(0, 6)
            tvBinding.recyclerView.adapter = VideoAdapter(null, this, null, tvData)
            binding.content.addView(tvBinding.root)
            tvBinding.adContainer.postDelayed({
                Log.d("lytest", "tvBinding loadFeedsAd")
                AdUtil.showFeedsAd(
                    requireActivity(),
                    tvBinding.adContainer,
                    UIUtil.feedsWidth,
                    0
                ) {
                    tvBinding.adContainer.visibility = View.VISIBLE
                }
            }, (counter++) * 1000L)
        }
        if (data.movie.isNotEmpty()) {
            val movieBinding = ItemHotBinding.inflate(layoutInflater)
            movieBinding.title.text = getString(R.string.hot_movie)
            movieBinding.more.setOnClickListener {
                mainViewModel.setHotCategory("MOVIE")
            }
            movieBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            val movieData = data.movie.takeIf { it.size <= 6 } ?: data.movie.subList(0, 6)
            movieBinding.recyclerView.adapter = VideoAdapter(null, this, null, movieData)
            binding.content.addView(movieBinding.root)
            movieBinding.adContainer.postDelayed({
                Log.d("lytest", "movieBinding loadFeedsAd")
                AdUtil.showFeedsAd(
                    requireActivity(),
                    movieBinding.adContainer,
                    UIUtil.feedsWidth,
                    0
                ) {
                    movieBinding.adContainer.visibility = View.VISIBLE
                }
            }, (counter++) * 1000L)
        }
        if (data.variety.isNotEmpty()) {
            val varietyBinding = ItemHotBinding.inflate(layoutInflater)
            varietyBinding.title.text = getString(R.string.hot_variety)
            varietyBinding.more.setOnClickListener {
                mainViewModel.setHotCategory("VARIETY")
            }
            varietyBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            val varietyData = data.variety.takeIf { it.size <= 6 } ?: data.variety.subList(0, 6)
            varietyBinding.recyclerView.adapter = VideoAdapter(null, this, null, varietyData)
            binding.content.addView(varietyBinding.root)
            varietyBinding.adContainer.postDelayed({
                Log.d("lytest", "varietyBinding loadFeedsAd")
                AdUtil.showFeedsAd(
                    requireActivity(),
                    varietyBinding.adContainer,
                    UIUtil.feedsWidth,
                    0
                ) {
                    varietyBinding.adContainer.visibility = View.VISIBLE
                }
            }, (counter++) * 1000L)
        }
        if (data.anime.isNotEmpty()) {
            val animeBinding = ItemHotBinding.inflate(layoutInflater)
            animeBinding.title.text = getString(R.string.hot_anime)
            animeBinding.more.setOnClickListener {
                mainViewModel.setHotCategory("ANIME")
            }
            animeBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            val animeData = data.anime.takeIf { it.size <= 6 } ?: data.anime.subList(0, 6)
            animeBinding.recyclerView.adapter = VideoAdapter(null, this, null, animeData)
            binding.content.addView(animeBinding.root)
            animeBinding.adContainer.postDelayed({
                Log.d("lytest", "animeBinding loadFeedsAd")
                AdUtil.showFeedsAd(
                    requireActivity(),
                    animeBinding.adContainer,
                    UIUtil.feedsWidth,
                    0
                ) {
                    animeBinding.adContainer.visibility = View.VISIBLE
                }
            }, (counter++) * 1000L)
        }
        if (!DataStoreUtil.getHotScrollViewInterstitialAdShown() && binding.content.childCount >= 2) {
            val onScrollChangeListener = object : NestedScrollView.OnScrollChangeListener {
                private var firstChildHeight = 0

                override fun onScrollChange(
                    v: NestedScrollView,
                    scrollX: Int,
                    scrollY: Int,
                    oldScrollX: Int,
                    oldScrollY: Int
                ) {
                    if (firstChildHeight == 0) {
                        firstChildHeight = binding.content.getChildAt(0).height
                    }
                    if (scrollY > oldScrollY && scrollY >= firstChildHeight) {
                        binding.scrollView.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)
                        viewLifecycleOwner.lifecycleScope.launch {
                            DataStoreUtil.setHotScrollViewInterstitialAdShown()
                        }
                        AdUtil.showInterstitialAd(requireActivity())
                    }
                }
            }
            binding.scrollView.setOnScrollChangeListener(onScrollChangeListener)
        }
    }

//    private fun isViewInViewPort(view: View): Boolean {
//        val location = IntArray(2)
//        view.getLocationOnScreen(location)
//        val viewRect = Rect(
//            location[0],
//            location[1],
//            location[0] + view.width,
//            location[1] + view.height
//        )
//        val displayRect = Rect()
//        view.getWindowVisibleDisplayFrame(displayRect)
//        return viewRect.intersect(displayRect)
//    }
}