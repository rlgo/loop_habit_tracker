package org.isoron.uhabits.core.commands;

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;

public class UnfavouriteHabitsCommand implements Command {
    @NonNull
    final HabitList habitList;

    @NonNull
    final List<Habit> selected;

    public UnfavouriteHabitsCommand(@NonNull HabitList habitList,
                                  @NonNull List<Habit> selected)
    {
        this.selected = new LinkedList<>(selected);
        this.habitList = habitList;
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setFavourite(false);
        habitList.update(selected);
    }
}
