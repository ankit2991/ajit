package com.lockerroom.face.features.puzzle.layout.slant;

import com.lockerroom.face.features.puzzle.Line;
import com.lockerroom.face.features.puzzle.PuzzleLayout;
import com.lockerroom.face.features.puzzle.slant.SlantPuzzleLayout;

public class OneSlantLayout extends NumberSlantLayout {
    public int getThemeCount() {
        return 4;
    }

    public OneSlantLayout(SlantPuzzleLayout slantPuzzleLayout, boolean z) {
        super(slantPuzzleLayout, z);
    }

    public OneSlantLayout(int i) {
        super(i);
    }

    public void layout() {
        switch (this.theme) {
            case 0:
                addLine(0, Line.Direction.HORIZONTAL, 0.56f, 0.44f);
                return;
            case 1:
                addLine(0, Line.Direction.VERTICAL, 0.56f, 0.44f);
                return;
            case 2:
                addCross(0, 0.56f, 0.44f, 0.56f, 0.44f);
                return;
            case 3:
                cutArea(0, 1, 2);
                return;
            default:
                return;
        }
    }

    public PuzzleLayout clone(PuzzleLayout puzzleLayout) {
        return new OneSlantLayout((SlantPuzzleLayout) puzzleLayout, true);
    }
}
