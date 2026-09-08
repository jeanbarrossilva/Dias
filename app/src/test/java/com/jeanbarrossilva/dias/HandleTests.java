package com.jeanbarrossilva.dias;

import androidx.annotation.NonNull;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HandleTests {
  @Test
  public void isUnpinnedByDefault() {
    final Handle handle = sampleHandle();
    assertThat(handle).extracting(Handle::isPinned).isEqualTo(false);
    assertThat(handle)
      .extracting(Handle::getPinIndex)
      .isEqualTo(Handle.UNPINNED);
  }

  @Test
  public void throwsWhenPinningAtNegativeIndex() {
    final Handle handle = sampleHandle();
    assertThatThrownBy(() -> handle.pin(Handle.UNPINNED))
      .isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  public void pins() {
    final Handle handle = sampleHandle();
    handle.pin(2);
    assertThat(handle).extracting(Handle::isPinned).isEqualTo(true);
    assertThat(handle).extracting(Handle::getPinIndex).isEqualTo(2);
  }

  @Test
  public void unpins() {
    final Handle handle = sampleHandle();
    handle.pin(2);
    handle.unpin();
    assertThat(handle).extracting(Handle::isPinned).isEqualTo(false);
    assertThat(handle)
      .extracting(Handle::getPinIndex)
      .isEqualTo(Handle.UNPINNED);
  }

  @NonNull
  private static Handle sampleHandle() {
    return new Handle(/* id = */ 0, /* label = */ "Dias");
  }
}