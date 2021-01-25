package org.isoron.uhabits.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataDeleteRequest
import com.google.android.gms.fitness.request.SessionInsertRequest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Timestamp
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import org.isoron.uhabits.core.models.Checkmark.*

class GoogleFitUtils(val context: Context) {

    private val fitRequestCode = 3792
    private val fitRequestCode2 = 3793
    private val tag = "Google Fit Utils"

    private val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_HYDRATION, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
            .build()

    private val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun processHabit(habit: Habit, value: Int, timestamp: Timestamp) {
        if (!habit.enableGoogleFit) return

        val duration = habit.activityDuration.toLong()

        if (value == YES_MANUAL) {
            val hydration = habit.hydration.toFloat()
            val calorie = habit.calorieBurned.toFloat()
            insertSession(habit.name, hydration, calorie, duration, timestamp)
        } else if (value == NO)
            deleteSession(duration, timestamp)
    }

    fun processNumericHabit(habit: Habit, value: Double, timestamp: Timestamp) {
        if (!habit.enableGoogleFit) return

        val duration = habit.activityDuration.toLong()

        if (value > 0) {
            val hydration = habit.hydration.toFloat()
            val calorie = habit.calorieBurned.toFloat()
            insertSession(habit.name, hydration, calorie, duration, timestamp)
        } else if (value == 0.0)
            deleteSession(duration, timestamp)
    }

    // duration: seconds | calorie: kcal | volume: litre
    private fun insertSession(name: String, volume: Float? = null, calorie: Float? = null,
                              duration: Long, timestamp: Timestamp) {

        val end = timestamp.toJavaDate().toInstant().atZone(ZoneId.systemDefault())
        val start = end.minusSeconds(duration)

        val session = Session.Builder()
                .setName(name)
                .setDescription("loop habits tracker")
                .setIdentifier(UUID.randomUUID().toString())
                .setActivity(FitnessActivities.RUNNING)
                .setStartTime(start.toEpochSecond(), TimeUnit.SECONDS)
                .setEndTime(end.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        val insertRequest = SessionInsertRequest.Builder()
                .setSession(session)

        if (calorie != null) {
            val calorieSource = DataSource.Builder()
                    .setAppPackageName(context)
                    .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                    .setType(DataSource.TYPE_RAW)
                    .build()

            val caloriePoint = DataPoint.builder(calorieSource)
                    .setField(Field.FIELD_CALORIES, calorie)
                    .setTimeInterval(start.toEpochSecond(), end.toEpochSecond(), TimeUnit.SECONDS)
                    .build()

            val calorieSet = DataSet.builder(calorieSource)
                    .add(caloriePoint)
                    .build()

            insertRequest.addDataSet(calorieSet)
        }

        if (volume != null) {
            val hydrationSource = DataSource.Builder()
                    .setAppPackageName(context)
                    .setDataType(DataType.TYPE_HYDRATION)
                    .setType(DataSource.TYPE_RAW)
                    .build()

            val hydrationPoint = DataPoint.builder(hydrationSource)
                    .setField(Field.FIELD_VOLUME, volume)
                    .setTimestamp(end.toEpochSecond(), TimeUnit.SECONDS)
                    .build()

            val hydrationSet = DataSet.builder(hydrationSource)
                    .add(hydrationPoint)
                    .build()

            insertRequest.addDataSet(hydrationSet)
        }

        Fitness.getSessionsClient(context, account)
                .insertSession(insertRequest.build())
                .addOnSuccessListener { Log.i(tag, "session inserted") }
                .addOnFailureListener { error ->
                    Log.e(tag, "session insert error $error")
                }
    }

    fun deleteSession(duration: Long, timestamp: Timestamp) {
        val end = timestamp.toJavaDate().toInstant().atZone(ZoneId.systemDefault())
        val start = end.minusSeconds(duration)

        val deleteRequest = DataDeleteRequest.Builder()
                .setTimeInterval(start.toEpochSecond(), end.toEpochSecond(), TimeUnit.SECONDS)
                .deleteAllData()
                .deleteAllSessions()
                .build()

        Fitness.getHistoryClient(context, account)
                .deleteData(deleteRequest)
                .addOnSuccessListener { Log.i(tag, "session deleted") }
                .addOnFailureListener { error ->
                    Log.e(tag, "delete session failed $error")
                }
    }

    fun getPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                        fitRequestCode2)
            }
        }
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,
                    fitRequestCode,
                    account,
                    fitnessOptions)
            Log.i(tag, "no permissions")
        } else {
            Log.i(tag, "permissions granted")
        }
    }

    fun onPermissionResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == fitRequestCode && resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, "Permission not granted", Toast.LENGTH_LONG).show()
        }
        if (requestCode == fitRequestCode2 && resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, "Permission not granted", Toast.LENGTH_LONG).show()
        }
    }
}