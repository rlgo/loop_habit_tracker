package org.isoron.uhabits.activities.habits.show.views

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.activities.habits.show.views.ScoreCardPresenter.Companion.getTruncateField
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

data class HydrationBarCardViewModel(
        val checkmarks: List<Checkmark>,
        val bucketSize: Int,
        val color: PaletteColor,
        val isNumerical: Boolean,
        val target: Double,
        val hydrationNumericalSpinnerPosition: Int,
        val hydrationBoolSpinnerPosition: Int,
)

class HydrationBarCard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitHydrationbarBinding.inflate(LayoutInflater.from(context), this)
    var onHydrationNumericalSpinnerPosition: (position: Int) -> Unit = {}
    var onHydrationBoolSpinnerPosition: (position: Int) -> Unit = {}


    fun update(data: HydrationBarCardViewModel) {

        binding.barChart.setCheckmarks(data.checkmarks)
        binding.barChart.setBucketSize(data.bucketSize)

        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
        binding.barChart.setColor(androidColor)
        if (data.isNumerical) {
            binding.hydrationBoolSpinner.visibility = GONE
            binding.barChart.setTarget(data.target)
        } else {
            binding.hydrationNumericalSpinner.visibility = GONE
            binding.barChart.setTarget(0.0)
        }

        binding.hydrationNumericalSpinner.onItemSelectedListener = null
        binding.hydrationNumericalSpinner.setSelection(data.hydrationNumericalSpinnerPosition)
        binding.hydrationNumericalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onHydrationNumericalSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.hydrationBoolSpinner.onItemSelectedListener = null
        binding.hydrationBoolSpinner.setSelection(data.hydrationBoolSpinnerPosition)
        binding.hydrationBoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onHydrationBoolSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}

class HydrationBarCardPresenter(
        val habit: Habit,
        val firstWeekday: Int,
) {
    val numericalBucketSizes = intArrayOf(1, 7, 31, 92, 365)
    val boolBucketSizes = intArrayOf(7, 31, 92, 365)

    fun present(
            hydrationNumericalSpinnerPosition: Int,
            hydrationBoolSpinnerPosition: Int,
    ): HydrationBarCardViewModel {
        val bucketSize = if(habit.isNumerical) {
            numericalBucketSizes[hydrationNumericalSpinnerPosition]
        } else {
            boolBucketSizes[hydrationBoolSpinnerPosition]
        }
        val checkmarks = if (bucketSize == 1) {
            habit.checkmarks.all
        } else {
            habit.checkmarks.groupBy(getTruncateField(bucketSize), firstWeekday)
        }
        return HydrationBarCardViewModel(
                checkmarks = checkmarks,
                bucketSize = bucketSize,
                color = habit.color,
                isNumerical = habit.isNumerical,
                target = (habit.targetValue / habit.frequency.denominator * bucketSize),
                hydrationNumericalSpinnerPosition = hydrationNumericalSpinnerPosition,
                hydrationBoolSpinnerPosition = hydrationBoolSpinnerPosition,
        )
    }
}