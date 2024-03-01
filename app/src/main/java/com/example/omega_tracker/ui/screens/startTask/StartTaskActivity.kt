package com.example.omega_tracker.ui.screens.startTask

import android.Manifest
import android.app.*
import android.content.*
import android.graphics.Typeface
import android.icu.text.DateFormatSymbols
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.work.*
import com.example.omega_tracker.Constants
import com.example.omega_tracker.Constants.CONTINUE
import com.example.omega_tracker.R
import com.example.omega_tracker.data.TaskStatus
import com.example.omega_tracker.data.local_data.Settings
import com.example.omega_tracker.data.local_data.TasksDao
import com.example.omega_tracker.databinding.ActivityStartTaskBinding
import com.example.omega_tracker.entity.Task
import com.example.omega_tracker.service.ForegroundService
import com.example.omega_tracker.service.ForegroundService.Companion.startTimerService
import com.example.omega_tracker.Constants.PAUSE
import com.example.omega_tracker.Constants.STOP
import com.example.omega_tracker.Constants.TOAST_TYPE_WARNING
import com.example.omega_tracker.data.local_data.TaskType
import com.example.omega_tracker.entity.StateTask
import com.example.omega_tracker.service.ForegroundService.Companion.completeTimerService
import com.example.omega_tracker.service.ForegroundService.Companion.stopTimerService
import com.example.omega_tracker.service.ServiceTask
import com.example.omega_tracker.ui.base_class.BaseActivity
import com.example.omega_tracker.ui.screens.main.CustomTask
import com.example.omega_tracker.utils.FormatTime
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import retrofit2.Retrofit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration

class StartTaskActivity : BaseActivity(R.layout.activity_start_task), StartTaskView,
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    companion object {
        fun createPendingIntent(context: Context, id: String): PendingIntent {
            return PendingIntent.getActivity(
                context, 1, createIntentStartTask(context, id), PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun createIntentStartTask(context: Context, id: String): Intent {
            return Intent(context, StartTaskActivity::class.java).putExtra("id", id)
        }
    }

    private lateinit var binding: ActivityStartTaskBinding

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var dataBase: TasksDao

    override val presenter: StartTaskPresenter by providePresenter {
        idTask = intent.getStringExtra("id")!!
        StartTaskPresenter(idTask, getToken()!!, intent, Settings(this))
    }

    private lateinit var popupMenu: PopupMenu
    private lateinit var infoTask: Task
    private lateinit var dialog: Dialog
    private lateinit var connection: ServiceConnection
    private lateinit var dialogLoading: Dialog

    private var nameProjects: MutableList<String> = mutableListOf("Личные задачи")
    private var format = FormatTime
    private var idTask: String = ""
    private var checkPause: Boolean = false
    private var timeLeft: Duration = Duration.ZERO
    private var timeSpent: Duration = Duration.ZERO
    private var timeFromLaunch: Duration = Duration.ZERO
    private var stateTask: List<StateTask>? = arrayListOf()

    private var monthForLabel = ""
    private var day: Int? = 0
    private var minuteFromCalendar = 0
    private var hourFromCalendar = 0
    private var hour = 0
    private var minute = 0


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStartTaskBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
            )
        }
        setContentView(binding.root)

        dialogLoading = Dialog(this)
        val startButton = binding.buttonStartButton
        val completeButton = binding.buttonComplete
        val buttonBack = binding.buttonBack
        val buttonPause = binding.buttonPause
        val buttonContinue = binding.buttonContinue
        val buttonMoreCustomTask = binding.buttonSettings


        presenter.getStateList()
        presenter.getAllNameProjects()

        // Отключаем кнопку до загрузки данных
        startButton.isEnabled = false

        buttonBack.setOnClickListener {
            finish()
        }

        startButton.setOnClickListener {
            visibleButton()
            checkPause = false

            val intent = startTimerService(this, infoTask)

            startForegroundService(intent)
            presenter.updateStatus(infoTask.id, TaskStatus.Run)
            presenter.updateLaunchTime(infoTask.id)
            infoTask.taskStatus = TaskStatus.Run
        }

        completeButton.setOnClickListener {
            val intent =
                Intent(getIntentForeground()).putExtra("ID", infoTask.id).setAction(PAUSE)
            startForegroundService(intent)

            presenter.updateStatus(infoTask.id, TaskStatus.Pause)
            onComplete(timeFromLaunch, stateTask!!)
        }

        buttonContinue.setOnClickListener {
            presenter.updateStatus(infoTask.id, TaskStatus.Run)
            val intent = Intent(getIntentForeground()).putExtra("ID", infoTask.id).setAction(CONTINUE)

            startForegroundService(intent)
            binding.linearlayoutPause.visibility = View.VISIBLE
            binding.linearlayoutContinue.visibility = View.GONE
        }

        binding.circularProgressBarTimer.setOnClickListener {
            if (checkVisibleTextTimer()) showTimeLeft(timeLeft)
            else showCurrentTime(timeSpent)

            val secondElement = binding.textCountTimeForward
            val firstElement = binding.textCountTimeBack

            if (firstElement.visibility == View.VISIBLE) {
                firstElement.visibility = View.GONE
                secondElement.visibility = View.VISIBLE
                binding.circularProgressBarTimer.setProgressWithAnimation(
                    if (timeLeft.inWholeSeconds == 0L) timeSpent.inWholeSeconds.toFloat() else infoTask.usedTime.inWholeSeconds.toFloat(),
                    700
                )
            } else {
                firstElement.visibility = View.VISIBLE
                secondElement.visibility = View.GONE
                binding.circularProgressBarTimer.setProgressWithAnimation(
                    if (timeSpent.inWholeSeconds == 0L) timeLeft.inWholeSeconds.toFloat() else infoTask.remainingTime.inWholeSeconds.toFloat(),
                    700
                )
            }
        }

        buttonPause.setOnClickListener {
            presenter.updateStatus(infoTask.id, TaskStatus.Pause)
            val intent = Intent(getIntentForeground()).putExtra("ID", infoTask.id).setAction(PAUSE)
            startService(intent)
            binding.linearlayoutPause.visibility = View.GONE
            binding.linearlayoutContinue.visibility = View.VISIBLE

            infoTask.taskStatus = TaskStatus.Pause
        }

        buttonMoreCustomTask.setOnClickListener {
            if (infoTask.taskStatus in setOf(TaskStatus.Open, TaskStatus.Pause)) {
                popupMenu = PopupMenu(this, it)
                openPopupMenu(popupMenu)
            } else {
                showToast(TOAST_TYPE_WARNING, R.string.stop_the_task)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        connection = presenter.returnConnection()
        Intent(this, ForegroundService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }
    }
    override fun setTask(taskInfo: Task) {
        infoTask = taskInfo
        timeSpent = taskInfo.usedTime
        timeLeft = taskInfo.remainingTime
        binding.textNameProject.text = taskInfo.nameProject
        binding.textState.text = presenter.getNameCustomField(infoTask.currentState)
        binding.textSummaryTask.text = taskInfo.summary
        binding.textDescriptionTask.text = taskInfo.description

        presenter.setColorTextTimer(taskInfo.remainingTime)
        binding.textCountTimeForward.text = format.formatSeconds(taskInfo.usedTime)
        binding.textCountTimeBack.text = format.formatSeconds(taskInfo.remainingTime)
        setProgressBarTimer(binding.circularProgressBarTimer, taskInfo)
        if (binding.textCountTimeForward.text.isNotEmpty()) {
            binding.buttonStartButton.isEnabled = true
            binding.progressBarLoading.visibility = View.GONE
        }
    }

    override fun getFormatTime(formatTime: FormatTime) {
        format = formatTime
    }

    private fun showChangeOfCustomDialog(infoTask: Task) {
        dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_settings_custom_task)

        val taskName = dialog.findViewById<EditText>(R.id.task_name)
        val taskDescription = dialog.findViewById<EditText>(R.id.task_description)
        val taskDeadLine = dialog.findViewById<TextView>(R.id.textview_change_date)
        val term = dialog.findViewById<EditText>(R.id.edittext_term_youtrack)
        var projectName: String = infoTask.nameProject
        val cancelButton = dialog.findViewById<Button>(R.id.button_cancel)
        val updateButton = dialog.findViewById<Button>(R.id.button_update)
        val projects = dialog.findViewById<Spinner>(R.id.spinner_projects)

        var yearFromCalendar = infoTask.onset?.year ?: LocalDate.now().year
        var monthFromCalendar = infoTask.onset?.month?.value ?: LocalDate.now().month.value.toInt()
        var dayFromCalendar = infoTask.onset?.dayOfMonth ?: LocalDate.now().dayOfMonth

        taskName.setText(infoTask.summary)
        taskDescription.setText(infoTask.description)
        taskDeadLine.text = convertLocalDateToString(infoTask.onset)
        term.setText(convertDurationToString(infoTask.evaluate))

        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_row, R.id.text_row, nameProjects)
        projects.adapter = arrayAdapter
        projects.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                projectName = nameProjects[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        taskDeadLine.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(yearFromCalendar, monthFromCalendar, dayFromCalendar)
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

        try {
            term.addTextChangedListener(object : TextWatcher {
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
                            term.removeTextChangedListener(this)
                            term.setText(beforeLastChar)
                            term.setSelection(beforeLastChar.length)
                            term.addTextChangedListener(this)
                        }
                    }
                }
            })
        } catch (e: java.lang.IllegalArgumentException) {
            term.text.clear()
            showToast(Constants.TOAST_TYPE_ERROR, R.string.error_in_field_term)
        }

        updateButton.setOnClickListener {
            convertStringToDuration(term.text.toString()) ?: return@setOnClickListener

            if (taskName.text.isNotEmpty() && term.text.isNotEmpty()) {
                val customTask = CustomTask(
                    idTask = infoTask.id,
                    nameProject = projectName,
                    summary = taskName.text.toString(),
                    description = taskDescription.text.toString(),
                    estimate = convertStringToDuration(term.text.toString()),
                    startDateTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    timeLeft = convertStringToDuration(term.text.toString()),
                    taskLaunchTime = null
                )
                presenter.updateCustomTask(customTask)

                dialog.dismiss()
            } else {
                showToast(TOAST_TYPE_WARNING, R.string.empty_fields)
            }

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun setCurrentTaskStatus(status: TaskStatus) {
        infoTask.taskStatus = status
    }

    private fun openPopupMenu(popupMenu: PopupMenu) {
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.change_task -> {
                    showChangeOfCustomDialog(infoTask)
                    true
                }
                R.id.delete_task -> {
                    showAlertDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.inflate(R.menu.menu_start_task)
        popupMenu.show()
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.сonfirmation))
        builder.setMessage(getString(R.string.are_you_sure_delete_task))

        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->

            presenter.removeTask(idTask)
            val intent = Intent(getIntentForeground()).putExtra("ID", infoTask.id).setAction(STOP)
            startService(intent)
            dialogInterface.dismiss()
            finish()
        }

        builder.setNegativeButton(getString(R.string.no)) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun convertLocalDateToString(date: LocalDateTime?): String {
        return if (date != null) {
            date.format(DateTimeFormatter.ofPattern("dd MMMM HH:mm", Locale.forLanguageTag("ru")))
        } else getString(R.string.no_date_specified)
    }

    private fun convertDurationToString(term: Duration): String {
        return term.toString().replace("d", "д").replace("h", "ч").replace("m", "м")
    }

    private fun convertStringToDuration(text: String): Duration? {
        val modifiedText = text.replace("д", "d").replace("ч", "h").replace("м", "m")
        var result: Duration? = null
        try {
            result = Duration.parse(modifiedText)
            log("$text $result")
        } catch (e: java.time.format.DateTimeParseException) {
            e.printStackTrace()
            showToast(TOAST_TYPE_WARNING, R.string.error_in_field_term)
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(TOAST_TYPE_WARNING, R.string.error_in_field_term)
            return null
        }
        return result
    }

    override fun getAllNameProjects(allNameProjects: List<String>?) {
        if (allNameProjects != null) {
            nameProjects.addAll(allNameProjects)
        }
    }

    private fun checkVisibleTextTimer(): Boolean {
        return binding.textCountTimeForward.isVisible
    }

    private fun getToken(): String? {
        val settings = Settings(this)
        return settings.getToken()
    }

    private fun getIntentForeground(): Intent {
        return Intent(this, ForegroundService::class.java)
    }

    override fun setVisibleButtonSettings(taskType: TaskType) {
        if (taskType == TaskType.Custom) {
            binding.layoutTopBar.weightSum = 6f
            binding.buttonSettings.visibility = View.VISIBLE
        } else {
            binding.layoutTopBar.weightSum = 0f
            binding.buttonSettings.visibility = View.GONE
        }
    }

    override fun showLayoutStartButton() {
        binding.layoutOnlyStartButton.visibility = View.VISIBLE
        binding.layoutStartAndCompleteButton.visibility = View.GONE
    }

    override fun showLayoutStartAndComplete() {
        binding.layoutOnlyStartButton.visibility = View.GONE
        binding.layoutStartAndCompleteButton.visibility = View.VISIBLE
    }

    override fun stopServices() {
        Intent(this, ForegroundService::class.java).also {
            it.action = ForegroundService.Action.STOP.toString()
            stopService(it)
        }
    }

    private fun showCompleteTaskDialog(
        timeFromLaunch: Duration, stateTask: List<StateTask>
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_finish_task)
        val termYouTrack = dialog.findViewById<EditText>(R.id.edittext_term_youtrack)
        val termCustom = dialog.findViewById<LinearLayout>(R.id.linearlayout_term_custom)

        val result = DataForResult(
            day = timeFromLaunch.inWholeDays.toInt(),
            hour = timeFromLaunch.inWholeHours.toInt(),
            minute = timeFromLaunch.inWholeMinutes.toInt(),
            idTask = idTask,
            token = getToken()
        )

        if (presenter.getTypeEnterTime()) {
            termYouTrack.visibility = View.GONE
            termCustom.visibility = View.VISIBLE

            dialog.findViewById<NumberPicker>(R.id.numberpicker_minuts).apply {
                minValue = 0
                maxValue = 59
                value = timeFromLaunch.inWholeMinutes.toInt()
                setOnValueChangedListener { _, _, newVal ->
                    result.minute = newVal
                }
            }

            dialog.findViewById<NumberPicker>(R.id.numberpicker_hours).apply {
                minValue = 0
                maxValue = 8
                value = timeFromLaunch.inWholeHours.toInt()
                setOnValueChangedListener { _, _, newVal ->
                    result.hour = newVal
                }
            }

            dialog.findViewById<NumberPicker>(R.id.numberpicker_days).apply {
                //textSize = 50F
                maxValue = 6
                minValue = 0
                value = timeFromLaunch.inWholeDays.toInt()
                setOnValueChangedListener { _, _, newVal ->
                    result.day = newVal
                }
            }

        } else {
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

        }

        val listName: List<String> = stateTask!!.map { it.localizedName }

        val spinner = dialog.findViewById<Spinner>(R.id.spinner_statetask)
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_row, R.id.text_row, listName)
        spinner.adapter = arrayAdapter
        spinner.setSelection(stateTask.indexOfFirst { it.name == infoTask.currentState })
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                result.idState = stateTask[p2].id
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val yesBtn = dialog.findViewById(R.id.button_ok) as CardView
        yesBtn.setOnClickListener {

            if (termYouTrack.text.toString().isNotEmpty()) {
                val durationTerm = convertStringToDuration(termYouTrack.text.toString())
                if (durationTerm != null) {
                    result.day = durationTerm.inWholeDays.toInt()
                    result.hour = (durationTerm.inWholeHours - (result.day * 24)).toInt()
                    result.minute =
                        (durationTerm.inWholeMinutes - (result.day * 24 * 60) - (result.hour * 60)).toInt()
                }
            } else {
                termYouTrack.text.clear()
                return@setOnClickListener
            }

            result.comment = dialog.findViewById<EditText>(R.id.comment).text.toString() ?: ""
            startService(getIntentForeground().also {
                it.action = ForegroundService.Action.STOP.toString()
            })
            if (result.day != 0 || result.hour != 0 || result.minute != 0) {
                // отправка данных в YouTrack
                presenter.postTimeSpent(result, infoTask)
                presenter.postStateTask(result)
                presenter.updateInfoTask()
                presenter.updateStatus(infoTask.id, TaskStatus.Open)
                presenter.removeTaskLaunchTime(result.idTask!!)
                startForegroundService(stopTimerService(this, idTask))
                dialog.dismiss()

            } else {
                showToast(TOAST_TYPE_WARNING, R.string.minimal_time)
            }
        }

        val noBtn = dialog.findViewById(R.id.button_cancel) as CardView
        noBtn.setOnClickListener {

            val intent =
                Intent(getIntentForeground()).putExtra("ID", infoTask.id).setAction(CONTINUE)
            startForegroundService(intent)

            presenter.updateStatus(infoTask.id, TaskStatus.Run)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showCompleteCustomTaskDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_finish_custom_task)

        val termYouTrack = dialog.findViewById<EditText>(R.id.edittext_term_youtrack)
        val termCustom = dialog.findViewById<LinearLayout>(R.id.linearlayout_term_custom)

        val addTime = dialog.findViewById<CardView>(R.id.cardview_add_time)
        val cancel = dialog.findViewById<CardView>(R.id.cardview_cancel)

        val result = DataForResult(
            day = timeFromLaunch.inWholeDays.toInt(),
            hour = timeFromLaunch.inWholeHours.toInt(),
            minute = timeFromLaunch.inWholeMinutes.toInt(),
            idTask = idTask,
            token = getToken()
        )

        if (presenter.getTypeEnterTime()) {
            termYouTrack.visibility = View.GONE
            termCustom.visibility = View.VISIBLE

            dialog.findViewById<NumberPicker>(R.id.numberpicker_minuts).apply {
                minValue = 0
                maxValue = 59
                value = timeFromLaunch.inWholeMinutes.toInt()
                setOnValueChangedListener { _, _, newVal ->
                    result.minute = newVal
                }
            }

            dialog.findViewById<NumberPicker>(R.id.numberpicker_hours).apply {
                minValue = 0
                maxValue = 8
                value = timeFromLaunch.inWholeHours.toInt()
                setOnValueChangedListener { _, _, newVal ->
                    result.hour = newVal
                }
            }

            dialog.findViewById<NumberPicker>(R.id.numberpicker_days).apply {
                maxValue = 6
                minValue = 0
                value = timeFromLaunch.inWholeDays.toInt()
                setOnValueChangedListener { _, _, newVal ->
                    result.day = newVal
                }
            }
        } else {
            termYouTrack.visibility = View.VISIBLE
            termCustom.visibility = View.GONE

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
        }

        addTime.setOnClickListener {
            if (termYouTrack.text.toString().isNotEmpty()) {
                val durationTerm = convertStringToDuration(termYouTrack.text.toString())
                if (durationTerm != null) {
                    result.day = durationTerm.inWholeDays.toInt()
                    result.hour = (durationTerm.inWholeHours - (result.day * 24)).toInt()
                    result.minute =
                        (durationTerm.inWholeMinutes - (result.day * 24 * 60) - (result.hour * 60)).toInt()
                } else {
                    termYouTrack.text.clear()
                    return@setOnClickListener
                }
            }

            presenter.updateTimeCustomTask(result, infoTask)
            presenter.removeTaskLaunchTime(result.idTask!!)
            binding.layoutOnlyStartButton.visibility = View.VISIBLE
            binding.layoutStartAndCompleteButton.visibility = View.GONE
            startForegroundService(completeTimerService(this, result.idTask.toString()))
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            val intent = Intent(getIntentForeground()).putExtra("ID", infoTask.id).setAction(CONTINUE)
            startForegroundService(intent)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun setProgressBarTimer(progressBar: CircularProgressBar, infoTask: Task) {
        if (infoTask.evaluate.inWholeSeconds == 0L) progressBar.progressMax = 600f
        else progressBar.progressMax = infoTask.evaluate.inWholeSeconds.toFloat()

        if (checkVisibleTextTimer()) {
            progressBar.progress = infoTask.usedTime.inWholeSeconds.toFloat()
        } else {
            progressBar.progress = infoTask.remainingTime.inWholeSeconds.toFloat()
        }
    }

    override fun setColorTextTimer(timeLeft: Duration) {
        binding.apply {
            textCountTimeForward.setTextColor(
                ContextCompat.getColor(
                    this@StartTaskActivity, R.color.error_color
                )
            )
            textCountTimeForward.text =
                getString(R.string.time_over, format.formatSeconds(timeLeft))
            val typeFont = Typeface.create("rubik_extrabold", Typeface.BOLD)
            textCountTimeForward.typeface = typeFont
        }
    }

    override fun visibleButton() {
        val onlyStartButton = binding.layoutOnlyStartButton
        val pauseAndCompleteButton = binding.layoutStartAndCompleteButton
        if (onlyStartButton.visibility == View.VISIBLE) {
            onlyStartButton.visibility = View.GONE
            pauseAndCompleteButton.visibility = View.VISIBLE
        } else {
            onlyStartButton.visibility = View.VISIBLE
            pauseAndCompleteButton.visibility = View.GONE
        }
    }

    override fun checkVisibleTextTimer(text: ServiceTask) {
        if (binding.textCountTimeForward.isVisible) showCurrentTime(text.timeSpent)
        else showTimeLeft(text.timeLeft!!)
    }

    override fun showCurrentTime(currentTime: Duration) {
        binding.textCountTimeForward.text = format.formatSeconds(currentTime)
        binding.circularProgressBarTimer.setProgressWithAnimation(
            currentTime.inWholeSeconds.toFloat(), 700
        )
    }

    override fun showTimeLeft(timeLeft: Duration) {
        binding.textCountTimeBack.text = format.formatSeconds(timeLeft)
        binding.circularProgressBarTimer.setProgressWithAnimation(
            timeLeft.inWholeSeconds.toFloat(), 700
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onComplete(
        timeFromLaunch: Duration, stateTask: List<StateTask>
    ) {
        binding.layoutOnlyStartButton.visibility = View.VISIBLE
        binding.layoutStartAndCompleteButton.visibility = View.GONE
        if (infoTask.taskType == TaskType.YouTrack) {
            showCompleteTaskDialog(timeFromLaunch, stateTask)
        } else {
            showCompleteCustomTaskDialog()
        }
    }

    override fun setState(state: List<StateTask>?) {
        stateTask = state
    }

    override fun closeActivity() {
        finish()
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[Calendar.MONTH] = monthOfYear
        val month = monthOfYear + 1
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
        textView.setTextColor(getColor(R.color.real_white))
        textView.text = textDeadLine
    }
}