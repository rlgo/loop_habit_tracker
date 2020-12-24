/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.utils

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import android.view.*
import android.view.ViewGroup.LayoutParams.*
import android.widget.*
import android.widget.RelativeLayout.*
import androidx.annotation.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.*
import com.google.android.material.snackbar.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import java.io.*

fun RelativeLayout.addBelow(view: View,
                            subject: View,
                            width: Int = MATCH_PARENT,
                            height: Int = WRAP_CONTENT,
                            applyCustomRules: (params: RelativeLayout.LayoutParams) -> Unit = {}) {

    view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        addRule(BELOW, subject.id)
        applyCustomRules(this)
    }
    view.id = View.generateViewId()
    this.addView(view)
}

fun RelativeLayout.addAtBottom(view: View,
                               width: Int = MATCH_PARENT,
                               height: Int = WRAP_CONTENT) {

    view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        addRule(ALIGN_PARENT_BOTTOM)
    }
    view.id = View.generateViewId()
    this.addView(view)
}

fun RelativeLayout.addAtTop(view: View,
                            width: Int = MATCH_PARENT,
                            height: Int = WRAP_CONTENT) {

    view.layoutParams = RelativeLayout.LayoutParams(width, height).apply {
        addRule(ALIGN_PARENT_TOP)
    }
    view.id = View.generateViewId()
    this.addView(view)
}

fun ViewGroup.buildToolbar(): Toolbar {
    val inflater = LayoutInflater.from(context)
    return inflater.inflate(R.layout.toolbar, null) as Toolbar
}

fun View.showMessage(@StringRes stringId: Int) {
    try {
        val snackbar = Snackbar.make(this, stringId, Snackbar.LENGTH_SHORT)
        val tvId = R.id.snackbar_text
        val tv = snackbar.view.findViewById<TextView>(tvId)
        tv?.setTextColor(Color.WHITE)
        snackbar.show()
    } catch (e: IllegalArgumentException) {
        return
    }
}

fun Activity.showMessage(@StringRes stringId: Int) {
    this.findViewById<View>(android.R.id.content).showMessage(stringId)
}

fun Activity.showSendFileScreen(archiveFilename: String) {
    val file = File(archiveFilename)
    val fileUri = FileProvider.getUriForFile(this, "org.isoron.uhabits", file)
    this.startActivity(Intent().apply {
        action = Intent.ACTION_SEND
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    })
}

fun View.setupToolbar(toolbar: Toolbar, title: String, color: PaletteColor) {
    toolbar.elevation = InterfaceUtils.dpToPixels(context, 2f)
    val res = StyledResources(context)
    toolbar.title = title
    val toolbarColor = if (!res.getBoolean(R.attr.useHabitColorAsPrimary)) {
        StyledResources(context).getColor(org.isoron.androidbase.R.attr.colorPrimary)
    } else {
        color.toThemedAndroidColor(context)
    }
    val darkerColor = ColorUtils.mixColors(toolbarColor, Color.BLACK, 0.75f)
    toolbar.background = ColorDrawable(toolbarColor)
    val activity = context as AppCompatActivity
    activity.window.statusBarColor = darkerColor
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
}

fun Int.toMeasureSpec(mode: Int) =
        View.MeasureSpec.makeMeasureSpec(this, mode)

fun Float.toMeasureSpec(mode: Int) =
        View.MeasureSpec.makeMeasureSpec(toInt(), mode)

fun View.isRTL() = InterfaceUtils.isLayoutRtl(this)
fun View.getFontAwesome() = InterfaceUtils.getFontAwesome(context)!!
fun View.dim(id: Int) = InterfaceUtils.getDimension(context, id)
fun View.sp(value: Float) = InterfaceUtils.spToPixels(context, value)
fun View.dp(value: Float) = InterfaceUtils.dpToPixels(context, value)
fun View.str(id: Int) = resources.getString(id)
val View.sres: StyledResources
    get() = StyledResources(context)
