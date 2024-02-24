package com.example.omega_tracker.ui.screens.profile

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.databinding.ActivityProfileBinding
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.ui.base_class.BaseActivity
import com.example.omega_tracker.ui.screens.authorization.AuthorizationActivity
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener

class ProfileActivity : BaseActivity(R.layout.activity_profile), ProfileView {
    companion object {
        fun createIntentProfile(context: Context): Intent {
            return Intent(
                context, ProfileActivity::class.java
            )
        }
    }

    lateinit var binding: ActivityProfileBinding

    lateinit var radioButtonDrums: RadioButton
    lateinit var radioButtonYouTrack: RadioButton
    lateinit var buttonExit: Button
    override val presenter: ProfilePresenter by providePresenter {
        ProfilePresenter(Settings(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }

        radioButtonDrums = binding.radiobuttonDrums
        radioButtonYouTrack = binding.radiobuttonYoutrack
        buttonExit = binding.buttonExitFromProfile
        radioButtonDrums.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                presenter.changeTypeEnterTime(true)
                radioButtonDrums.isChecked = true
                radioButtonYouTrack.isChecked = !radioButtonDrums.isChecked
            }
        }
        radioButtonYouTrack.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                presenter.changeTypeEnterTime(false)
                radioButtonYouTrack.isChecked = true
                radioButtonDrums.isChecked = !radioButtonYouTrack.isChecked
                log("YouTrack")
            }
        }
        buttonExit.setOnClickListener{
            showAlertDialog()
        }


    }

    private fun getToken(): String? {
        val settings = Settings(this)
        return settings.getToken()
    }

    override fun loadImageProfile(uri: Uri) {
        Glide.with(this).load(uri).error(
            GlideToVectorYou.init().with(this@ProfileActivity)
                .withListener(object : GlideToVectorYouListener {
                    override fun onLoadFailed() {
                        Toast.makeText(
                            this@ProfileActivity, "Load image failed", Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onResourceReady() {
                        Toast.makeText(this@ProfileActivity, "Image ready", Toast.LENGTH_SHORT)
                            .show()
                    }
                }).load(uri, binding.imageProfile)
        ).into(binding.imageProfile)
    }

    override fun setProfile(profile: Profile) {
        binding.userName.text = profile.name
        binding.email.text = profile.email
    }

    override fun setStateRadioButton(state: Boolean) {
        radioButtonDrums.isChecked = state
        radioButtonYouTrack.isChecked = !radioButtonDrums.isChecked
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.сonfirmation))
        builder.setMessage(getString(R.string.are_you_sure_to_log_out_of_your_profile))

        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->

            presenter.deleteToken()
            presenter.clearDataBase()
            gotoAuth()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton(getString(R.string.no)) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun gotoAuth() {
        startActivity(AuthorizationActivity.createIntent(this))
    }
}