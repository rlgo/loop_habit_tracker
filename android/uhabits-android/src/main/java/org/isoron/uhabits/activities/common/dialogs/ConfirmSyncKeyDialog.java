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

package org.isoron.uhabits.activities.common.dialogs;

import android.content.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;

import com.google.auto.factory.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.R;

import butterknife.*;

@AutoFactory(allowSubclasses = true)
public class ConfirmSyncKeyDialog extends AlertDialog
{
    @BindString(R.string.sync_confirm)
    protected String question;

    @BindString(R.string.yes)
    protected String yes;

    @BindString(R.string.no)
    protected String no;

    protected ConfirmSyncKeyDialog(@Provided @ActivityContext Context context,
                                   @NonNull OnConfirmedCallback callback)
    {
        super(context);
        ButterKnife.bind(this);

        setTitle(R.string.device_sync);
        setMessage(question);
        setButton(BUTTON_POSITIVE, yes, (dialog, which) -> callback.onConfirmed());
        setButton(BUTTON_NEGATIVE, no, (dialog, which) -> {});
    }
}
