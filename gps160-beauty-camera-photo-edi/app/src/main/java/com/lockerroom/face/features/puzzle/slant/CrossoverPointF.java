package com.lockerroom.face.features.puzzle.slant;

import android.graphics.PointF;

class CrossoverPointF extends PointF {
    com.lockerroom.face.features.puzzle.slant.SlantLine horizontal;
    com.lockerroom.face.features.puzzle.slant.SlantLine vertical;

    CrossoverPointF() {
    }

    CrossoverPointF(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    CrossoverPointF(com.lockerroom.face.features.puzzle.slant.SlantLine slantLine, com.lockerroom.face.features.puzzle.slant.SlantLine slantLine2) {
        this.horizontal = slantLine;
        this.vertical = slantLine2;
    }


}
