package com.noblegas.wecare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.noblegas.wecare.activities.AddMedicineActivity
import com.noblegas.wecare.activities.PhoneNumberAuthActivity
import com.noblegas.wecare.fragments.AvailableMedicinesFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private lateinit var mNavHeader: View
    private lateinit var mNameTV: TextView
    private lateinit var mEmailTV: TextView
    private lateinit var mProfileImage: ImageView

    private var mCurrentUser: FirebaseUser? = null
    private lateinit var mFirebaseAuth: FirebaseAuth

    private lateinit var mFirebaseAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white)
        }

        mNavHeader = nav_view.getHeaderView(0)
        mNameTV = mNavHeader.findViewById(R.id.name)
        mEmailTV = mNavHeader.findViewById(R.id.email)
        mProfileImage = mNavHeader.findViewById(R.id.profile_image)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuthStateListener = FirebaseAuth.AuthStateListener {
            mCurrentUser = mFirebaseAuth.currentUser

            if (mCurrentUser != null) {
                // User is signed in

                onSignInInitialization()
                if (mCurrentUser?.phoneNumber.isNullOrEmpty()) {
                    startActivity(Intent(this, PhoneNumberAuthActivity::class.java))
                }
            } else {
                // User is not signed in

                onSignOutCleanUp()
                createSignInIntent()
            }
        }
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener)

        add_medicine_fab.setOnClickListener { startActivity(Intent(this, AddMedicineActivity::class.java)) }

        setUpNavigationDrawer()
        loadFragment(AvailableMedicinesFragment())
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content_container, fragment).commit()
    }

    private fun createSignInIntent() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    arrayListOf(
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                ).build(),
            RC_SIGN_IN
        )
    }

    private fun setUpNavigationDrawer() {
        nav_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    nav_view.setCheckedItem(R.id.nav_home)
                    title = getString(R.string.nav_home)
                }
                R.id.nav_settings -> {
                    nav_view.setCheckedItem(R.id.nav_settings)
                    title = getString(R.string.nav_settings)
                }
                R.id.nav_about_us -> {
                    toast("Will open about us activity")
                }
                else -> super.onContextItemSelected(menuItem)
            }

            drawer.closeDrawers()
            true
        }
    }

    private fun onSignInInitialization() {
        loadNavHeader()
    }

    private fun onSignOutCleanUp() {
    }

    private fun loadNavHeader() {
        mNameTV.text = mCurrentUser?.displayName
        mEmailTV.text = mCurrentUser?.email

        val photoUrl = mCurrentUser?.photoUrl
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .apply(RequestOptions().circleCrop())
                .transition(DrawableTransitionOptions().crossFade())
                .into(mProfileImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                mCurrentUser = FirebaseAuth.getInstance().currentUser
            } else {
                if (response != null) {
                    toast("Error: ${response.error?.errorCode}")
                } else {
                    Snackbar.make(nav_view, "User Login Required", Snackbar.LENGTH_SHORT).show()
                    createSignInIntent()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            android.R.id.home -> {
                drawer.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_sign_out -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnSuccessListener { toast("Sign Out Successful") }
                    .addOnFailureListener { toast("Sign Out Failed") }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val RC_SIGN_IN = 5
    }
}
