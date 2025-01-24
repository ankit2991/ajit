package com.lockerroom.face.utils;

import com.lockerroom.face.features.puzzle.PuzzleLayout;
import com.lockerroom.face.features.puzzle.layout.slant.SlantLayoutHelper;
import com.lockerroom.face.features.puzzle.layout.straight.StraightLayoutHelper;

import java.util.ArrayList;
import java.util.List;

public class PuzzleUtils {

    private PuzzleUtils() {
    }

    public static List<PuzzleLayout> getPuzzleLayouts(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(SlantLayoutHelper.getAllThemeLayout(i));
        arrayList.addAll(StraightLayoutHelper.getAllThemeLayout(i));
        return arrayList;
    }
}
