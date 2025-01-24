package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.STD_Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class STD_Adapter extends BaseAdapter {
    ArrayList<STD_Data> alData = new ArrayList();
    ArrayList<STD_Data> alDataTemp = new ArrayList();
    Context context;
    LayoutInflater inflater;
    String stQuerry = "";
    private int position1;

    static class ViewHolder {
        public LinearLayout layout1;
        TextView text;
    }

    public STD_Adapter(Context context, ArrayList<STD_Data> alData) {
        this.context = context;
        this.alData.addAll(alData);
        this.alDataTemp.addAll(alData);
    }

    public int getCount() {
        return this.alDataTemp.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        position1 = position;
        this.inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.cell_std, null);
            holder.text = (TextView) convertView.findViewById(R.id.tv_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String filter = this.stQuerry;
        String itemValue = new StringBuilder(String.valueOf(((STD_Data) this.alDataTemp.get(position)).getCity())).append("\n").append(((STD_Data) this.alDataTemp.get(position)).getCode()).toString();
        int startPos = itemValue.toLowerCase(Locale.US).indexOf(filter.toLowerCase(Locale.US));
        int endPos = startPos + filter.length();
        if (startPos != -1) {
            Spannable spannable = new SpannableString(itemValue);
            spannable.setSpan(new TextAppearanceSpan(null, 0, -1, new ColorStateList(new int[][]{new int[0]}, new int[]{this.context.getResources().getColor(R.color.orange_highligh)}), null), startPos, endPos, 33);
            holder.text.setText(spannable);
        } else {
            holder.text.setText(itemValue);
        }
        return convertView;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase().replace(" ", "");
        this.stQuerry = charText;
        this.alDataTemp.clear();
        if (charText.length() == 0) {
            this.alDataTemp.addAll(this.alData);
        } else {
            Iterator it = this.alData.iterator();
            while (it.hasNext()) {
                STD_Data wp = (STD_Data) it.next();
                if (new StringBuilder(String.valueOf(wp.getCity().trim())).append(wp.getCode()).toString().replace(" ", "").toLowerCase().contains(charText)) {
                    this.alDataTemp.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}
