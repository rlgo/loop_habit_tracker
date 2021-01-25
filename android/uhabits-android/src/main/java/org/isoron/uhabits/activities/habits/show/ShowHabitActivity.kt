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
package org.isoron.uhabits.activities.habits.show

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.isoron.androidbase.AndroidDirFinder
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.HabitsDirFinder
import org.isoron.uhabits.activities.common.dialogs.ConfirmDeleteDialogFactory
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitBehavior
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitMenuBehavior
import org.isoron.uhabits.intents.IntentFactory


class ShowHabitActivity : AppCompatActivity(), CommandRunner.Listener {

    private lateinit var commandRunner: CommandRunner
    private lateinit var menu: ShowHabitMenu
    private lateinit var presenter: ShowHabitPresenter
    private lateinit var view: ShowHabitView

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (applicationContext as HabitsApplication).component
        val habitList = appComponent.habitList
        val habit = habitList.getById(ContentUris.parseId(intent.data!!))!!

        val preferences = appComponent.preferences
        commandRunner = appComponent.commandRunner
        AndroidThemeSwitcher(this, preferences).apply()

        view = ShowHabitView(this,habit)
        presenter = ShowHabitPresenter(
                context = this,
                habit = habit,
                preferences = appComponent.preferences,
        )

        val screen = ShowHabitScreen(
                activity = this,
                confirmDeleteDialogFactory = ConfirmDeleteDialogFactory { this },
                habit = habit,
                intentFactory = IntentFactory(),
                numberPickerFactory = NumberPickerFactory(this),
                widgetUpdater = appComponent.widgetUpdater,
        )

        val behavior = ShowHabitBehavior(
                commandRunner = commandRunner,
                habit = habit,
                habitList = habitList,
                preferences = preferences,
                screen = screen,
        )

        val menuBehavior = ShowHabitMenuBehavior(
                commandRunner = commandRunner,
                habit = habit,
                habitList = habitList,
                screen = screen,
                system = HabitsDirFinder(AndroidDirFinder(this)),
                taskRunner = appComponent.taskRunner,
        )

        menu = ShowHabitMenu(
                activity = this,
                behavior = menuBehavior,
                preferences = preferences,
        )

        view.onScoreCardSpinnerPosition = behavior::onScoreCardSpinnerPosition
        view.onBarCardBoolSpinnerPosition = behavior::onBarCardBoolSpinnerPosition
        view.onBarCardNumericalSpinnerPosition = behavior::onBarCardNumericalSpinnerPosition
        view.onCalorieBoolSpinnerPosition = behavior::onCalorieBarCardBoolSpinnerPosition
        view.onCalorieNumericalSpinnerPosition = behavior::onCalorieBarCardNumericalSpinnerPosition
        view.onHydrationBoolSpinnerPosition = behavior::onHydrationBarCardBoolSpinnerPosition
        view.onHydrationNumericalSpinnerPosition = behavior::onHydrationBarCardNumericalSpinnerPosition
        view.onActivitydurationBoolSpinnerPosition = behavior::onActivitydurationBarCardBoolSpinnerPosition
        view.onActivitydurationNumericalSpinnerPosition = behavior::onActivitydurationBarCardNumericalSpinnerPosition
        view.onClickEditHistoryButton = behavior::onClickEditHistory

        setContentView(view)

        val googleFitBtn = findViewById(R.id.googleFitBtn) as Button
        googleFitBtn.setOnClickListener {
            // your code to perform when the user clicks on the button
            Toast.makeText(this, "Redirecting to Google Fit.", Toast.LENGTH_SHORT).show()
            var intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.fitness")
            if (intent != null) {
                // We found the activity now start the activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                // Bring user to the market or let them choose an app?
                intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.parse("market://details?id=" + "com.google.android.apps.fitness")
                startActivity(intent)
            }
        }

    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        return menu.onCreateOptionsMenu(m)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menu.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        commandRunner.addListener(this)
        refresh()
    }

    override fun onPause() {
        commandRunner.removeListener(this)
        super.onPause()
    }

    override fun onCommandExecuted(command: Command?, refreshKey: Long?) {
        refresh()
    }

    fun refresh() {
            val appComponentP = (applicationContext as HabitsApplication).component
    val habitListP = appComponentP.habitList
    val Passedhabit = habitListP.getById(ContentUris.parseId(intent.data!!))!!
        scope.launch {
            view.update(presenter.present(),Passedhabit)
        }
    }
}

