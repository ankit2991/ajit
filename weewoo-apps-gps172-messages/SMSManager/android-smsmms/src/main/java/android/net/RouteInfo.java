package android.net;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import timber.log.Timber;

public class RouteInfo implements Parcelable {
    public static final Creator<RouteInfo> CREATOR =
            new Creator<RouteInfo>() {
                public RouteInfo createFromParcel(Parcel in) {
                    InetAddress destAddr = null;
                    int prefix = 0;
                    InetAddress gateway = null;

                    if (in.readByte() == 1) {
                        byte[] addr = in.createByteArray();
                        prefix = in.readInt();

                        try {
                            destAddr = InetAddress.getByAddress(addr);
                        } catch (UnknownHostException e) {
                        }
                    }

                    if (in.readByte() == 1) {
                        byte[] addr = in.createByteArray();

                        try {
                            gateway = InetAddress.getByAddress(addr);
                        } catch (UnknownHostException e) {
                        }
                    }

                    LinkAddress dest = null;

                    if (destAddr != null) {
                        dest = new LinkAddress(destAddr, prefix);
                    }

                    return new RouteInfo(dest, gateway);
                }

                public RouteInfo[] newArray(int size) {
                    return new RouteInfo[size];
                }
            };
    private final LinkAddress mDestination;
    private final InetAddress mGateway;
    private final boolean mIsDefault;
    private final boolean mIsHost;

    public RouteInfo(LinkAddress destination, InetAddress gateway) {
        if (destination == null) {
            if (gateway != null) {
                if (gateway instanceof Inet4Address) {
                    try {
                        destination = new LinkAddress(Inet4Address.getLocalHost(), 0);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        Timber.e(e, "exception thrown");
                    }
                } else {
                    try {
                        destination = new LinkAddress(Inet6Address.getLocalHost(), 0);
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        Timber.e(e, "exception thrown");
                    }
                }
            } else {
                throw new RuntimeException("Invalid arguments passed in.");
            }
        }
        if (gateway == null) {
            if (destination.getAddress() instanceof Inet4Address) {
                try {
                    gateway = Inet4Address.getLocalHost();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    Timber.e(e, "exception thrown");
                }
            } else {
                try {
                    gateway = Inet6Address.getLocalHost();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    Timber.e(e, "exception thrown");
                }
            }
        }
        mDestination = new LinkAddress(NetworkUtilsHelper.getNetworkPart(destination.getAddress(),
                destination.getNetworkPrefixLength()), destination.getNetworkPrefixLength());
        mGateway = gateway;
        mIsDefault = isDefault();
        mIsHost = isHost();
    }

    public RouteInfo(InetAddress gateway) {
        this(null, gateway);
    }

    public static RouteInfo makeHostRoute(InetAddress host) {
        return makeHostRoute(host, null);
    }

    public static RouteInfo makeHostRoute(InetAddress host, InetAddress gateway) {
        if (host == null) return null;

        if (host instanceof Inet4Address) {
            return new RouteInfo(new LinkAddress(host, 32), gateway);
        } else {
            return new RouteInfo(new LinkAddress(host, 128), gateway);
        }
    }

    public static RouteInfo selectBestRoute(Collection<RouteInfo> routes, InetAddress dest) {
        if ((routes == null) || (dest == null)) return null;

        RouteInfo bestRoute = null;
        for (RouteInfo route : routes) {
            if (NetworkUtilsHelper.addressTypeMatches(route.mDestination.getAddress(), dest)) {
                if ((bestRoute != null) &&
                        (bestRoute.mDestination.getNetworkPrefixLength() >=
                                route.mDestination.getNetworkPrefixLength())) {
                    continue;
                }
                if (route.matches(dest)) bestRoute = route;
            }
        }
        return bestRoute;
    }

    private boolean isHost() {
        try {
            return (mGateway.equals(Inet4Address.getLocalHost()) || mGateway.equals(Inet6Address.getLocalHost()));
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            return false;
        }
    }

    private boolean isDefault() {
        boolean val = false;
        if (mGateway != null) {
            if (mGateway instanceof Inet4Address) {
                val = (mDestination == null || mDestination.getNetworkPrefixLength() == 0);
            } else {
                val = (mDestination == null || mDestination.getNetworkPrefixLength() == 0);
            }
        }
        return val;
    }

    public LinkAddress getDestination() {
        return mDestination;
    }

    public InetAddress getGateway() {
        return mGateway;
    }

    public boolean isDefaultRoute() {
        return mIsDefault;
    }

    public boolean isHostRoute() {
        return mIsHost;
    }

    public String toString() {
        String val = "";
        if (mDestination != null) val = mDestination.toString();
        if (mGateway != null) val += " -> " + mGateway.getHostAddress();
        return val;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (mDestination == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeByteArray(mDestination.getAddress().getAddress());
            dest.writeInt(mDestination.getNetworkPrefixLength());
        }

        if (mGateway == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeByteArray(mGateway.getAddress());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof RouteInfo)) return false;

        RouteInfo target = (RouteInfo) obj;

        boolean sameDestination = (mDestination == null) ?
                target.getDestination() == null
                : mDestination.equals(target.getDestination());

        boolean sameAddress = (mGateway == null) ?
                target.getGateway() == null
                : mGateway.equals(target.getGateway());

        return sameDestination && sameAddress
                && mIsDefault == target.mIsDefault;
    }

    @Override
    public int hashCode() {
        return (mDestination == null ? 0 : mDestination.hashCode())
                + (mGateway == null ? 0 : mGateway.hashCode())
                + (mIsDefault ? 3 : 7);
    }

    private boolean matches(InetAddress destination) {
        if (destination == null) return false;

        if (isDefault()) return true;

        InetAddress dstNet = NetworkUtilsHelper.getNetworkPart(destination,
                mDestination.getNetworkPrefixLength());

        return mDestination.getAddress().equals(dstNet);
    }
}
