package com.example.omega_tracker.ui.screens.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.DateFormatSymbols
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.RelativeLayout.LayoutParams
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.omega_tracker.Constants
import com.example.omega_tracker.R
import com.example.omega_tracker.data.RunningTask
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.databinding.ActivityMainBinding
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ForegroundService
import com.example.omega_tracker.ui.base_class.BaseActivity
import com.example.omega_tracker.ui.screens.Profile.ProfileActivity.Companion.createIntentProfile
import com.example.omega_tracker.ui.screens.authorization.AuthorizationActivity
import com.example.omega_tracker.ui.screens.main.modelrecycleview.*
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import com.omega_r.libs.omegaintentbuilder.utils.ExtensionUtils.Companion.isNullOrLessZero
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseActivity(R.layout.activity_main), MainView,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener, OnItemClickListener {

    companion object {
        fun createIntentMainActivity(context: Context): Intent {
            return Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    private lateinit var binding: ActivityMainBinding

    override val presenter: MainPresenter by providePresenter {
        MainPresenter(getToken()!!, Settings(this), adapter)
    }

    //Переменные для кастомных задач
    private lateinit var dialog: Dialog
    private lateinit var dialogLoading: Dialog
    private var yearFromCalendar = 0
    private var monthFromCalendar = 0
    private var monthForLabel = ""
    private var dayFromCalendar = 0
    private var hourFromCalendar = 0
    private var minuteFromCalendar = 0
    private var day: Int? = null
    private var month: Int? = null
    private var hour: Int? = null
    private var minute: Int? = null
    private lateinit var nameProjects: MutableList<String>

    private lateinit var connection: ServiceConnection

    private var currentStateList = true
    private var onFirst = true

    //RecycleView
    private val adapter = MultiViewAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter.setProfile()
        dialogLoading =
            Dialog(this)
        nameProjects = mutableListOf(getString(R.string.custom_task))

        binding.recycleView.layoutManager = LinearLayoutManager(this)
        binding.recycleView.adapter = adapter

        binding.buttonSettings.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            openPopupMenu(popupMenu)
        }
        // Добавить кастомную задачу
        binding.buttonAddCustomTasks.setOnClickListener {
            showCreateCustomTaskDialog()
        }

        presenter.getAllNameProjects()
        presenter.getTaskForRestore()
    }

    override fun onStart() {
        super.onStart()
        connection = presenter.returnConnection()
        Intent(this, ForegroundService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }
    }


    override fun onResume() {
        super.onResume()
        presenter.updateListTask(currentStateList)
        presenter.getNotRunningTask()
    }

    override fun removeNotRunningTask(notRunningTask: RunningTask) {
        adapter.removeNotRunningTask(notRunningTask)
    }

    //Получение названий проектов
    override fun getAllNameProjects(allNameProjects: List<String>?) {
        if (allNameProjects != null) {
            nameProjects.addAll(allNameProjects)
        }
    }

    override fun onClickItemChange(): Boolean {
        currentStateList = !currentStateList
        if (currentStateList) {
            loadCurrentDataTasks()
        } else {
            loadAllTasks()
        }
        return currentStateList
    }

    // Обновить список запущенных задач
    override fun updateRunningTask(listRunningTask: RunningTask) {
        adapter.updateRunningTask(listRunningTask)
    }

    override fun addItemRunningTask(itemLRunningTask: RunningTask) {
        adapter.addRunningTask(itemLRunningTask)
    }

    // Восстановить запущенные задачи
    override fun restoreTask(task: Task) {
        startForegroundService(ForegroundService.startTimerService(this, task))
    }

    // Получить токен
    private fun getToken(): String? {
        val settings = Settings(this)
        return settings.getToken()
    }

    // Показать изображение профиля
    override fun loadImageProfile(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .error(
                GlideToVectorYou.init().with(this@MainActivity)
                    .withListener(object : GlideToVectorYouListener {
                        override fun onLoadFailed() {
                            Toast.makeText(
                                this@MainActivity,
                                "Load image failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onResourceReady() {
                            Toast.makeText(this@MainActivity, "Image ready", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }).load(uri, binding.imageProfile)
            ).into(binding.imageProfile)
    }

    // Загрузить задачи на сегодня
    override fun loadCurrentDataTasks() {
        presenter.loadCurrentDataTask(adapter, onFirst)
        onFirst = false
    }

    // Загрузить все задачи
    override fun loadAllTasks() {
        presenter.loadAllTasks(adapter, onFirst)
        onFirst = false
    }


    // Убрать анимацию загрузки
    override fun removeLoadBar() {
        if (dialogLoading.isShowing) {
            dialogLoading.dismiss()
        }
        return
    }

    // Восстановить анимацию загрузки
    override fun restoreLoadBar() {

        dialogLoading.setContentView(R.layout.dialog_loading)
        dialogLoading.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        dialogLoading.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        binding.progressBarLoading.visibility = View.VISIBLE
//        binding.buttonSettings.isEnabled = false
        dialogLoading.show()
    }

    // Перейти на активити авторизации
    override fun gotoAuth() {
        startActivity(AuthorizationActivity.createIntent(this))
    }

    // Открыть меню
    private fun openPopupMenu(popupMenu: PopupMenu) {
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.exitUser -> {
                    showToast(Constants.TOAST_TYPE_INFO, R.string.come_out)
                    presenter.deleteToken()
                    presenter.clearDataBase()
                    gotoAuth()
                    true
                }
                R.id.settings -> {
                    startActivity(createIntentProfile(this))
                    true
                }
                else -> false
            }
        }
        popupMenu.inflate(R.menu.menu_main)
        popupMenu.show()
    }

    private fun showCreateCustomTaskDialog() {
        dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_new_custom_task)

        val summary = dialog.findViewById<EditText>(R.id.title)
        val description = dialog.findViewById<EditText>(R.id.comment)
        var estimate = 0.minutes
        var projectName = ""
        var timeLeft: Duration
        var minutes: Duration = 0.minutes
        var hours: Duration = 0.hours
        var days: Duration = 0.days

        val inputType: Boolean

        val termYouTrack = dialog.findViewById<EditText>(R.id.edittext_term_youtrack)
        val termCustom = dialog.findViewById<LinearLayout>(R.id.linearlayout_term_custom)

        val spinnerProjects = dialog.findViewById<Spinner>(R.id.spinner_projects)
        val chooseDataTime = dialog.findViewById<TextView>(R.id.textview_change_date)

        if (presenter.getInputTypeTime()) {
            inputType = true
            termYouTrack.visibility = View.VISIBLE
            termCustom.visibility = View.GONE

            try {
                termYouTrack.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {
                        val input = p0.toString()
                        if (input.length > 1) {
                            val lastChar = input.substring(input.length - 1)
                            val beforeLastChar = input.substring(0, input.length - 1)
                            if (lastChar.matches("[а-яА-Я]".toRegex()) && beforeLastChar.contains(
                                    lastChar
                                )
                            ) {
                                termYouTrack.removeTextChangedListener(this)
                                termYouTrack.setText(beforeLastChar)
                                termYouTrack.setSelection(beforeLastChar.length)
                                termYouTrack.addTextChangedListener(this)
                            }
                        }
                    }
                })
            } catch (e: java.lang.IllegalArgumentException) {
                termYouTrack.text.clear()
                showToast(Constants.TOAST_TYPE_ERROR, R.string.error_in_field_term)
            }

        } else {
            inputType = false
            termYouTrack.visibility = View.GONE
            termCustom.visibility = View.VISIBLE

            dialog.findViewById<NumberPicker>(R.id.numberpicker_minuts).apply {
                minValue = 0
                maxValue = 59
                value = 0
                setOnValueChangedListener { _, _, newVal ->
                    minutes = newVal.minutes
                    estimate = minutes + hours + days
                }
            }

            dialog.findViewById<NumberPicker>(R.id.numberpicker_hours).apply {
                minValue = 0
                maxValue = 8
                value = 0
                setOnValueChangedListener { _, _, newVal ->
                    hours = newVal.hours
                    estimate = minutes + hours + days
                }
            }

            dialog.findViewById<NumberPicker>(R.id.numberpicker_days).apply {
                maxValue = 6
                minValue = 0
                value = 0
                setOnValueChangedListener { _, _, newVal ->
                    days = newVal.days
                    estimate = minutes + hours + days
                }
            }

        }

        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_row, R.id.text_row, nameProjects!!)
        spinnerProjects.adapter = arrayAdapter
        spinnerProjects.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                projectName = nameProjects!![p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        chooseDataTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            yearFromCalendar = calendar.get(Calendar.YEAR)
            monthFromCalendar = calendar.get(Calendar.MONTH)
            dayFromCalendar = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(
                dialog.context,
                R.style.DataPickerDialogTheme,
                this,
                yearFromCalendar,
                monthFromCalendar,
                dayFromCalendar
            ).show()
        }

        val yesButton = dialog.findViewById<CardView>(R.id.button_ok)
        val noButton = dialog.findViewById<CardView>(R.id.button_cancel)

        yesButton.setOnClickListener {
            if (inputType) {
                if (termYouTrack.text.toString().isNullOrEmpty()) {
                    showToast(Constants.TOAST_TYPE_WARNING, R.string.fill_all_fields)
                    return@setOnClickListener
                }
                val duration = convertStringToDuration(termYouTrack.text.toString())
                if (duration != null) {
                    estimate = duration
                } else {
                    termYouTrack.text.clear()
                    return@setOnClickListener
                }
            } else {
                if (dayFromCalendar == 0 || month.isNullOrLessZero() || summary.text.isEmpty() || estimate == Duration.ZERO) {
                    showToast(Constants.TOAST_TYPE_WARNING, R.string.fill_all_fields)
                    return@setOnClickListener
                }
            }
            timeLeft = estimate
            val customTask = CustomTask(
                idTask = generateRandomId(5),
                nameProject = projectName,
                summary = summary.text.toString(),
                description = description.text?.toString() ?: "",
                estimate = estimate,
                startDateTime = convertDateCalendarToLocalDataTime(),
                timeLeft = timeLeft,
                taskLaunchTime = null
            )
            presenter.createCustomTask(customTask)

            showToast(Constants.TOAST_TYPE_INFO, R.string.custom_task_create)
            dayFromCalendar = 0
            month = null
            hourFromCalendar = 0
            minuteFromCalendar = 0
            dialog.dismiss()

            presenter.updateListTask(currentStateList)
        }

        noButton.setOnClickListener {
            showToast(Constants.TOAST_TYPE_INFO, R.string.canceled)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun convertDateCalendarToLocalDataTime(): Long {
        val date = LocalDateTime.of(
            yearFromCalendar,
            month!!,
            day!!,
            hourFromCalendar,
            minuteFromCalendar,
            0,
            0
        )
        return date.toEpochSecond(ZoneOffset.UTC) * 100
    }

    private fun convertStringToDuration(text: String): Duration? {
        val modifiedText = text.replace("д", "d").replace("ч", "h").replace("м", "m")

        var result: Duration? = null

        try {
            result = Duration.parse(modifiedText)
        } catch (e: java.time.format.DateTimeParseException) {
            e.printStackTrace()
            showToast(Constants.TOAST_TYPE_WARNING, R.string.error_in_field_term)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(Constants.TOAST_TYPE_WARNING, R.string.error_in_field_term)
        }

        return result
    }

    private fun generateRandomId(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return charPool.shuffled().take(length).toSet().joinToString("")
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[Calendar.MONTH] = monthOfYear
        month = monthOfYear + 1
        monthForLabel = DateFormatSymbols().months[calendar[Calendar.MONTH]]
        day = dayOfMonth
        TimePickerDialog(
            this,
            R.style.TimePickerDialogTheme,
            this,
            hourFromCalendar,
            minuteFromCalendar,
            true
        ).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minuteOfHour: Int) {
        val textView = dialog.findViewById<TextView>(R.id.textview_change_date)
        hour = hourOfDay
        minute = minuteOfHour
        minuteFromCalendar = minuteOfHour
        hourFromCalendar = hourOfDay

        var hourFromLabel = ""
        var minuteFromLabel = ""

        hourFromLabel = if (hour!! < 10) {
            "0$hour"
        } else "$hour"
        minuteFromLabel = if (minute!! < 10) {
            "0$minute"
        } else "$minute"
        val textDeadLine = "$day $monthForLabel $hourFromLabel:$minuteFromLabel"
        textView.setTextColor(getColor(R.color.white))
        textView.text = textDeadLine
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    override fun log(message: String) {
        Log.d("MyLog", message)
    }

    override fun onClickRunningTask(item: UiModel.RunningTaskModel, position: Int) {
        TODO("Not yet implemented")
    }


}