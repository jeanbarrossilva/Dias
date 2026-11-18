package com.jeanbarrossilva.dias.app.util;

import static androidx.collection.ObjectIntMapKt.emptyObjectIntMap;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ObjectIntMap;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(Enclosed.class)
public final class SafeAreaInsetterTests {
  @RunWith(RobolectricTestRunner.class)
  public final static class AttachmentTests {
    private enum Attachment {
      ATTACHED {
        @Override
        public <T> T lambda() {
          return notNull();
        }
      },
      DETACHED {
        @Override
        public <T> T lambda() {
          return isNull();
        }
      };

      public abstract <T> T lambda();
    }

    @Test
    public void doesNotAttachToNullRootView() {
      final SafeAreaInsetter<?> insetter = SafeAreaInsetter.create();
      insetter.attach(
        /* rootView = */    null,
        /* constraints = */ emptyObjectIntMap()
      );
      assertThat(insetter.isAttached()).isFalse();
    }

    @Test
    public void doesNotAttachWhenConstraintMapIsNull() {
      final SafeAreaInsetter<?> insetter = SafeAreaInsetter.create();
      final View rootView = spy(new View(getApplicationContext()));
      insetter.attach(rootView, /* constraints = */ null);
      assertAttachment(insetter, rootView, /* attachment = */ null);
      reset(rootView);
    }

    @Test
    public void isAttached() {
      final SafeAreaInsetter<?> insetter = SafeAreaInsetter.create();
      final View rootView = spy(new View(getApplicationContext()));
      insetter.attach(rootView, /* constraints = */ emptyObjectIntMap());
      assertAttachment(insetter, rootView, Attachment.ATTACHED);
      reset(rootView);
    }

    @Test
    public void detachesFromPreviousRootViewWhenAttachingConsecutively() {
      final SafeAreaInsetter<?> insetter = SafeAreaInsetter.create();
      final View oldRootView = spy(new View(getApplicationContext()));
      final var newRootView = new View(getApplicationContext());
      final ObjectIntMap<View> constraints = emptyObjectIntMap();
      insetter.attach(oldRootView, constraints);
      insetter.attach(newRootView, constraints);
      assertAttachment(insetter, oldRootView, Attachment.DETACHED);
      reset(oldRootView);
    }

    @Test
    public void detaches() {
      final SafeAreaInsetter<?> insetter = SafeAreaInsetter.create();
      final View rootView = spy(new View(getApplicationContext()));
      insetter.attach(rootView, emptyObjectIntMap());
      insetter.detach();
      assertAttachment(insetter, rootView, Attachment.DETACHED);
      reset(rootView);
    }

    private void assertAttachment(
      @NonNull final SafeAreaInsetter<?> insetter,
      @NonNull final View rootView,
      @Nullable final Attachment attachment
    ) {
      if (attachment != null) {
        verify(rootView).setOnApplyWindowInsetsListener(attachment.lambda());
        verify(rootView).setWindowInsetsAnimationCallback(attachment.lambda());
      }
      final AbstractBooleanAssert<?> asserter =
        assertThat(insetter.isAttached(rootView))
          .describedAs("SafeAreaInsetter.isAttached(" + rootView + ')');
      if (attachment == Attachment.ATTACHED)
        asserter.isTrue();
      else
        asserter.isFalse();
    }
  }
}