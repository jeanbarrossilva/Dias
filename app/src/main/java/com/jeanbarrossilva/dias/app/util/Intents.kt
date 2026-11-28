/*
 * Copyright © 2026 Jean Silva
 *
 * Dias is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * Dias is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dias. If not, see https://www.gnu.org/licenses.
 */

@file:JvmName("Intents")

package com.jeanbarrossilva.dias.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Instantiates an intent for an activity of the given type.
 *
 * @param ActivityType Activity for which the intent is.
 * @param context Context in which the activity may be started.
 */
internal inline fun <reified ActivityType: Activity> intentOf(
  context: Context
) = Intent(context, ActivityType::class.java)