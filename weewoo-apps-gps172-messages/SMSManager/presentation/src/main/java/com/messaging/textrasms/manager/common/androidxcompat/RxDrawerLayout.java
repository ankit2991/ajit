package com.messaging.textrasms.manager.common.androidxcompat;

import static com.jakewharton.rxbinding2.internal.Preconditions.checkNotNull;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.jakewharton.rxbinding2.InitialValueObservable;

import io.reactivex.functions.Consumer;

public final class RxDrawerLayout {
    private RxDrawerLayout() {
        throw new AssertionError("No instances.");
    }

    @CheckResult
    @NonNull
    public static InitialValueObservable<Boolean> drawerOpen(
            @NonNull DrawerLayout view, int gravity) {
        checkNotNull(view, "view == null");
        return new DrawerLayoutDrawerOpenedObservable(view, gravity);
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Boolean> open(
            @NonNull final DrawerLayout view, final int gravity) {
        checkNotNull(view, "view == null");
        return new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {
                    view.openDrawer(gravity);
                } else {
                    view.closeDrawer(gravity);
                }
            }
        };
    }
}