package callerid.truecaller.trackingnumber.phonenumbertracker.block.google;

import android.content.Context;
import android.content.SharedPreferences;

public class SFun {

    private Context myActivity;
    private SharedPreferences sPref;

    public SFun(Context applicationContext) {
        myActivity = applicationContext;
    }

    public int getTimeVar(String key) {
        try {
            this.sPref = this.myActivity.getSharedPreferences("var_" + this.myActivity.getPackageName(), 0);
            return this.sPref.getInt(key, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setTimeVar(String key, int val) {
        try {
            this.sPref = this.myActivity.getSharedPreferences("var_" + this.myActivity.getPackageName(), 0);
            SharedPreferences.Editor ed = this.sPref.edit();
            ed.putInt(key, val);
            ed.commit();
        } catch (Exception e) {
        }
    }


}
