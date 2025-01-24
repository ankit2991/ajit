package com.lockerroom.face.features.puzzle.layout.straight;

import com.lockerroom.face.features.puzzle.Line;
import com.lockerroom.face.features.puzzle.PuzzleLayout;
import com.lockerroom.face.features.puzzle.straight.StraightPuzzleLayout;

public class FourStraightLayout extends NumberStraightLayout {
    private static final String TAG = "FourStraightLayout";

    public int getThemeCount() {
        return 8;
    }

    public FourStraightLayout(StraightPuzzleLayout straightPuzzleLayout, boolean z) {
        super(straightPuzzleLayout, z);
    }

    public FourStraightLayout(int i) {
        super(i);
    }

    public void layout() {
        switch (this.theme) {
            case 0:
                addCross(0, 0.5f);
                return;
            case 1:
                addLine(0, Line.Direction.HORIZONTAL, 0.33333334f);
                cutAreaEqualPart(0, 3, Line.Direction.VERTICAL);
                return;
            case 2:
                addLine(0, Line.Direction.HORIZONTAL, 0.6666667f);
                cutAreaEqualPart(1, 3, Line.Direction.VERTICAL);
                return;
            case 3:
                addLine(0, Line.Direction.VERTICAL, 0.33333334f);
                cutAreaEqualPart(0, 3, Line.Direction.HORIZONTAL);
                return;
            case 4:
                addLine(0, Line.Direction.VERTICAL, 0.6666667f);
                cutAreaEqualPart(1, 3, Line.Direction.HORIZONTAL);
                return;
            case 5:
                addLine(0, Line.Direction.VERTICAL, 0.5f);
                addLine(1, Line.Direction.HORIZONTAL, 0.6666667f);
                addLine(1, Line.Direction.HORIZONTAL, 0.33333334f);
                return;
            case 6:
                cutAreaEqualPart(0, 4, Line.Direction.HORIZONTAL);
                return;
            case 7:
                cutAreaEqualPart(0, 4, Line.Direction.VERTICAL);
                return;
            default:
                cutAreaEqualPart(0, 4, Line.Direction.HORIZONTAL);
                return;
        }
    }

    public PuzzleLayout clone(PuzzleLayout puzzleLayout) {
        return new FourStraightLayout((StraightPuzzleLayout) puzzleLayout, true);
    }
}
