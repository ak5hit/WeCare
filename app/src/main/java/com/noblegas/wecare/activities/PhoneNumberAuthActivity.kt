package com.noblegas.wecare.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.noblegas.wecare.R
import com.noblegas.wecare.misc.OTPInputWatcher
import kotlinx.android.synthetic.main.activity_phone_number_auth.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class PhoneNumberAuthActivity : AppCompatActivity() {

    private var mStoredVerificationId: String? = ""
    private var mStoredToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var mPhoneNumberVerificationStateChangeListener: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number_auth)
        title = getString(R.string.enter_phone_number)

        verify_number_button.setOnClickListener {
            if (!phone_number_input.text.isNullOrEmpty()) {
                showProgress()
                title = getString(R.string.enter_security_code)
                phone_number_tv.text = getEnteredPhoneNumber()
                startPhoneNumberVerification(getEnteredPhoneNumber())
            } else {
                phone_number_input.error = getString(R.string.invalid_number_input_error)
                hideProgress()
            }
        }

        verify_otp_button.setOnClickListener {
            val code = getUserInputOTP()
            if (code.length != 6) {
                Snackbar.make(verify_otp_group, getString(R.string.enter_valid_otp), Snackbar.LENGTH_SHORT)
                hideProgress()
            } else {
                showProgress()
                verifyPhoneNumberWithCode(getUserInputOTP())
            }
        }

        mPhoneNumberVerificationStateChangeListener =
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(credentials: PhoneAuthCredential?) {

                        longToast("Verification Done Automatically.")
                        linkAccountWithGoogleAccount(credentials)
                        finish()
                    }

                    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                        super.onCodeSent(verificationId, token)

                        mStoredToken = token
                        mStoredVerificationId = verificationId

                        hideProgress()
                        startResendTimer()
                        loadOTPVerificationGroup()
                    }

                    override fun onVerificationFailed(exception: FirebaseException?) {
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            Snackbar.make(phone_number_input, "Invalid phone number.", Snackbar.LENGTH_SHORT)
                        } else {
                            longToast("Something went Wrong!! Try Again.")
                            hideProgress()
                        }
                    }
                }

        initializeOTPInputView()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            mPhoneNumberVerificationStateChangeListener
        )
    }

    private fun verifyPhoneNumberWithCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(mStoredVerificationId!!, code)
        linkAccountWithGoogleAccount(credential)
    }

    private fun linkAccountWithGoogleAccount(credential: PhoneAuthCredential?) {
        FirebaseAuth.getInstance().currentUser!!.updatePhoneNumber(credential!!)
            .addOnSuccessListener {
                finish()
            }
            .addOnFailureListener {
                toast("Enter Valid OTP")
                cleanOTPInputs()
                hideProgress()
            }
    }

    private fun loadOTPVerificationGroup() {
        disableVerifyOTPButton()
        verify_otp_group.visibility = View.VISIBLE
        verify_number_group.visibility = View.GONE
    }

    private fun initializeOTPInputView() {
        otp_input_1.addTextChangedListener(OTPInputWatcher(otp_input_1, otp_input_2))
        otp_input_2.addTextChangedListener(OTPInputWatcher(otp_input_1, otp_input_3))
        otp_input_3.addTextChangedListener(OTPInputWatcher(otp_input_2, otp_input_4))
        otp_input_4.addTextChangedListener(OTPInputWatcher(otp_input_3, otp_input_5))
        otp_input_5.addTextChangedListener(OTPInputWatcher(otp_input_4, otp_input_6))
        otp_input_6.addTextChangedListener(OTPInputWatcher(otp_input_5, otp_input_6))
        otp_input_6.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (otp_input_6.text.isNullOrEmpty()) {
                    verify_otp_button.isEnabled = false
                    verify_otp_button.background.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY)
                } else {
                    verify_otp_button.isEnabled = true
                    verify_otp_button.background.colorFilter = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun startResendTimer() {
        object : CountDownTimer(30000, 1000) {
            override fun onFinish() {
                resend_code_TV.isClickable = true
                resend_code_TV.text = getString(R.string.resend_code)
                resend_code_TV.setTextColor(Color.parseColor("#4A85E3"))
            }

            override fun onTick(millisUntilFinished: Long) {
                resend_code_TV.isClickable = false
                resend_code_TV.text = "Resend 00:${String.format("%02d", millisUntilFinished.div(1000).toInt())}"
            }
        }.start()
    }

    private fun getEnteredPhoneNumber(): String {
        val countryCode = country_code_picker.selectedCountryCodeWithPlus
        val phoneNumber = phone_number_input.text
        return countryCode + phoneNumber
    }

    private fun getUserInputOTP(): String {
        return "${otp_input_1.text}${otp_input_2.text}${otp_input_3.text}" +
                "${otp_input_4.text}${otp_input_5.text}${otp_input_6.text}"
    }

    private fun showProgress() {
        disableVerifyOTPButton()
        disableVerifyNumberButton()
        verification_progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        enableVerifyOTPButton()
        enableVerifyNumberButton()
        verification_progress.visibility = View.INVISIBLE
    }

    private fun enableVerifyNumberButton() {
        verify_number_button.isEnabled = true
        verify_number_button.background.colorFilter = null
    }

    private fun disableVerifyNumberButton() {
        verify_number_button.isEnabled = false
        verify_number_button.background.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY)
    }

    private fun enableVerifyOTPButton() {
        verify_otp_button.isEnabled = true
        verify_otp_button.background.colorFilter = null
    }

    private fun disableVerifyOTPButton() {
        verify_otp_button.isEnabled = false
        verify_otp_button.background.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY)
    }

    private fun cleanOTPInputs() {
        otp_input_1.setText("")
        otp_input_2.setText("")
        otp_input_3.setText("")
        otp_input_4.setText("")
        otp_input_5.setText("")
        otp_input_6.setText("")
        otp_input_1.requestFocus()
    }

    override fun onBackPressed() {
        longToast(getString(R.string.mandatory_verification_message))
    }
}
