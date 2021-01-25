/*
 * Copyright (C) 2017 linson Santos Xavier <isoron@gmail.com>
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
 *
 *
 */

package org.isoron.uhabits.core.models.sqlite.records;

import org.isoron.uhabits.core.database.Column;
import org.isoron.uhabits.core.database.Table;
import org.isoron.uhabits.core.models.Frequency;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.PaletteColor;
import org.isoron.uhabits.core.models.Reminder;
import org.isoron.uhabits.core.models.WeekdayList;

/**
 * The SQLite database record corresponding to a {@link Habit}.
 */
@Table(name = "habits")
public class HabitRecord {
    @Column
    public String description;

    @Column
    public String question;

    @Column
    public String name;

    @Column(name = "freq_num")
    public Integer freqNum;

    @Column(name = "freq_den")
    public Integer freqDen;

    @Column
    public Integer color;

    @Column
    public Integer position;

    @Column
    public Integer favourite;

    @Column(name = "reminder_hour")
    public Integer reminderHour;

    @Column(name = "reminder_min")
    public Integer reminderMin;

    @Column(name = "reminder_days")
    public Integer reminderDays;

    @Column
    public Integer highlight;

    @Column
    public Integer archived;

    @Column
    public Integer type;

    @Column(name = "target_value")
    public Double targetValue;

    @Column(name = "target_type")
    public Integer targetType;

    @Column
    public String unit;

    @Column
    public Long id;

    @Column
    public String uuid;

    @Column(name = "enable_google_fit")
    public Integer enableGoogleFit;

    @Column(name = "calorie_burned")
    public Double calorieBurned;

    @Column
    public Double hydration;

    @Column(name = "activity_duration")
    public Double activityDuration;

    public void copyFrom(Habit model) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.highlight = 0;
        this.color = model.getColor().getPaletteIndex();
        this.archived = model.isArchived() ? 1 : 0;
        this.type = model.getType();
        this.targetType = model.getTargetType();
        this.targetValue = model.getTargetValue();
        this.unit = model.getUnit();
        this.position = model.getPosition();
        this.favourite = model.isFavourite() ? 1 : 0;
        this.question = model.getQuestion();
        this.uuid = model.getUUID();

        this.enableGoogleFit = model.getEnableGoogleFit() ? 1 : 0;
        this.calorieBurned = model.getCalorieBurned();
        this.hydration = model.getHydration();
        this.activityDuration = model.getActivityDuration();

        Frequency freq = model.getFrequency();
        this.freqNum = freq.getNumerator();
        this.freqDen = freq.getDenominator();
        this.reminderDays = 0;
        this.reminderMin = null;
        this.reminderHour = null;

        if (model.hasReminder()) {
            Reminder reminder = model.getReminder();
            this.reminderHour = reminder.getHour();
            this.reminderMin = reminder.getMinute();
            this.reminderDays = reminder.getDays().toInteger();
        }
    }

    public void copyTo(Habit habit) {
        habit.setId(this.id);
        habit.setName(this.name);
        habit.setDescription(this.description);
        habit.setQuestion(this.question);
        habit.setFrequency(new Frequency(this.freqNum, this.freqDen));
        habit.setColor(new PaletteColor(this.color));
        habit.setArchived(this.archived != 0);
        habit.setType(this.type);
        habit.setTargetType(this.targetType);
        habit.setTargetValue(this.targetValue);
        habit.setUnit(this.unit);
        habit.setPosition(this.position);
        habit.setFavourite(this.favourite != 0);
        habit.setUUID(this.uuid);
        habit.setEnableGoogleFit(this.enableGoogleFit == 1);
        habit.setCalorieBurned(this.calorieBurned);
        habit.setHydration(this.hydration);
        habit.setActivityDuration(this.activityDuration);

        if (reminderHour != null && reminderMin != null) {
            habit.setReminder(new Reminder(reminderHour, reminderMin,
                    new WeekdayList(reminderDays)));
        }
    }
}
