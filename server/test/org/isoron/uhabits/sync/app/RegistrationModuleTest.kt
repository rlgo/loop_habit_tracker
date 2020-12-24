/*
 * Copyright (C) 2016-2020 Alinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.sync.app

import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import org.isoron.uhabits.sync.*
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import kotlin.test.*

class RegistrationModuleTest : BaseApplicationTest() {
    @Test
    fun `when register succeeds should return generated key`():Unit = runBlocking {
        `when`(server.register()).thenReturn("ABCDEF")
        withTestApplication(app()) {
            val call = handleRequest(HttpMethod.Post, "/register")
            assertEquals(HttpStatusCode.OK, call.response.status())
            assertEquals("{\"key\":\"ABCDEF\"}", call.response.content)
        }
    }

    @Test
    fun `when registration is unavailable should return 503`():Unit = runBlocking {
        `when`(server.register()).thenThrow(ServiceUnavailable())
        withTestApplication(app()) {
            val call = handleRequest(HttpMethod.Post, "/register")
            assertEquals(HttpStatusCode.ServiceUnavailable, call.response.status())
        }
    }
}