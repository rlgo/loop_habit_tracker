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

package org.isoron.uhabits.core.commands;

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

public class UnarchiveHabitsCommand implements Command
{
    @NonNull
    final HabitList habitList;

    @NonNull
    final List<Habit> selected;

    public UnarchiveHabitsCommand(@NonNull HabitList habitList,
                                  @NonNull List<Habit> selected)
    {
        this.selected = new LinkedList<>(selected);
        this.habitList = habitList;
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setArchived(false);
        habitList.update(selected);
    }
}