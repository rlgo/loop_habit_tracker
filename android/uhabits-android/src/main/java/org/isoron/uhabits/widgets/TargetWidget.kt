/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.widgets

import android.content.*
import android.view.*
import android.view.ViewGroup.*
import android.view.ViewGroup.LayoutParams.*
import kotlinx.coroutines.*
import org.isoron.uhabits.activities.common.views.*
import org.isoron.uhabits.activities.habits.show.views.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.views.*

class TargetWidget(
        context: Context,
        id: Int,
        private val habit: Habit
) : BaseWidget(context, id) {

    override fun getOnClickPendingIntent(context: Context) =
            pendingIntentFactory.showHabit(habit)

    override fun refreshData(view: View) = runBlocking {
            val widgetView = view as GraphWidgetView
            widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
            if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
            val chart = (widgetView.dataView as TargetChart)
            val presenter = TargetCardPresenter(habit, prefs.firstWeekday, context.resources)
            val data = presenter.present()
            chart.setColor(data.color.toThemedAndroidColor(context))
            chart.setTargets(data.targets)
            chart.setLabels(data.labels)
            chart.setValues(data.values)
    }

    override fun buildView(): View {
        return GraphWidgetView(context, TargetChart(context)).apply {
            setTitle(habit.name)
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    override fun getDefaultHeight() = 200
    override fun getDefaultWidth() = 200
}
