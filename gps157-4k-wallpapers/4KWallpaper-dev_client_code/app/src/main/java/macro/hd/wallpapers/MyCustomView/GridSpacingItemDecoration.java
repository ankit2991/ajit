package macro.hd.wallpapers.MyCustomView;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    public void setPos0Apply(boolean pos0Apply) {
        isPos0Apply = pos0Apply;
    }

    boolean isPos0Apply=false;

    public void setPos1Apply(boolean pos1Apply) {
        isPos1Apply = pos1Apply;
    }

    boolean isPos1Apply=false;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position


        if (includeEdge) {
            int column = position % spanCount; // item column
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom
        } else {
            if((position==0 && isPos0Apply) || ((isPos0Apply?position==1:position==0)  && isPos1Apply)){
                //spanCount=1;
            }else {
                if(isPos0Apply && isPos1Apply)
                    position=position-2;
                else if(isPos0Apply)
                    position=position-1;
                else if(isPos0Apply || isPos1Apply)
                    position=position-1;

                int column = position % spanCount; // item column
                spanCount = 2;
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}