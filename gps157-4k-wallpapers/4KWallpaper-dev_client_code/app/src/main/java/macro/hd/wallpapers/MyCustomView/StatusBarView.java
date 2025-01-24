/**
 *
 */
package macro.hd.wallpapers.MyCustomView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import macro.hd.wallpapers.Utilily.CommonFunctions;

/**
 * @author XTPL
 */
public class StatusBarView extends RelativeLayout {

    public StatusBarView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param arg1
     */
    public StatusBarView(Context context, AttributeSet arg1) {
        super(context, arg1);
        initView(context);
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        try {
            int height1 = CommonFunctions.getStatusBarHeight(context);
            setMinimumHeight(height1);
            //getLayoutParams().height = height1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
