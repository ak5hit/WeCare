package com.noblegas.wecare.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.noblegas.wecare.R
import com.noblegas.wecare.adapters.AvailableMedicinesListAdapter
import com.noblegas.wecare.models.Medicine
import kotlinx.android.synthetic.main.fragment_available_medicines.*

class AvailableMedicinesFragment : Fragment() {

    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mAvailableMedicinesDBRef: DatabaseReference
    private lateinit var mAvailableMedicineChildEventListener: ChildEventListener

    private lateinit var mAvailableMedicinesData: ArrayList<DataSnapshot>
    private lateinit var mAvailableMedicinesAdapter: AvailableMedicinesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mAvailableMedicinesDBRef = mFirebaseDatabase.getReference(AVAILABLE_MEDICINES)
//        mFirebaseDatabase.setPersistenceEnabled(true)

        mAvailableMedicinesData = ArrayList()
        mAvailableMedicinesAdapter = AvailableMedicinesListAdapter(context!!, mAvailableMedicinesData)

        mAvailableMedicineChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                mAvailableMedicinesData.add(0, dataSnapshot)
                mAvailableMedicinesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_available_medicines, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAvailableMedicinesDBRef.addChildEventListener(mAvailableMedicineChildEventListener)
        available_medicines_rv.layoutManager = LinearLayoutManager(context)
        available_medicines_rv.adapter = mAvailableMedicinesAdapter
    }

    override fun onDetach() {
        super.onDetach()
        mAvailableMedicinesDBRef.removeEventListener(mAvailableMedicineChildEventListener)
    }

    private companion object {
        private const val AVAILABLE_MEDICINES = "availableMedicines"
    }
}
