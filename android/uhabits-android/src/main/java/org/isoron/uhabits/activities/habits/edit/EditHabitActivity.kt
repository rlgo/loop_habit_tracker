/*
 * Copyright (C) 2016 linson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.activities.habits.edit

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.android.datetimepicker.time.RadialPickerLayout
import com.android.datetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_edit_habit.*
import org.isoron.androidbase.utils.ColorUtils
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.common.dialogs.ColorPickerDialogFactory
import org.isoron.uhabits.activities.common.dialogs.FrequencyPickerDialog
import org.isoron.uhabits.activities.common.dialogs.WeekdayPickerDialog
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.ActivityEditHabitBinding
import org.isoron.uhabits.utils.formatTime
import org.isoron.uhabits.utils.toFormattedString
import org.isoron.uhabits.utils.toThemedAndroidColor


class EditHabitActivity : AppCompatActivity() {

    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var binding: ActivityEditHabitBinding
    private lateinit var commandRunner: CommandRunner

    var habitId = -1L
    var habitType = -1
    var unit = ""
    var color = PaletteColor(11)
    var androidColor = 0
    var freqNum = 1
    var freqDen = 1
    var reminderHour = -1
    var reminderMin = -1
    var reminderDays: WeekdayList = WeekdayList.EVERY_DAY
    var enableGoogleFit: Boolean = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val component = (application as HabitsApplication).component
        themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        binding = ActivityEditHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("habitId")) {
            binding.toolbar.title = getString(R.string.edit_habit)
            habitId = intent.getLongExtra("habitId", -1)
            val habit = component.habitList.getById(habitId)!!
            habitType = habit.type
            color = habit.color
            freqNum = habit.frequency.numerator
            freqDen = habit.frequency.denominator
            if (habit.hasReminder()) {
                reminderHour = habit.reminder.hour
                reminderMin = habit.reminder.minute
                reminderDays = habit.reminder.days
            }
            binding.nameInput.setText(habit.name)
            binding.questionInput.setText(habit.question)
            binding.notesInput.setText(habit.description)
            binding.unitInput.setText(habit.unit)
            binding.targetInput.setText(habit.targetValue.toString())
            enableGoogleFit = habit.enableGoogleFit
            binding.fitSwitchInput.isChecked = habit.enableGoogleFit
            if (habit.enableGoogleFit) {
                binding.flCalorieBurned.visibility = View.VISIBLE
                binding.flHydration.visibility = View.VISIBLE
                binding.flActivityDuration.visibility = View.VISIBLE

                binding.calorieBurnedInput.setText(
                        when (habit.calorieBurned.toString() == "0.0") {
                            true -> ""
                            false -> habit.calorieBurned.toString()
                        })
                binding.hydrationInput.setText(
                        when (habit.hydration.toString() == "0.0") {
                            true -> ""
                            false -> habit.hydration.toString()
                        })
                binding.activityDurationInput.setText(
                        when (habit.activityDuration.toString() == "0.0") {
                            true -> ""
                            false -> habit.activityDuration.toString()
                        }
                )
            }
        } else {
            habitType = intent.getIntExtra("habitType", Habit.YES_NO_HABIT)
        }

        if (state != null) {
            habitId = state.getLong("habitId")
            habitType = state.getInt("habitType")
            color = PaletteColor(state.getInt("paletteColor"))
            freqNum = state.getInt("freqNum")
            freqDen = state.getInt("freqDen")
            reminderHour = state.getInt("reminderHour")
            reminderMin = state.getInt("reminderMin")
            reminderDays = WeekdayList(state.getInt("reminderDays"))
        }

        updateColors()

        if (habitType == Habit.YES_NO_HABIT) {
            binding.unitOuterBox.visibility = View.GONE
            binding.targetOuterBox.visibility = View.GONE
        } else {
            binding.nameInput.hint = getString(R.string.measurable_short_example)
            binding.questionInput.hint = getString(R.string.measurable_question_example)
            binding.frequencyOuterBox.visibility = View.GONE
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.elevation = 10.0f

        val colorPickerDialogFactory = ColorPickerDialogFactory(this)
        binding.colorButton.setOnClickListener {
            val dialog = colorPickerDialogFactory.create(color)
            dialog.setListener { paletteColor ->
                this.color = paletteColor
                updateColors()
            }
            dialog.show(supportFragmentManager, "colorPicker")
        }

        populateFrequency()
        binding.booleanFrequencyPicker.setOnClickListener {
            val dialog = FrequencyPickerDialog(freqNum, freqDen)
            dialog.onFrequencyPicked = { num, den ->
                freqNum = num
                freqDen = den
                populateFrequency()
            }
            dialog.show(supportFragmentManager, "frequencyPicker")
        }

        binding.numericalFrequencyPicker.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item)
            arrayAdapter.add(getString(R.string.every_day))
            arrayAdapter.add(getString(R.string.every_week))
            arrayAdapter.add(getString(R.string.every_month))
            builder.setAdapter(arrayAdapter) { dialog, which ->
                freqDen = when (which) {
                    1 -> 7
                    2 -> 30
                    else -> 1
                }
                populateFrequency()
                dialog.dismiss()
            }
            builder.show()
        }

        populateReminder()
        binding.reminderTimePicker.setOnClickListener {
            val currentHour = if (reminderHour >= 0) reminderHour else 8
            val currentMin = if (reminderMin >= 0) reminderMin else 0
            val is24HourMode = DateFormat.is24HourFormat(this)
            val dialog = TimePickerDialog.newInstance(object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
                    reminderHour = hourOfDay
                    reminderMin = minute
                    populateReminder()
                }

                override fun onTimeCleared(view: RadialPickerLayout?) {
                    reminderHour = -1
                    reminderMin = -1
                    reminderDays = WeekdayList.EVERY_DAY
                    populateReminder()
                }
            }, currentHour, currentMin, is24HourMode, androidColor)
            dialog.show(supportFragmentManager, "timePicker")
        }

        binding.reminderDatePicker.setOnClickListener {
            val dialog = WeekdayPickerDialog()
            dialog.setListener { days ->
                reminderDays = days
                if (reminderDays.isEmpty) reminderDays = WeekdayList.EVERY_DAY
                populateReminder()
            }
            dialog.setSelectedDays(reminderDays)
            dialog.show(supportFragmentManager, "dayPicker")
        }

        binding.fitSwitchInput.setOnCheckedChangeListener { _, isChecked ->
            run {
                enableGoogleFit = isChecked
                if (isChecked) {
                    fl_calorie_burned.visibility = View.VISIBLE
                    fl_hydration.visibility = View.VISIBLE
                    fl_activity_duration.visibility = View.VISIBLE
                } else {
                    fl_calorie_burned.visibility = View.GONE
                    fl_hydration.visibility = View.GONE
                    fl_activity_duration.visibility = View.GONE
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            if (validate()) save()
        }

        for (fragment in supportFragmentManager.fragments) {
            (fragment as DialogFragment).dismiss()
        }
    }

    private fun save() {
        val component = (application as HabitsApplication).component
        val habit = component.modelFactory.buildHabit()

        var original: Habit? = null
        if (habitId >= 0) {
            original = component.habitList.getById(habitId)!!
            habit.copyFrom(original)
        }

        habit.name = nameInput.text.trim().toString()
        habit.question = questionInput.text.trim().toString()
        habit.description = notesInput.text.trim().toString()
        habit.color = color
        if (reminderHour >= 0) {
            habit.setReminder(Reminder(reminderHour, reminderMin, reminderDays))
        } else {
            habit.setReminder(null)
        }

        habit.frequency = Frequency(freqNum, freqDen)
        if (habitType == Habit.NUMBER_HABIT) {
            habit.targetValue = targetInput.text.toString().toDouble()
            habit.targetType = Habit.AT_LEAST
            habit.unit = unitInput.text.trim().toString()
        }
        habit.type = habitType
        habit.enableGoogleFit = enableGoogleFit

        if (enableGoogleFit) {
            habit.calorieBurned = calorieBurnedInput.text.toString().toDoubleOrNull()
                    ?: 0.0
            habit.hydration = hydrationInput.text.toString().toDoubleOrNull()
                    ?: 0.0
            habit.activityDuration = activityDurationInput.text.toString().toDoubleOrNull() ?: 0.0
        } else {
            habit.calorieBurned = 0.0
            habit.hydration = 0.0
            habit.activityDuration = 0.0
        }

        val command = if (habitId >= 0) {
            component.editHabitCommandFactory.create(component.habitList, original, habit)
        } else {
            component.createHabitCommandFactory.create(component.habitList, habit)
        }
        component.commandRunner.execute(command, null)
        finish()
    }

    private fun validate(): Boolean {
        var isValid = true
        if (nameInput.text.isEmpty()) {
            nameInput.error = getString(R.string.validation_cannot_be_blank)
            isValid = false
        }
        if (habitType == Habit.NUMBER_HABIT) {
            if (unitInput.text.isEmpty()) {
                unitInput.error = getString(R.string.validation_cannot_be_blank)
                isValid = false
            }
            if (targetInput.text.isEmpty()) {
                targetInput.error = getString(R.string.validation_cannot_be_blank)
                isValid = false
            }
        }
        return isValid
    }

    private fun populateReminder() {
        if (reminderHour < 0) {
            binding.reminderTimePicker.text = getString(R.string.reminder_off)
            binding.reminderDatePicker.visibility = View.GONE
            binding.reminderDivider.visibility = View.GONE
        } else {
            val time = formatTime(this, reminderHour, reminderMin)
            binding.reminderTimePicker.text = time
            binding.reminderDatePicker.visibility = View.VISIBLE
            binding.reminderDivider.visibility = View.VISIBLE
            binding.reminderDatePicker.text = reminderDays.toFormattedString(this)
        }
    }

    private fun populateFrequency() {
        binding.booleanFrequencyPicker.text = when {
            freqNum == 1 && freqDen == 1 -> getString(R.string.every_day)
            freqNum == 1 && freqDen == 7 -> getString(R.string.every_week)
            freqNum == 1 && freqDen > 1 -> getString(R.string.every_x_days, freqDen)
            freqDen == 7 -> getString(R.string.x_times_per_week, freqNum)
            freqDen == 31 -> getString(R.string.x_times_per_month, freqNum)
            else -> "Unknown"
        }
        binding.numericalFrequencyPicker.text = when (freqDen) {
            1 -> getString(R.string.every_day)
            7 -> getString(R.string.every_week)
            30 -> getString(R.string.every_month)
            else -> "Unknown"
        }
    }

    private fun updateColors() {
        androidColor = color.toThemedAndroidColor(this)
        binding.colorButton.backgroundTintList = ColorStateList.valueOf(androidColor)
        if (!themeSwitcher.isNightMode) {
            val darkerAndroidColor = ColorUtils.mixColors(Color.BLACK, androidColor, 0.15f)
            window.statusBarColor = darkerAndroidColor
            binding.toolbar.setBackgroundColor(androidColor)
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        with(state) {
            putLong("habitId", habitId)
            putInt("habitType", habitType)
            putInt("paletteColor", color.paletteIndex)
            putInt("androidColor", androidColor)
            putInt("freqNum", freqNum)
            putInt("freqDen", freqDen)
            putInt("reminderHour", reminderHour)
            putInt("reminderMin", reminderMin)
            putInt("reminderDays", reminderDays.toInteger())
        }
    }
}
