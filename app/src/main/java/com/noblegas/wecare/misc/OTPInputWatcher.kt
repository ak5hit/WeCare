package com.noblegas.wecare.misc

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class OTPInputWatcher(private val previousEditText: EditText, private val nextEditText: EditText) : TextWatcher {

    override fun afterTextChanged(editable: Editable?) {
        val currentText = editable.toString()

        if (currentText.length == 1) {
            nextEditText.requestFocus()
        }

        if (currentText.isEmpty()) {
            previousEditText.requestFocus()
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

}