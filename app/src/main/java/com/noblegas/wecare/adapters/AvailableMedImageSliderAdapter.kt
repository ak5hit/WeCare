package com.noblegas.wecare.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.noblegas.wecare.R

class AvailableMedImageSliderAdapter(private val mContext: Context, private val mImageUrls: ArrayList<String>): PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_image_slider, container, false)
        val imageView = view.findViewById<ImageView>(R.id.image_view)
        Glide.with(mContext)
            .load(mImageUrls[position])
            .transition(DrawableTransitionOptions().crossFade())
            .into(imageView)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun getCount(): Int {
        return mImageUrls.size
    }
}