/*
 * Copyright © 2026 Jean Silva
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses.
 */

package com.jeanbarrossilva.dias.app.util;

import static com.jeanbarrossilva.dias.launcher.util.UUIDs.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;

import static java.util.UUID.randomUUID;

import com.jeanbarrossilva.dias.launcher.util.UUIDs;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(Enclosed.class)
public final class UUIDsTests {
  public static final class ToByteArrayTests {
    @Test
    public void returnsNullWhenUUIDIsNull() {
      assertThat(toByteArray(null)).isNull();
    }

    @Test
    public void converts() {
      final UUID uuid = randomUUID();
      assertThat(UUIDs.of(toByteArray(uuid))).isEqualTo(uuid);
    }
  }
}