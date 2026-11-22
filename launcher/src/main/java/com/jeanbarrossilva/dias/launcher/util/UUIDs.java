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

package com.jeanbarrossilva.dias.launcher.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/// Extensions for [UUID]s.
public final class UUIDs {
  private UUIDs() {}

   /// Instantiates a UUID from its most and least significant bits.
   ///
   /// This is the identity function of [#toByteArray(UUID)]: for a UUID
   /// `self`, `of(toByteArray(self)).equals(self)`.
   ///
   /// @param bytes Array containing, in order, the most significant bits and
   ///   the least significant ones.
   /// @return The instantiated UUID, or `null` if the array is `null`.
   /// @throws IllegalArgumentException If the array has less or more than 16
   ///   bytes (128 bits).
  public static UUID of(final byte[] bytes) throws IllegalArgumentException {
    if (bytes == null)
      return null;
    if (bytes.length != 16)
      throw new IllegalArgumentException(
        "UUID should contain 16 bytes (128 bits), but given one had "
          + bytes.length
          + ' '
          + (bytes.length == 1 ? "byte" : "bytes")
          + ": "
          + Arrays.toString(bytes)
      );
    return new UUID(
      /* mostSigBits = */ ((long) (bytes[0] & 0xff) << 56)
        | ((long) (bytes[1] & 0xff) << 48)
        | ((long) (bytes[2] & 0xff) << 40)
        | ((long) (bytes[3] & 0xff) << 32)
        | ((long) (bytes[4] & 0xff) << 24)
        | ((long) (bytes[5] & 0xff) << 16)
        | ((long) (bytes[6] & 0xff) << 8)
        | ((long) (bytes[7] & 0xff)),
      /* leastSigBits = */ ((long) (bytes[8] & 0xff) << 56)
        | ((long) (bytes[9] & 0xff) << 48)
        | ((long) (bytes[10] & 0xff) << 40)
        | ((long) (bytes[11] & 0xff) << 32)
        | ((long) (bytes[12] & 0xff) << 24)
        | ((long) (bytes[13] & 0xff) << 16)
        | ((long) (bytes[14] & 0xff) << 8)
        | ((long) (bytes[15] & 0xff))
    );
  }

  /// Converts the given UUID into an array of bytes.
  ///
  /// This is the identity function of [#of(byte\[\])]:
  /// `of(toByteArray(self)).equals(self)`.
  ///
  /// @param self UUID whose bytes will be returned.
  /// @return The UUID's bytes, or `null` if the UUID is `null`.
  public static byte[] toByteArray(final UUID self) {
    if (self == null)
      return null;
    final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
    buffer.putLong(self.getMostSignificantBits());
    buffer.putLong(self.getLeastSignificantBits());
    return buffer.array();
  }
}