package macro.hd.wallpapers.Utilily;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;

import java.lang.ref.WeakReference;

/**
 * Created by yarolegovich on 08.03.2017.
 */

public class DiscreteScrollViewOptions {

    private static DiscreteScrollViewOptions instance;

    private final String KEY_TRANSITION_TIME;

    public static void init(Context context) {
        instance = new DiscreteScrollViewOptions(context);
    }

    private DiscreteScrollViewOptions(Context context) {
        KEY_TRANSITION_TIME = context.getString(R.string.pref_key_transition_time);
    }


    public static void smoothScrollToUserSelectedPosition(final DiscreteScrollView scrollView, View anchor) {
        PopupMenu popupMenu = new PopupMenu(scrollView.getContext(), anchor);
        Menu menu = popupMenu.getMenu();
        final RecyclerView.Adapter<?> adapter = scrollView.getAdapter();
        int itemCount = (adapter instanceof InfiniteScrollAdapter) ?
                ((InfiniteScrollAdapter<?>) adapter).getRealItemCount() :
                (adapter != null ? adapter.getItemCount() : 0);
        for (int i = 0; i < itemCount; i++) {
            menu.add(String.valueOf(i + 1));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int destination = Integer.parseInt(String.valueOf(item.getTitle())) - 1;
                if (adapter instanceof InfiniteScrollAdapter) {
                    destination = ((InfiniteScrollAdapter<?>) adapter).getClosestPosition(destination);
                }
                scrollView.smoothScrollToPosition(destination);
                return true;
            }
        });
        popupMenu.show();
    }

    public static int getTransitionTime() {
        return defaultPrefs().getInt(instance.KEY_TRANSITION_TIME, 150);
    }

    @SuppressWarnings("deprecation")
    private static SharedPreferences defaultPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(WallpapersApplication.sContext);
    }

    private static class TransitionTimeChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        private WeakReference<DiscreteScrollView> scrollView;

        public TransitionTimeChangeListener(DiscreteScrollView scrollView) {
            this.scrollView = new WeakReference<>(scrollView);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(instance.KEY_TRANSITION_TIME)) {
                DiscreteScrollView scrollView = this.scrollView.get();
                if (scrollView != null) {
                    scrollView.setItemTransitionTimeMillis(sharedPreferences.getInt(key, 150));
                } else {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                }
            }
        }
    }
}
