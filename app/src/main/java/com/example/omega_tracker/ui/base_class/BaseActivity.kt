package com.example.omega_tracker.ui.base_class

import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import com.example.omega_tracker.Constants
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.omega_r.base.components.OmegaActivity
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.mvp.views.OmegaView
import com.omegar.mvp.MvpAppCompatActivity
import com.omegar.mvp.MvpPresenter

abstract class BaseActivity : OmegaActivity,BaseView {

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    abstract override val presenter: OmegaPresenter<out OmegaView>

    override fun log(message: String) {
        Log.d("MyLog", "$message")
    }

    override fun showToast(toastType: Int, message: Int) {
        val toast = layoutInflater.inflate(
            R.layout.layout_custom_toast_error, findViewById(R.id.toast_message)
        )
        val toastContainer = toast.findViewById<LinearLayout>(R.id.toast_message)
        val textMessage = toast.findViewById<TextView>(R.id.text_toast_message)

        when (toastType) {
            Constants.TOAST_TYPE_WARNING -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_warning)
                textMessage.text = getString(message)
            }
            Constants.TOAST_TYPE_ERROR -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_error)
                textMessage.text = getString(message)
            }
            Constants.TOAST_TYPE_SUCCESS -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_success)
                textMessage.text = getString(message)
            }
            Constants.TOAST_TYPE_INFO -> {
                toastContainer.setBackgroundResource(R.drawable.bg_toast_info)
                textMessage.text = getString(message)
            }
        }
        with(Toast(applicationContext)) {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.CENTER, 0, 480)
            view = toast
            show()
        }
    }
}