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

package org.isoron.uhabits;

import android.appwidget.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.util.*;

import androidx.annotation.*;
import androidx.test.filters.*;
import androidx.test.platform.app.*;
import androidx.test.uiautomator.*;

import junit.framework.*;

import org.isoron.androidbase.*;
import org.isoron.androidbase.activities.*;
import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static androidx.test.platform.app.InstrumentationRegistry.*;
import static androidx.test.uiautomator.UiDevice.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@MediumTest
public class BaseAndroidTest extends TestCase
{
    // 8:00am, January 25th, 2015 (UTC)
    public static final long FIXED_LOCAL_TIME = 1422172800000L;

    protected Context testContext;

    protected Context targetContext;

    protected Preferences prefs;

    protected HabitList habitList;

    protected TaskRunner taskRunner;

    protected HabitFixtures fixtures;

    protected CountDownLatch latch;

    protected HabitsApplicationTestComponent appComponent;

    protected ModelFactory modelFactory;

    protected HabitsActivityTestComponent component;

    private boolean isDone = false;

    private UiDevice device;

    @Override
    @Before
    public void setUp()
    {
        if (Looper.myLooper() == null) Looper.prepare();
        device = getInstance(getInstrumentation());

        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testContext = InstrumentationRegistry.getInstrumentation().getContext();

        DateUtils.setFixedLocalTime(FIXED_LOCAL_TIME);
        DateUtils.setStartDayOffset(0, 0);
        setResolution(2.0f);
        setTheme(R.style.AppBaseTheme);
        setLocale("en", "US");

        latch = new CountDownLatch(1);

        Context context = targetContext.getApplicationContext();
        File dbFile = DatabaseUtils.getDatabaseFile(context);
        appComponent = DaggerHabitsApplicationTestComponent
            .builder()
            .appContextModule(new AppContextModule(context))
            .habitsModule(new HabitsModule(dbFile))
            .build();

        HabitsApplication.Companion.setComponent(appComponent);
        prefs = appComponent.getPreferences();
        habitList = appComponent.getHabitList();
        taskRunner = appComponent.getTaskRunner();
        modelFactory = appComponent.getModelFactory();

        prefs.clear();

        fixtures = new HabitFixtures(modelFactory, habitList);
        fixtures.purgeHabits(appComponent.getHabitList());
        Habit habit = fixtures.createEmptyHabit();

        component = DaggerHabitsActivityTestComponent
            .builder()
            .activityContextModule(new ActivityContextModule(targetContext))
            .habitsApplicationComponent(appComponent)
            .build();
    }

    protected void assertWidgetProviderIsInstalled(Class componentClass)
    {
        ComponentName provider =
            new ComponentName(targetContext, componentClass);
        AppWidgetManager manager = AppWidgetManager.getInstance(targetContext);

        List<ComponentName> installedProviders = new LinkedList<>();
        for (AppWidgetProviderInfo info : manager.getInstalledProviders())
            installedProviders.add(info.provider);

        assertThat(installedProviders, hasItems(provider));
    }

    protected void awaitLatch() throws InterruptedException
    {
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    protected void setLocale(@NonNull String language, @NonNull String country)
    {
        Locale locale = new Locale(language, country);
        Locale.setDefault(locale);
        Resources res = targetContext.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
    }

    protected void setResolution(float r)
    {
        DisplayMetrics dm = targetContext.getResources().getDisplayMetrics();
        dm.density = r;
        dm.scaledDensity = r;
        InterfaceUtils.setFixedResolution(r);
    }

    protected void runConcurrently(Runnable... runnableList) throws Exception
    {
        isDone = false;
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future> futures = new LinkedList<>();
        for (Runnable r : runnableList)
            futures.add(executor.submit(() ->
            {
                while (!isDone) r.run();
                return null;
            }));

        Thread.sleep(3000);
        isDone = true;
        executor.shutdown();
        for(Future f : futures) f.get();
        while (!executor.isTerminated()) Thread.sleep(50);
    }

    protected void setTheme(@StyleRes int themeId)
    {
        targetContext.setTheme(themeId);
        StyledResources.setFixedTheme(themeId);
    }

    protected void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (InterruptedException e)
        {
            fail();
        }
    }

    public long timestamp(int year, int month, int day)
    {
        GregorianCalendar cal = DateUtils.getStartOfTodayCalendar();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    protected void startTracing()
    {
        File dir = new AndroidDirFinder(targetContext).getFilesDir("Profile");
        assertNotNull(dir);
        String tracePath = dir.getAbsolutePath() + "/performance.trace";
        Log.d("PerformanceTest", String.format("Saving trace file to %s", tracePath));
        Debug.startMethodTracingSampling(tracePath, 0, 1000);
    }

    protected void stopTracing()
    {
        Debug.stopMethodTracing();
    }

    protected Timestamp day(int offset)
    {
        return DateUtils.getToday().minus(offset);
    }


    public void setSystemTime(String tz,
                              int year,
                              int javaMonth,
                              int day,
                              int hourOfDay,
                              int minute) throws Exception
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.SECOND, 0);
        cal.set(year, javaMonth, day, hourOfDay, minute);
        cal.setTimeZone(TimeZone.getTimeZone(tz));
        setSystemTime(cal);
    }

    private void setSystemTime(GregorianCalendar cal) throws Exception
    {
        ZoneId tz = cal.getTimeZone().toZoneId();

        // Set time zone (temporary)
        String command = String.format("service call alarm 3 s16 %s", tz);
        device.executeShellCommand(command);

        // Set time zone (permanent)
        command = String.format("setprop persist.sys.timezone %s", tz);
        device.executeShellCommand(command);

        // Set time
        String date = String.format("%02d%02d%02d%02d%02d.%02d",
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.SECOND));

        // Set time (method 1)
        // Run twice to override daylight saving time
        device.executeShellCommand("date " + date);
        device.executeShellCommand("date " + date);

        // Set time (method 2)
        // Run in addition to the method above because one of these mail fail, depending
        // on the Android API version.
        command = String.format("date -u @%d", cal.getTimeInMillis() / 1000);
        device.executeShellCommand(command);

        // Wait for system events to settle
        Thread.sleep(1000);
    }

    private GregorianCalendar savedCalendar = null;

    public void saveSystemTime()
    {
        savedCalendar = new GregorianCalendar();
    }

    public void restoreSystemTime() throws Exception
    {
        if (savedCalendar == null) throw new NullPointerException();
        setSystemTime(savedCalendar);
    }
}
