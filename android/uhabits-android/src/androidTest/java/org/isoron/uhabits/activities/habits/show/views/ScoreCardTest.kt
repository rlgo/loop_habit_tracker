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
import androidx.test.filters.*
import org.isoron.uhabits.*
import org.junit.*
import org.junit.runner.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class ScoreCardTest : BaseViewTest() {
    val PATH = "habits/show/ScoreCard/"
    private lateinit var view: ScoreCard

    @Before
    override fun setUp() {
        super.setUp()
        val habit = fixtures.createLongHabit()
        view = LayoutInflater
                .from(targetContext)
                .inflate(R.layout.show_habit, null)
                .findViewById<View>(R.id.scoreCard) as ScoreCard
        view.update(ScoreCardPresenter(habit, 0).present(0))
        measureView(view, 800f, 600f)
    }

    @Test
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }
}