
package org.isoron.uhabits.activities.habits.show.views

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.activities.habits.show.views.ScoreCardPresenter.Companion.getTruncateField
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

data class CalorieBarCardViewModel(
        val checkmarks: List<Checkmark>,
        val bucketSize: Int,
        val color: PaletteColor,
        val isNumerical: Boolean,
        val target: Double,
        val calorieNumericalSpinnerPosition: Int,
        val calorieBoolSpinnerPosition: Int,
)

class CalorieBarCard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitCaloriebarBinding.inflate(LayoutInflater.from(context), this)
    var onCalorieNumericalSpinnerPosition: (position: Int) -> Unit = {}
    var onCalorieBoolSpinnerPosition: (position: Int) -> Unit = {}


    fun update(data: CalorieBarCardViewModel) {

        Log.d("test calorie bar card",data.toString())

        binding.barChart.setCheckmarks(data.checkmarks)
        binding.barChart.setBucketSize(data.bucketSize)

        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
//        binding.title.setText("Calorie")
        binding.barChart.setColor(androidColor)
        if (data.isNumerical) {
            binding.calorieBoolSpinner.visibility = GONE
            binding.barChart.setTarget(data.target)
        } else {
            binding.calorieNumericalSpinner.visibility = GONE
            binding.barChart.setTarget(0.0)
        }

        binding.calorieNumericalSpinner.onItemSelectedListener = null
        binding.calorieNumericalSpinner.setSelection(data.calorieNumericalSpinnerPosition)
        binding.calorieNumericalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onCalorieNumericalSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.calorieBoolSpinner.onItemSelectedListener = null
        binding.calorieBoolSpinner.setSelection(data.calorieBoolSpinnerPosition)
        binding.calorieBoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onCalorieBoolSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}

class CalorieBarCardPresenter(
        val habit: Habit,
        val firstWeekday: Int,
) {
    val numericalBucketSizes = intArrayOf(1, 7, 31, 92, 365)
    val boolBucketSizes = intArrayOf(7, 31, 92, 365)

    fun present(
            calorieNumericalSpinnerPosition: Int,
            calorieBoolSpinnerPosition: Int,
    ): CalorieBarCardViewModel {
        val bucketSize = if(habit.isNumerical) {
            numericalBucketSizes[calorieNumericalSpinnerPosition]
        } else {
            boolBucketSizes[calorieBoolSpinnerPosition]
        }
        val checkmarks = if (bucketSize == 1) {
            habit.checkmarks.all
        } else {
            habit.checkmarks.groupBy(getTruncateField(bucketSize), firstWeekday)
        }
        return CalorieBarCardViewModel(
                checkmarks = checkmarks,
                bucketSize = bucketSize,
                color = habit.color,
                isNumerical = habit.isNumerical,
                target = (habit.targetValue / habit.frequency.denominator * bucketSize),
                calorieNumericalSpinnerPosition = calorieNumericalSpinnerPosition,
                calorieBoolSpinnerPosition = calorieBoolSpinnerPosition,
        )
    }
}