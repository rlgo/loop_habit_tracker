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

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

data class HistoryCardViewModel(
        val checkmarks: IntArray,
        val color: PaletteColor,
        val firstWeekday: Int,
        val isNumerical: Boolean,
        val isSkipEnabled: Boolean,
        val target: Double,
)

class HistoryCard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitHistoryBinding.inflate(LayoutInflater.from(context), this)

    var onClickEditButton: () -> Unit = {}

    init {
        binding.edit.setOnClickListener { onClickEditButton() }
    }

    fun update(data: HistoryCardViewModel) {
        binding.historyChart.setFirstWeekday(data.firstWeekday)
        binding.historyChart.setSkipEnabled(data.isSkipEnabled)
        binding.historyChart.setCheckmarks(data.checkmarks)
        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
        binding.historyChart.setColor(androidColor)
        if (data.isNumerical) {
            binding.historyChart.setNumerical(true)
            binding.historyChart.setTarget(data.target)
        }

    }
}

class HistoryCardPresenter(
        val habit: Habit,
        val firstWeekday: Int,
        val isSkipEnabled: Boolean,
) {
    fun present() = HistoryCardViewModel(
            checkmarks = habit.checkmarks.allValues,
            color = habit.color,
            firstWeekday = firstWeekday,
            isNumerical = habit.isNumerical,
            isSkipEnabled = isSkipEnabled,
            target = habit.targetValue / habit.frequency.denominator,
    )
}