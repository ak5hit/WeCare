package com.noblegas.wecare.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.noblegas.wecare.R
import com.noblegas.wecare.adapters.ImageSliderAdapter
import com.noblegas.wecare.models.Medicine
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_add_medicine.*
import org.jetbrains.anko.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddMedicineActivity : AppCompatActivity() {

    private var mExpiryDate: Long = 0
    private var mImagesUploaded: Int = 0
    private lateinit var mSelectedImages: ArrayList<File>
    private lateinit var mSliderAdapter: ImageSliderAdapter

    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mAvailableMedicinesDBRef: DatabaseReference
    private var mCurrentMedDBKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medicine)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mSelectedImages = ArrayList()

        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mAvailableMedicinesDBRef = mFirebaseDatabase.getReference(AVAILABLE_MEDICINES)

        mSliderAdapter = ImageSliderAdapter(this, mSelectedImages)
        image_slider.adapter = mSliderAdapter
        image_slider_tab_layout.setupWithViewPager(image_slider)

        expiry_date_tv.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val dateFormat = SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH)
                val date = dateFormat.parse("$day/$month/$year")
                mExpiryDate = date.time
                expiry_date_tv.text = "$day/${month + 1}/$year"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))

            dpd.datePicker.minDate = System.currentTimeMillis().minus(1000)
            dpd.show()
        }

        radio_group_quantity.setOnCheckedChangeListener { radioGroup, _ ->
            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_tablet -> quantity_unit.text = getString(R.string.tablets_string)
                R.id.radio_syrup -> quantity_unit.text = getString(R.string.ml_string)
            }
        }

        choose_images_button.setOnClickListener {
            handleStorageReadPermission()
        }
    }

    private fun uploadDataToDatabase() {
        val quantityUnit = quantity_unit.text.toString()
        val medicineName = medicine_name_input.text.toString()
        val medicineQuantity = quantity_input.text.toString().toLong()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val medicine = Medicine(
            currentUser!!.uid, medicineQuantity,
            quantityUnit, mExpiryDate, medicineName
        )

        val pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)
        pd.setMessage("Uploading...")
        pd.isIndeterminate = true
        pd.show()

        mCurrentMedDBKey = mAvailableMedicinesDBRef.push().key
        if (mCurrentMedDBKey != null) {
            mAvailableMedicinesDBRef.child(mCurrentMedDBKey!!).setValue(medicine)
                .addOnSuccessListener {
                    toast("Details Saved.")
                    val imageStorageRef = mFirebaseStorage.getReference(mCurrentMedDBKey!!)
                    for (i in 0 until mSelectedImages.size) {
                        val currentImage = mSelectedImages[i]
                        imageStorageRef.child(currentImage.name).putFile(Uri.fromFile(currentImage))
                            .addOnSuccessListener {
                                if (++mImagesUploaded == mSelectedImages.size) {
                                    pd.setMessage("Images Uploaded: $mImagesUploaded")
                                    longToast("All Images Uploaded.")
                                    pd.dismiss()
                                    finish()
                                } else pd.setMessage("Images Uploaded: $mImagesUploaded")
                            }
                            .addOnFailureListener { exception ->
                                longToast("Error: ${exception.message}")
                                pd.dismiss()
                            }
                    }
                }
                .addOnFailureListener {
                    longToast("Error: ${it.message}")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CHOOSE_PHOTOS) {
                if (data?.data != null) {
                    // User selected single image
                    val actualFile = File(getRealPath(data.data))
                    val compressedFile = Compressor(this).compressToFile(actualFile)
                    mSelectedImages.add(compressedFile)
                } else if (data?.clipData != null) {
                    // User selected multiple images
                    val numberOfImages = data.clipData!!.itemCount
                    for (i in 0 until numberOfImages) {
                        val actualImage = File(getRealPath(data.clipData!!.getItemAt(i).uri))
                        val compressedImage = Compressor(this).compressToFile(actualImage)
                        mSelectedImages.add(compressedImage)
                    }
                }
                image_slider_tab_layout.visibility = View.VISIBLE
                image_slider.apply {
                    visibility = View.VISIBLE
                    adapter!!.notifyDataSetChanged()
                }
            } else if (requestCode == REQUEST_TAKE_PHOTOS && data?.data != null) {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_add_medicine_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }

            R.id.action_save -> {
                if (isInputsValid()) {
                    uploadDataToDatabase()
                }
            }

            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_STORAGE_PERMISSION) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startImageChooser()
            }
        }
    }

    private fun isInputsValid(): Boolean {
        return when {
            medicine_name_input.text.isNullOrEmpty() -> {
                medicine_name_input.error = getString(R.string.empty_input_error)
                false
            }
            quantity_input.text.isNullOrEmpty() -> {
                quantity_input.error = getString(R.string.empty_input_error)
                false
            }
            mExpiryDate == 0L -> {
                longToast(getString(R.string.expiry_date_error))
                false
            }
            image_slider.adapter!!.count == 0 -> {
                longToast(getString(R.string.no_image_selected_error))
                false
            }
            else -> true
        }
    }

    private fun startImageChooser() {
        val chooseIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseIntent.type = "image/*"
        chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(chooseIntent, REQUEST_CHOOSE_PHOTOS)
    }

    private fun handleStorageReadPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                longToast(getString(R.string.storage_permission_rationale))
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), RC_STORAGE_PERMISSION
                )
            }
        } else {
            startImageChooser()
        }
    }

    // This method is copied and it just works so lets just don't talk about that
    private fun getRealPath(fileUri: Uri?): String? {
        var filePath = ""
        val wholeID = DocumentsContract.getDocumentId(fileUri)

        // Split at colon, use second item in the array
        val id = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

        val column = arrayOf(MediaStore.Images.Media.DATA)

        // where id is equal to
        val sel = MediaStore.Images.Media._ID + "=?"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(id), null
        )

        if (cursor != null) {
            val columnIndex = cursor.getColumnIndex(column[0])

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return filePath
    }

    override fun onBackPressed() {
        alert(getString(R.string.editingActivityBackButtonAlert)) {
            positiveButton(getString(R.string.keep_editing)) {
                it.dismiss()
            }
            negativeButton(getString(R.string.discard)) {
                super.onBackPressed()
            }
        }.show()
    }

    private companion object {
        private const val REQUEST_CHOOSE_PHOTOS = 0
        private const val REQUEST_TAKE_PHOTOS = 1
        private const val RC_STORAGE_PERMISSION = 101

        private const val AVAILABLE_MEDICINES = "availableMedicines"
    }
}
