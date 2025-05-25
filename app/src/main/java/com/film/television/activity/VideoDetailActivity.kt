package com.film.television.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.film.television.adapter.VideoAdapter
import com.film.television.databinding.ActivityVideoDetailBinding
import com.film.television.model.GeneralVideoInfoResp
import com.film.television.model.VideoBean
import com.film.television.showShortToast
import com.film.television.utils.AdUtil
import com.film.television.utils.ApplicationData
import com.film.television.utils.Constants
import com.film.television.utils.ImageLoader
import com.film.television.utils.RouteUtil
import com.film.television.utils.TokenUtil
import com.film.television.utils.UIUtil
import com.film.television.viewmodel.VideoDetailViewModel
import kotlinx.coroutines.launch

class VideoDetailActivity : BaseActivity<ActivityVideoDetailBinding>() {
    private lateinit var videoBean: VideoBean
    private val videoDetailViewModel: VideoDetailViewModel by viewModels()
    private var dataObserver: Observer<GeneralVideoInfoResp.Data>? = null

    override fun getViewBinding(): ActivityVideoDetailBinding {
        return ActivityVideoDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        videoBean = intent.getParcelableExtra(Constants.KEY_VIDEO_BEAN)!!
        Log.d("lytest", "VideoDetailActivity videoBean: $videoBean")
        binding.back.setOnClickListener { finish() }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        if (Constants.GLOBAL_AD_ENABLED && Constants.FEEDS_AD_ENABLED) {
            AdUtil.showFeedsAd(this, binding.adContainer, UIUtil.feedsWidth, 0) {
                binding.adContainer.visibility = View.VISIBLE
            }
        }
        setView()
    }

    private fun setView() {
        if (!Constants.GLOBAL_AD_ENABLED || !Constants.SPLASH_AD_ENABLED) {
            ImageLoader.loadImage(this, videoBean.imageUrl, binding.cover)
        } else {
            binding.imgPlay.visibility = View.VISIBLE
            binding.textPlay.visibility = View.VISIBLE
            val onPlayListener = View.OnClickListener {
                if (Constants.VIDEO_AD_ENABLED) {
                    AdUtil.showRewardVideoAd(this) {
                        if (it) {
                            RouteUtil.goToWebActivity(this, null, videoBean.playUrl)
                        } else {
                            showShortToast("没有获得奖励！")
                        }
                    }
                } else {
                    RouteUtil.goToWebActivity(this, null, videoBean.playUrl)
                }
            }
            binding.imgPlay.setOnClickListener(onPlayListener)
            binding.textPlay.setOnClickListener(onPlayListener)
        }
        if (videoBean.rating != 0f) {
            binding.name.text = "${videoBean.title}·"
            binding.rating.visibility = View.VISIBLE
            binding.rating.text = "·${videoBean.rating}分"
        } else {
            binding.name.text = "${videoBean.title}"
            binding.rating.visibility = View.GONE
        }
        binding.intro.text = videoBean.introduction.takeIf { !it.isNullOrEmpty() } ?: "无"
        if (Constants.GLOBAL_AD_ENABLED && Constants.INTERSTITIAL_AD_ENABLED) {
            AdUtil.showInterstitialAd(this)
        }
    }

    override fun initObservation() {
        setTags()
    }

    private fun setTags() {
        dataObserver?.let { ApplicationData.generalVideoInfoData.removeObserver(it) }
        dataObserver = object : Observer<GeneralVideoInfoResp.Data> {
            override fun onChanged(value: GeneralVideoInfoResp.Data) {
                val tags = mutableListOf<String>()
                value.categories.get(videoBean.releaseYear)?.asString?.let {
                    tags.add(it)
                }
                value.genres.get(videoBean.genre)?.asString?.let {
                    tags.add(it)
                }
                value.regions.get(videoBean.region)?.asString?.let {
                    tags.add(it)
                }
                binding.tags.visibility = View.VISIBLE
                binding.tags.text = tags.joinToString(" | ")
            }
        }.also {
            ApplicationData.generalVideoInfoData.observe(this, it)
        }
    }

    override fun initData() {
        getRecommendation()
    }

    private fun getRecommendation() {
        lifecycleScope.launch {
            val token = TokenUtil.getToken()
            if (token != null) {
                val videoBeanList = videoDetailViewModel.getRecommendation(
                    token,
                    videoBean.category,
                    videoBean.genre
                )
                if (videoBeanList.isNotEmpty()) {
                    binding.recommendation.visibility = View.VISIBLE
                    binding.recyclerView.adapter = VideoAdapter(
                        null,
                        null,
                        this@VideoDetailActivity,
                        if (videoBeanList.size > 18) videoBeanList.subList(0, 18) else videoBeanList
                    )
                } else {
                    binding.recommendation.visibility = View.GONE
                    binding.recyclerView.adapter = null
                }
            } else {
                binding.recommendation.visibility = View.GONE
                binding.recyclerView.adapter = null
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        videoBean = intent.getParcelableExtra(Constants.KEY_VIDEO_BEAN)!!
        binding.root.smoothScrollTo(0, 0)
        setView()
        setTags()
        getRecommendation()
    }
}