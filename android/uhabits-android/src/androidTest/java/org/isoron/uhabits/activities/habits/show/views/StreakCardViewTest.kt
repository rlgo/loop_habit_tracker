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
package org.isoron.uhabits.activities.habits.show.views

import android.view.*
import androidx.test.ext.junit.runners.*
import org.junit.runner.RunWith
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.activities.habits.show.views.StreakCardView
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.show.views.StreakCardViewTest
import org.isoron.uhabits.core.models.*
import org.junit.*
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
@MediumTest
class StreakCardViewTest : BaseViewTest() {
    val PATH = "habits/show/StreakCard/"
    private lateinit var view: StreakCardView

    @Before
    override fun setUp() {
        super.setUp()
        val habit = fixtures.createLongHabit()
        view = LayoutInflater
                .from(targetContext)
                .inflate(R.layout.show_habit, null)
                .findViewById<View>(R.id.streakCard) as StreakCardView
        view.update(StreakCardViewModel(
                bestStreaks = habit.streaks.getBest(10),
                color = habit.color,
        ))
        measureView(view, 800f, 600f)
    }

    @Test
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }
}