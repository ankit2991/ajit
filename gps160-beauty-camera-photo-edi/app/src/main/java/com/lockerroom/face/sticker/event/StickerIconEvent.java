package com.lockerroom.face.sticker.event;

import android.view.MotionEvent;

import com.lockerroom.face.sticker.StickerView;

public interface StickerIconEvent {
    void onActionDown(StickerView paramStickerView, MotionEvent paramMotionEvent);

    void onActionMove(StickerView paramStickerView, MotionEvent paramMotionEvent);

    void onActionUp(StickerView paramStickerView, MotionEvent paramMotionEvent);
}
