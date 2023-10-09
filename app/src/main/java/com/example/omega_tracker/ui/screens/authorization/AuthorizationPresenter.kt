package com.example.omega_tracker.ui.screens.authorization

import com.example.omega_tracker.Constants
import com.example.omega_tracker.R
import com.example.omega_tracker.data.repository.local_data.Settings
import com.example.omega_tracker.data.repository.local_data.Tasks_DAO
import com.example.omega_tracker.data.repository.remote_data.api.InterfaceAuthModel
import com.example.omega_tracker.ui.base_class.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject


class AuthorizationPresenter @Inject constructor(
    retrofit: Retrofit,
    private val view: AuthorizationView,
    private val dataBaseTasks: Tasks_DAO
) : BasePresenter() {

    private val interfaceAuthModel = InterfaceAuthModel(Dispatchers.Main,retrofit,dataBaseTasks)
    private fun checkIsEmptyToken(token: String): Boolean {
        return token.isEmpty()
    }

    private fun checkToken(token: String): String {
        return if (token.contains("perm:"))
            "Bearer $token"
        else "Bearer perm:$token"
    }

    fun auth(token: String) {
        if (checkIsEmptyToken(token)) {
            view.tokenIsEmpty()
        } else {
            val fullToken = checkToken(token)
            requestApi(fullToken)
        }
    }
    private fun requestApi(token: String) {
        view.loadStart()
        launch {
                val answer = interfaceAuthModel.getAuthResult(token)
                answerDecision(answer, token)
        }
    }

    private fun answerDecision(answer: Boolean, token: String) {
        if (answer) {
            receivedGoodAnswer(token)
        } else {
            if (view.checkInternet())
                receivedBadAnswer()
            else
                receiveNoInternet()
        }
    }

    private fun receivedGoodAnswer(token: String) {
        saveToken(view.initCacheToken(), token)
        view.loadEnd()
        view.showToast(Constants.TOAST_TYPE_SUCCESS, R.string.successfully)
        view.setButtonColorRight()
        view.goMainScreen(token)
    }

    private fun receivedBadAnswer() {
        view.loadEnd()
        view.setButtonColorError()
        view.showToast(Constants.TOAST_TYPE_ERROR, R.string.invalidToken)
    }

    private fun receiveNoInternet(){
        view.loadEnd()
        view.setButtonColorWarning()
        view.showToast(Constants.TOAST_TYPE_WARNING, R.string.no_internet)
    }

    private fun saveToken(authCacheToken: Settings, token: String) {
        authCacheToken.saveString(token)
    }

}