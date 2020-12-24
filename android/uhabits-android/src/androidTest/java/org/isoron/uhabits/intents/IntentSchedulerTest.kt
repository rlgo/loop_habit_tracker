/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.uhabits.intents

import android.content.ContentUris.*
import androidx.test.ext.junit.runners.*
import androidx.test.filters.*
import org.hamcrest.Matchers.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.reminders.ReminderScheduler.SchedulerResult.*
import org.isoron.uhabits.receivers.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.*
import java.util.*
import java.util.Calendar.*

class IntentSchedulerTest : BaseAndroidTest() {

    @Before
    override fun setUp() {
        super.setUp()
        saveSystemTime()
    }

    @After
    override fun tearDown() {
        restoreSystemTime()
        super.tearDown()
    }

    @Test
    @MediumTest
    @Throws(Exception::class)
    fun testSetSystemTime() {
        setSystemTime("America/Chicago", 2020, JUNE, 1, 12, 40)
        var cal = GregorianCalendar()
        assertThat(cal.timeZone, equalTo(TimeZone.getTimeZone("America/Chicago")))
        assertThat(cal[YEAR], equalTo(2020))
        assertThat(cal[MONTH], equalTo(JUNE))
        assertThat(cal[DAY_OF_MONTH], equalTo(1))
        assertThat(cal[HOUR_OF_DAY], equalTo(12))
        assertThat(cal[MINUTE], equalTo(40))

        setSystemTime("Europe/Paris", 2019, MAY, 15, 6, 30)
        cal = GregorianCalendar()
        assertThat(cal.timeZone, equalTo(TimeZone.getTimeZone("Europe/Paris")))
        assertThat(cal[YEAR], equalTo(2019))
        assertThat(cal[MONTH], equalTo(MAY))
        assertThat(cal[DAY_OF_MONTH], equalTo(15))
        assertThat(cal[HOUR_OF_DAY], equalTo(6))
        assertThat(cal[MINUTE], equalTo(30))

        setSystemTime("Asia/Tokyo", 2021, DECEMBER, 20, 18, 0)
        cal = GregorianCalendar()
        assertThat(cal.timeZone, equalTo(TimeZone.getTimeZone("Asia/Tokyo")))
        assertThat(cal[YEAR], equalTo(2021))
        assertThat(cal[MONTH], equalTo(DECEMBER))
        assertThat(cal[DAY_OF_MONTH], equalTo(20))
        assertThat(cal[HOUR_OF_DAY], equalTo(18))
        assertThat(cal[MINUTE], equalTo(0))
    }

    @Test
    @MediumTest
    fun testScheduleShowReminder() {
        for (h in habitList) h.setReminder(null)
        ReminderReceiver.clearLastReceivedIntent()

        setSystemTime("America/Chicago", 2020, JUNE, 1, 12, 30)
        val reminderTime = 1591155900000 // 2020-06-02 22:45:00 (America/Chicago)

        val habit = habitList.getByPosition(0)
        val scheduler = appComponent.intentScheduler
        assertThat(scheduler.scheduleShowReminder(reminderTime, habit, 0), equalTo(OK))

        setSystemTime("America/Chicago", 2020, JUNE, 2, 22, 44)
        assertNull(ReminderReceiver.getLastReceivedIntent())

        setSystemTime("America/Chicago", 2020, JUNE, 2, 22, 46)
        val intent = ReminderReceiver.getLastReceivedIntent()
        assertNotNull(intent)
        assertThat(parseId(intent.data!!), equalTo(habit.id))
    }

    @Test
    @MediumTest
    fun testScheduleWidgetUpdate() {
        WidgetReceiver.clearLastReceivedIntent()

        setSystemTime("America/Chicago", 2020, JUNE, 1, 12, 30)
        val updateTime = 1591155900000 // 2020-06-02 22:45:00 (America/Chicago)

        val scheduler = appComponent.intentScheduler
        assertThat(scheduler.scheduleWidgetUpdate(updateTime), equalTo(OK))

        setSystemTime("America/Chicago", 2020, JUNE, 2, 22, 44)
        assertNull(WidgetReceiver.getLastReceivedIntent())

        setSystemTime("America/Chicago", 2020, JUNE, 2, 22, 46)
        val intent = WidgetReceiver.getLastReceivedIntent()
        assertNotNull(intent)
    }
}