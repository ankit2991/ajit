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
import android.widget.TextView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.State_Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;


public class State_Adapter extends BaseAdapter {
    ArrayList<State_Data> alData = new ArrayList();
    ArrayList<State_Data> alDataTemp = new ArrayList();
    Context context;
    LayoutInflater inflater;
    String stQuerry = "";

    static class ViewHolder {
        TextView text;

        ViewHolder() {
        }
    }

    public State_Adapter(Context context, ArrayList<State_Data> alData) {
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
        this.inflater = (LayoutInflater) this.context.getSystemService("layout_inflater");
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.cell_state, null);
            holder.text = (TextView) convertView.findViewById(R.id.tv_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String filter = this.stQuerry;
        String itemValue = ((State_Data) this.alDataTemp.get(position)).getState();
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
                State_Data wp = (State_Data) it.next();
                if (new StringBuilder(String.valueOf(wp.getState().trim())).append(wp.getState()).toString().replace(" ", "").toLowerCase().contains(charText)) {
                    this.alDataTemp.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}
