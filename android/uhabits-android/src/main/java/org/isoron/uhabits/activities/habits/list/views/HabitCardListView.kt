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

package org.isoron.uhabits.activities.habits.list.views

import android.content.*
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import android.view.*
import androidx.core.content.ContextCompat
import com.google.auto.factory.*
import dagger.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.*
import org.isoron.uhabits.core.models.*

@AutoFactory
class HabitCardListView(
        @Provided @ActivityContext context: Context,
        @Provided private val adapter: HabitCardListAdapter,
        @Provided private val cardViewFactory: HabitCardViewFactory,
        @Provided private val controller: Lazy<HabitCardListController>
) : RecyclerView(context, null, R.attr.scrollableRecyclerViewStyle) {

    var checkmarkCount: Int = 0

    var dataOffset: Int = 0
        set(value) {
            field = value
            attachedHolders
                    .map { it.itemView as HabitCardView }
                    .forEach { it.dataOffset = value }
        }

    private val attachedHolders = mutableListOf<HabitCardViewHolder>()
    private val touchHelper = ItemTouchHelper(TouchHelperCallback()).apply {
        attachToRecyclerView(this@HabitCardListView)
    }
    val swipeToDeleteCallBack = object : SwipeToDeleteCallBack(){
        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            controller.get().onItemSwipe(position)
            adapter.notifyItemRemoved(position)
        }
    }
    val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack).apply {
        attachToRecyclerView(this@HabitCardListView)
    }

    init {
        setHasFixedSize(true)
        isLongClickable = true
        layoutManager = LinearLayoutManager(context)
        super.setAdapter(adapter)
    }

    fun createHabitCardView(): View {
        return cardViewFactory.create()
    }

    fun bindCardView(holder: HabitCardViewHolder,
                     habit: Habit,
                     score: Double,
                     checkmarks: IntArray,
                     selected: Boolean): View {
        val cardView = holder.itemView as HabitCardView
        cardView.habit = habit
        cardView.isSelected = selected
        cardView.values = checkmarks
        cardView.buttonCount = checkmarkCount
        cardView.dataOffset = dataOffset
        cardView.score = score
        cardView.unit = habit.unit
        cardView.threshold = habit.targetValue / habit.frequency.denominator

        val detector = GestureDetector(context, CardViewGestureDetector(holder))
        cardView.setOnTouchListener { _, ev ->
            detector.onTouchEvent(ev)
            true
        }

        return cardView
    }

    fun attachCardView(holder: HabitCardViewHolder) {
        attachedHolders.add(holder)
    }

    fun detachCardView(holder: HabitCardViewHolder) {
        attachedHolders.remove(holder)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter.onAttached()
    }

    override fun onDetachedFromWindow() {
        adapter.onDetached()
        super.onDetachedFromWindow()
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is BundleSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        dataOffset = state.bundle.getInt("dataOffset")
        super.onRestoreInstanceState(state.superState)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle().apply {
            putInt("dataOffset", dataOffset)
        }
        return BundleSavedState(superState, bundle)
    }

    interface Controller {
        fun drop(from: Int, to: Int) {}
        fun onItemClick(pos: Int) {}
        fun onItemLongClick(pos: Int) {}
        fun onItemSwipe(pos: Int) {}
        fun startDrag(position: Int) {}
    }

    private inner class CardViewGestureDetector(
            private val holder: HabitCardViewHolder
    ) : GestureDetector.SimpleOnGestureListener() {

        override fun onLongPress(e: MotionEvent) {
            val position = holder.adapterPosition
            controller.get().onItemLongClick(position)
            if (adapter.isSortable) touchHelper.startDrag(holder)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val position = holder.adapterPosition
            controller.get().onItemClick(position)
            return true
        }
    }

    inner class TouchHelperCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView,
                                      viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(UP or DOWN, START or END)
        }

        override fun onMove(recyclerView: RecyclerView,
                            from: RecyclerView.ViewHolder,
                            to: RecyclerView.ViewHolder): Boolean {
            controller.get().drop(from.adapterPosition, to.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                              direction: Int) {
        }

        override fun isItemViewSwipeEnabled() = false
        override fun isLongPressDragEnabled() = false

    }

    inner abstract class SwipeToDeleteCallBack : ItemTouchHelper.Callback() {
        private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24)
        private val intrinsicWidth = deleteIcon!!.intrinsicWidth
        private val intrinsicHeight = deleteIcon!!.intrinsicHeight
        private val background = ColorDrawable()
        private val backgroundColor = Color.parseColor("#f44336")

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            val swipeFlag = ItemTouchHelper.LEFT
            return makeMovementFlags(0, swipeFlag)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
            return false
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            //Draw red delete background
            background.color = backgroundColor
            background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
            )
            background.draw(c)

            //Calculate position of delete icon
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight)/2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            //Draw delete icon
            deleteIcon!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        override fun isItemViewSwipeEnabled() = true
    }
}
