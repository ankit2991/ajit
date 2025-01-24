package macro.hd.wallpapers.NetworkManager;

import java.nio.charset.Charset;

public interface WebServiceConstants {


    public static final String IMEI = "X-Device-Info";
    public static final String IMEI_HEADER = "X-Imei";


    public static final String SEPARATOR_QUERY_START = "?";
    public static final String SEPARATOR_QUERY_ASSIGNMENT = "=";
    public static final String SEPARATOR_QUERY_SERIES = "&";
    
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";

    public static final String COOKIE_HEADER = "Set-Cookie";
    
    public static final String CHARSET_DEFAULT = Charset.defaultCharset( ).name( );
    public static final int COOKIE_VERSION = 1;
    
    public static final int TIMEOUT_DEFAULT = 0;
    public static final int RETRIES_DEFAULT = 1;

}
