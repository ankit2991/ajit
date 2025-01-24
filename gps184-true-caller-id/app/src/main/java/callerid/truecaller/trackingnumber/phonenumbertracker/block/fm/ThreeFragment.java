package callerid.truecaller.trackingnumber.phonenumbertracker.block.fm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.Glob;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

public class ThreeFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_top_view, container, false);
        ImageView img_logo = view.findViewById(R.id.img_logo);
        TextView txt_title = view.findViewById(R.id.txt_title);
        TextView txt_star = view.findViewById(R.id.txt_star);
        Button txt_install = view.findViewById(R.id.txt_install);
        TextView txt_description = view.findViewById(R.id.txt_description);

        if (Utils.Big_native != null) {
            if (Utils.Big_native.size() > 3) {
                Picasso.get().load(Utils.Big_native.get(2).getNative_icon()).into(img_logo);
                txt_title.setText(Utils.Big_native.get(2).getNative_title());
                txt_description.setText(Utils.Big_native.get(2).getNative_desc());
                txt_star.setText("4.4");
                txt_install.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Glob.isOnline(getActivity())) {
                            try {
                                Uri uri = Uri.parse(Utils.Big_native.get(2).getApp_link());
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (Exception e) {

                            }

                        } else {
                            Toast.makeText(getActivity(), "Check Your Internet Connection And Try Again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        return view;
    }
}