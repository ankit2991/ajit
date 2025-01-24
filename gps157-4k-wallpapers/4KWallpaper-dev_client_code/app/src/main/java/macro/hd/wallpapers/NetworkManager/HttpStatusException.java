package macro.hd.wallpapers.NetworkManager;

public class HttpStatusException extends Exception {
    private static final long serialVersionUID = -6041714650963785982L;
    
    private int mStatusCode;
    private boolean mRetry;
    
    public HttpStatusException(int statusCode, String statusMessage ) {
        this( statusCode, statusMessage, true );
    }
    
    public HttpStatusException(int statusCode, String statusMessage, boolean retry ) {
        super( statusMessage );
        this.mStatusCode = statusCode;
        this.mRetry = retry;
    }
    
    public int getStatusCode( ) {
        return mStatusCode;
    }
    
    public boolean canRetry( ) {
        return mRetry;
    }
}
