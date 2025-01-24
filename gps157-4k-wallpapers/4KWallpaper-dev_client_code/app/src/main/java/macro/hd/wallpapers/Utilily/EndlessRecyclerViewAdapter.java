package macro.hd.wallpapers.Utilily;

import android.content.Context;
import androidx.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import macro.hd.wallpapers.R;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;


public class EndlessRecyclerViewAdapter extends RecyclerViewAdapterWrapper {
    public static final int TYPE_PENDING = 999;
    private final Context context;
    private final int pendingViewResId;
    private AtomicBoolean keepOnAppending;
    private AtomicBoolean dataPending;
    private RequestToLoadMoreListener requestToLoadMoreListener;
    private int type;

    public void setUpdateLastOnly(boolean updateLastOnly) {
        isUpdateLastOnly = updateLastOnly;
    }

    private boolean isUpdateLastOnly;

    public EndlessRecyclerViewAdapter(Context context, Adapter wrapped, RequestToLoadMoreListener requestToLoadMoreListener, @LayoutRes int pendingViewResId, boolean keepOnAppending, int type) {
        super(wrapped);
        this.context = context;
        this.requestToLoadMoreListener = requestToLoadMoreListener;
        this.pendingViewResId = pendingViewResId;
        this.keepOnAppending = new AtomicBoolean(keepOnAppending);
        dataPending = new AtomicBoolean(false);
        this.type = type;
    }

    public EndlessRecyclerViewAdapter(Context context, Adapter wrapped, RequestToLoadMoreListener requestToLoadMoreListener, int type) {
        this(context, wrapped, requestToLoadMoreListener, R.layout.item_loading, true, type);
    }

    public EndlessRecyclerViewAdapter(Context context, Adapter wrapped, RequestToLoadMoreListener requestToLoadMoreListener) {
        this(context, wrapped, requestToLoadMoreListener, R.layout.item_loading, true, -1);
    }

    private void stopAppending() {
        setKeepOnAppending(false);
    }

    /**
     * Let the adapter know that data is load and ready to view.
     *
     * @param keepOnAppending whether the adapter should request to load more when scrolling to the bottom.
     */
    public void onDataReady(boolean keepOnAppending) {
        dataPending.set(false);
        setKeepOnAppending(keepOnAppending);
    }

    private void setKeepOnAppending(boolean newValue) {
        keepOnAppending.set(newValue);
//        getWrappedAdapter().notifyDataSetChanged();
        if(isUpdateLastOnly)
            getWrappedAdapter().notifyItemChanged(getItemCount());
        else
            getWrappedAdapter().notifyDataSetChanged();
    }

    /**
     *
     */
    public void restartAppending() {
        dataPending.set(false);
        setKeepOnAppending(true);
    }

    private View getPendingView(ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(pendingViewResId, viewGroup, false);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (keepOnAppending.get() ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getWrappedAdapter().getItemCount()) {
            return TYPE_PENDING;
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PENDING) {
            return new PendingViewHolder(getPendingView(parent));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PENDING) {
            if (!dataPending.get()) {
                dataPending.set(true);
                requestToLoadMoreListener.onLoadMoreRequested();
            }
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    public interface RequestToLoadMoreListener {
        /**
         * The adapter requests to load more data.
         */
        void onLoadMoreRequested();
    }

    static class PendingViewHolder extends ViewHolder {

        public PendingViewHolder(View itemView) {
            super(itemView);
        }
    }
}

