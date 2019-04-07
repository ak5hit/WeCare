package com.noblegas.wecare.adapters

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.noblegas.wecare.R
import com.noblegas.wecare.models.Medicine
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AvailableMedicinesListAdapter(
    private val mContext: Context,
    private var mAvailableMedicinesData: ArrayList<DataSnapshot>
) : RecyclerView.Adapter<AvailableMedicinesListAdapter.AvailMedViewHolder>() {

    private val mLayoutInflater = LayoutInflater.from(mContext)
    private val mFirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailMedViewHolder {
        val view = mLayoutInflater.inflate(R.layout.item_available_medicines_list, parent, false)
        return AvailMedViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvailMedViewHolder, position: Int) {
        holder.bind(mAvailableMedicinesData[position])
    }

    override fun getItemCount(): Int {
        return mAvailableMedicinesData.size
    }

    inner class AvailMedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val medName = itemView.findViewById<TextView>(R.id.item_medicine_name)!!
        private val imageSlider = itemView.findViewById<ViewPager>(R.id.item_image_slider)!!
        private val dotIndicator = itemView.findViewById<TabLayout>(R.id.item_dot_indicator)!!
        private val medExpiryDate = itemView.findViewById<TextView>(R.id.item_expiry_date)!!
        private val medQuantity = itemView.findViewById<TextView>(R.id.item_quantity)!!
        private val userProfileImage = itemView.findViewById<ImageView>(R.id.item_user_profile_image)!!
        private val userName = itemView.findViewById<TextView>(R.id.item_user_name)!!

        fun bind(medicineData: DataSnapshot) {
            val medicine = medicineData.getValue(Medicine::class.java)
            if (medicine != null) {
                medName.text = medicine.name
                userName.text = medicine.userID.substring(0, 5)
                medExpiryDate.text = formatDate(medicine.expiryDate)
                    medQuantity.text = "${medicine.quantity} ${medicine.quantityUnit}"
/*
                Glide.with(mContext)
                    .load(FirebaseAuth.getInstance().currentUser!!.photoUrl)
                    .apply(RequestOptions().optionalCircleCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userProfileImage)
*/

                setupImageSlider(medicine.imageUrls)
            }
        }

        private fun setupImageSlider(imageUrlsHash: HashMap<String, String>) {
            val imageUrls = ArrayList<String>()
            for (keyValue in imageUrlsHash) {
                imageUrls.add(keyValue.value)
            }
            val sliderAdapter = AvailableMedImageSliderAdapter(mContext, imageUrls)
            imageSlider.adapter = sliderAdapter
            dotIndicator.setupWithViewPager(imageSlider)
        }

        private fun formatDate(date: Long): String {
            val dateFormat = SimpleDateFormat("d MMM yy", Locale.ENGLISH)
            return dateFormat.format(Date(date))
        }
    }
}