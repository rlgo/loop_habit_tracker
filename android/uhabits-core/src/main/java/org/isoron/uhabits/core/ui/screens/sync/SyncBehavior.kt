/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui.screens.sync

import org.isoron.uhabits.core.io.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.sync.*

class SyncBehavior(
        val screen: Screen,
        val preferences: Preferences,
        val server: AbstractSyncServer,
        val logging: Logging,
) {
    val logger = logging.getLogger("SyncBehavior")

    suspend fun onResume() {
        if (preferences.syncKey.isBlank()) {
            register()
        } else {
            displayCurrentKey()
        }
    }

    suspend fun displayCurrentKey() {
        screen.showLink("https://loophabits.org/sync/${preferences.syncKey}#${preferences.encryptionKey}")
    }

    suspend fun register() {
        screen.showLoadingScreen()
        try {
            val syncKey = server.register()
            val encKey = EncryptionKey.generate()
            preferences.enableSync(syncKey, encKey.base64)
            displayCurrentKey()
        } catch (e: Exception) {
            logger.error("Unexpected exception")
            logger.error(e)
            screen.showErrorScreen()
        }
    }

    interface Screen {
        suspend fun showLoadingScreen()
        suspend fun showErrorScreen()
        suspend fun showLink(link: String)
    }
}