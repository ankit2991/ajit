package com.lockerroom.face.features.picker.event;

import com.lockerroom.face.features.picker.entity.Photo;

public interface Selectable {

    int getSelectedItemCount();

    boolean isSelected(Photo photo);

}
