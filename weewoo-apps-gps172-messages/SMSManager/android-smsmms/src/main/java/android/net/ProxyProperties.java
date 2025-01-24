package android.net;


import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.net.InetSocketAddress;

public class ProxyProperties implements Parcelable {

    public static final Creator<ProxyProperties> CREATOR =
            new Creator<ProxyProperties>() {
                public ProxyProperties createFromParcel(Parcel in) {
                    String host = null;
                    int port = 0;
                    if (in.readByte() == 1) {
                        host = in.readString();
                        port = in.readInt();
                    }
                    String exclList = in.readString();
                    ProxyProperties proxyProperties =
                            new ProxyProperties(host, port, exclList, null);
                    return proxyProperties;
                }

                public ProxyProperties[] newArray(int size) {
                    return new ProxyProperties[size];
                }
            };
    private String mHost;
    private int mPort;
    private String mExclusionList;
    private String[] mParsedExclusionList;

    private ProxyProperties(String host, int port, String exclList, String[] parsedExclList) {
        mHost = host;
        mPort = port;
        mExclusionList = exclList;
        mParsedExclusionList = parsedExclList;
    }

    public ProxyProperties(ProxyProperties source) {
        if (source != null) {
            mHost = source.getHost();
            mPort = source.getPort();
            mExclusionList = source.getExclusionList();
            mParsedExclusionList = source.mParsedExclusionList;
        }
    }

    public InetSocketAddress getSocketAddress() {
        InetSocketAddress inetSocketAddress = null;
        try {
            inetSocketAddress = new InetSocketAddress(mHost, mPort);
        } catch (IllegalArgumentException e) {
        }
        return inetSocketAddress;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public String getExclusionList() {
        return mExclusionList;
    }

    @SuppressLint("DefaultLocale")
    private void setExclusionList(String exclusionList) {
        mExclusionList = exclusionList;
        if (mExclusionList == null) {
            mParsedExclusionList = new String[0];
        } else {
            String[] splitExclusionList = exclusionList.toLowerCase().split(",");
            mParsedExclusionList = new String[splitExclusionList.length * 2];
            for (int i = 0; i < splitExclusionList.length; i++) {
                String s = splitExclusionList[i].trim();
                if (s.startsWith(".")) s = s.substring(1);
                mParsedExclusionList[i * 2] = s;
                mParsedExclusionList[(i * 2) + 1] = "." + s;
            }
        }
    }

    public boolean isExcluded(String url) {
        if (TextUtils.isEmpty(url) || mParsedExclusionList == null ||
                mParsedExclusionList.length == 0) return false;

        Uri u = Uri.parse(url);
        String urlDomain = u.getHost();
        if (urlDomain == null) return false;
        for (int i = 0; i < mParsedExclusionList.length; i += 2) {
            if (urlDomain.equals(mParsedExclusionList[i]) ||
                    urlDomain.endsWith(mParsedExclusionList[i + 1])) {
                return true;
            }
        }
        return false;
    }

    public java.net.Proxy makeProxy() {
        java.net.Proxy proxy = java.net.Proxy.NO_PROXY;
        if (mHost != null) {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(mHost, mPort);
                proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, inetSocketAddress);
            } catch (IllegalArgumentException e) {
            }
        }
        return proxy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mHost != null) {
            sb.append("[");
            sb.append(mHost);
            sb.append("] ");
            sb.append(mPort);
            if (mExclusionList != null) {
                sb.append(" xl=").append(mExclusionList);
            }
        } else {
            sb.append("[ProxyProperties.mHost == null]");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProxyProperties)) return false;
        ProxyProperties p = (ProxyProperties) o;
        if (mExclusionList != null && !mExclusionList.equals(p.getExclusionList())) return false;
        if (mHost != null && p.getHost() != null && mHost.equals(p.getHost()) == false) {
            return false;
        }
        if (mHost != null && p.mHost == null) return false;
        if (mHost == null && p.mHost != null) return false;
        return mPort == p.mPort;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public int hashCode() {
        return ((null == mHost) ? 0 : mHost.hashCode())
                + ((null == mExclusionList) ? 0 : mExclusionList.hashCode())
                + mPort;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (mHost != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mHost);
            dest.writeInt(mPort);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeString(mExclusionList);
        dest.writeStringArray(mParsedExclusionList);
    }
}
