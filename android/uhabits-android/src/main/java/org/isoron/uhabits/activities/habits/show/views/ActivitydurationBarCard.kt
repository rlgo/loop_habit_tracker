package org.isoron.uhabits.activities.habits.show.views

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.activities.habits.show.views.ScoreCardPresenter.Companion.getTruncateField
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

data class ActivitydurationBarCardViewModel(
        val checkmarks: List<Checkmark>,
        val bucketSize: Int,
        val color: PaletteColor,
        val isNumerical: Boolean,
        val target: Double,
        val activitydurationNumericalSpinnerPosition: Int,
        val activitydurationBoolSpinnerPosition: Int,
)

class ActivitydurationBarCard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitActivitydurationbarBinding.inflate(LayoutInflater.from(context), this)
    var onActivitydurationNumericalSpinnerPosition: (position: Int) -> Unit = {}
    var onActivitydurationBoolSpinnerPosition: (position: Int) -> Unit = {}


    fun update(data: ActivitydurationBarCardViewModel) {

        binding.barChart.setCheckmarks(data.checkmarks)
        binding.barChart.setBucketSize(data.bucketSize)

        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
        binding.barChart.setColor(androidColor)
        if (data.isNumerical) {
            binding.activitydurationBoolSpinner.visibility = GONE
            binding.barChart.setTarget(data.target)
        } else {
            binding.activitydurationNumericalSpinner.visibility = GONE
            binding.barChart.setTarget(0.0)
        }

        binding.activitydurationNumericalSpinner.onItemSelectedListener = null
        binding.activitydurationNumericalSpinner.setSelection(data.activitydurationNumericalSpinnerPosition)
        binding.activitydurationNumericalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onActivitydurationNumericalSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.activitydurationBoolSpinner.onItemSelectedListener = null
        binding.activitydurationBoolSpinner.setSelection(data.activitydurationBoolSpinnerPosition)
        binding.activitydurationBoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onActivitydurationBoolSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}

class ActivitydurationBarCardPresenter(
        val habit: Habit,
        val firstWeekday: Int,
) {
    val numericalBucketSizes = intArrayOf(1, 7, 31, 92, 365)
    val boolBucketSizes = intArrayOf(7, 31, 92, 365)

    fun present(
            activitydurationNumericalSpinnerPosition: Int,
            activitydurationBoolSpinnerPosition: Int,
    ): ActivitydurationBarCardViewModel {
        val bucketSize = if(habit.isNumerical) {
            numericalBucketSizes[activitydurationNumericalSpinnerPosition]
        } else {
            boolBucketSizes[activitydurationBoolSpinnerPosition]
        }
        val checkmarks = if (bucketSize == 1) {
            habit.checkmarks.all
        } else {
            habit.checkmarks.groupBy(getTruncateField(bucketSize), firstWeekday)
        }
        return ActivitydurationBarCardViewModel(
                checkmarks = checkmarks,
                bucketSize = bucketSize,
                color = habit.color,
                isNumerical = habit.isNumerical,
                target = (habit.targetValue / habit.frequency.denominator * bucketSize),
                activitydurationNumericalSpinnerPosition = activitydurationNumericalSpinnerPosition,
                activitydurationBoolSpinnerPosition = activitydurationBoolSpinnerPosition,
        )
    }
}