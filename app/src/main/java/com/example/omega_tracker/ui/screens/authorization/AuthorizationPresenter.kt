package com.example.omega_tracker.ui.screens.authorization

import com.example.omega_tracker.Constants
import com.example.omega_tracker.OmegaTrackerApp
import com.example.omega_tracker.R
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.repository.AppRepository
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.entity.Profile
import com.example.omega_tracker.ui.base_class.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

class AuthorizationPresenter(private val settings: Settings) : BasePresenter<AuthorizationView>() {
    init {
        OmegaTrackerApp.appComponent!!.inject(this)
    }

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBaseTasks: TasksDao

    private val appRepository = AppRepository(Dispatchers.Main, retrofit, dataBaseTasks)

    private fun checkIsEmptyToken(token: String): Boolean {
        return token.isEmpty()
    }

    fun authorization(token: String) {
        if (checkIsEmptyToken(token)) {
            viewState.tokenIsEmpty()
        } else {
            requestApi(checkToken(token))
        }
    }
    private fun checkToken(token: String): String {
        return if (token.contains("perm:"))
            "Bearer $token"
        else "Bearer perm:$token"
    }

    private fun requestApi(token: String) {
        viewState.loadStart()
        launch {
            val answer = appRepository.getAuthResult(token)
            answerDecision(answer, token)
        }
    }

    private fun answerDecision(answer: Profile?, token: String) {
        if (answer == null) {
            viewState.checkInternet(token)
        } else {
            launch {
                val stateTask =appRepository.getStateBundle(token)
                appRepository.setStateTask(stateTask)
            }
            settings.saveProfile(answer)
            receivedGoodAnswer(token)
        }
    }

    private fun receivedGoodAnswer(token: String) {
        viewState.saveToken(token)
        viewState.loadEnd()
        viewState.showToast(Constants.TOAST_TYPE_SUCCESS, R.string.successfully)
        viewState.setButtonColorRight()
        viewState.goMainScreen(token)
    }

    fun receivedBadAnswer() {
        viewState.loadEnd()
        viewState.setButtonColorError()
        viewState.showToast(Constants.TOAST_TYPE_ERROR, R.string.invalidToken)
    }

    fun receiveNoInternet() {
        viewState.loadEnd()
        viewState.setButtonColorWarning()
        viewState.showToast(Constants.TOAST_TYPE_WARNING, R.string.no_internet)
    }
}