package macro.hd.wallpapers.MyCustomView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;


/**
 * Created by hungama1 on 27/4/16.
 */
public class TextViewBold extends androidx.appcompat.widget.AppCompatTextView {

    public TextViewBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TextViewBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextViewBold(Context context) {
        super(context);
        init();
    }

    public static Typeface tf;

    public void init() {
        try {
//            if(tf ==null)
//                tf = Typeface.createFromAsset(getContext().getAssets(), AppConstant.TYPE_ROBOTO_CONDENSE_BOLD);
//
//            setTypeface(tf ,1);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
