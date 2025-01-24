package macro.hd.wallpapers.Utilily;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import macro.hd.wallpapers.WallpapersApplication;

public class NetworkChecker
{
    private Context _context;
    private boolean _isNetworkAvailable;
    private ConnectivityManager.NetworkCallback _networkCallback = null;

    public NetworkChecker(Context context)
    {
        _context = context;
        registerConnectivityCallback();
    }

    public boolean isNetworkAvailable()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return _isNetworkAvailable;
        }
        else
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null)
            {
                android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());
            }
            return true;
        }
    }

    @TargetApi(21)
    private void registerConnectivityCallback()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            return;
        }
        if (_networkCallback == null)
        {
            _networkCallback = new ConnectivityManager.NetworkCallback()
            {
                @Override
                public void onAvailable(Network network)
                {
                    _isNetworkAvailable = true;
                    Log.d("NetworkChecker", "Network Callback: onAvailable");
                }

                @Override
                public void onLost(Network network)
                {
                    _isNetworkAvailable = false;
                    Log.d("NetworkChecker", "Network Callback: onLost");
                }

                @Override
                public void onUnavailable()
                {
                    _isNetworkAvailable = false;
                    Log.d("NetworkChecker", "Network Callback: onUnavailable");
                }
            };
        }

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
            if (connectivityManager != null)
            {
                connectivityManager.registerNetworkCallback(requestBuilder.build(), _networkCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(21)
    private void unregisterConnectivityCallback()
    {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                return;
            }
            if (_networkCallback != null)
            {
                ConnectivityManager connectivityManager;
                if(_context!=null) {
                     connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
                }else
                    connectivityManager = (ConnectivityManager) WallpapersApplication.getApplication().sContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(_networkCallback);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy()
    {
        unregisterConnectivityCallback();
        _context = null;
    }
}
