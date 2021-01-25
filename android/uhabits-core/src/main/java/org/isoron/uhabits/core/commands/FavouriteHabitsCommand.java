package org.isoron.uhabits.core.commands;

import androidx.annotation.*;

import org.isoron.uhabits.core.models.*;

import java.util.*;


public class FavouriteHabitsCommand implements Command {
    final List<Habit> selected;

    final HabitList habitList;

    public FavouriteHabitsCommand(@NonNull HabitList habitList,
                                  @NonNull List<Habit> selected)
    {
        super();
        this.habitList = habitList;
        this.selected = new LinkedList<>(selected);
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) h.setFavourite(true);
        habitList.update(selected);
    }
}
