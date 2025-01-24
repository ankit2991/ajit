package macro.hd.wallpapers.Utilily;

import android.content.Context;

import android.os.Build;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import macro.hd.wallpapers.R;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {

    public static void configSnackbar(Context context, Snackbar snack,int bottom_margin) {
        addMargins(snack,bottom_margin);
        setRoundBordersBg(context, snack);
        ViewCompat.setElevation(snack.getView(), 6f);
    }

    private static void setRoundBordersBg(Context context, Snackbar snackbar) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                snackbar.getView().setBackground(context.getDrawable(R.drawable.bg_snackbar));
            }else
                snackbar.getView().setBackground(context.getResources().getDrawable(R.drawable.bg_snackbar));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addMargins(Snackbar snack,int bottom_margin) {
        try {
            final FrameLayout snackBarView = (FrameLayout) snack.getView();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackBarView.getChildAt(0).getLayoutParams();
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin+bottom_margin);
            snackBarView.getChildAt(0).setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}