package com.film.television.adapter

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.film.television.databinding.ItemAdBinding
import com.film.television.utils.AdUtil

class AdViewHolder(private val activity: Activity, private val binding: ItemAdBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(width: Int) {
        AdUtil.showFeedsAd(activity, binding.adContainer, width, 0)
    }

}