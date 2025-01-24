package macro.hd.wallpapers.NetworkManager;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

class WebServiceErrorHelper {
    public static WebServiceError getError( Throwable cause ) {
        int errorCode = WebServiceErrorCodes.UNKNOWN;
        String description = "";

        if(cause !=null){
         description =  cause.toString( );
        }
        if(description!=null){

            if ( cause instanceof MalformedURLException) {
                errorCode = WebServiceErrorCodes.INVALID_URL;
            } else if ( cause instanceof UnknownHostException) {
                errorCode = WebServiceErrorCodes.UNKNOWN_HOST;
            } else if ( cause instanceof SocketTimeoutException) {
                errorCode = WebServiceErrorCodes.TIMED_OUT;
            } else if ( cause instanceof HttpStatusException ) {
                HttpStatusException ex = (HttpStatusException) cause;
                errorCode = WebServiceErrorCodes.BAD_HTTP_STATUS;

                description = ex.getMessage( );
            }
        }

        
        return new WebServiceError( errorCode, description );
    }
}
