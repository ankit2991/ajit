package macro.hd.wallpapers.NetworkManager;

public class WebServiceError {
    private int mErrorCode;

    private String mDescription;
    private int mOperationCode;

    public WebServiceError( ) {
        this( WebServiceErrorCodes.UNKNOWN,  null );
    }
    
    public WebServiceError(int errorCode ) {
        this( errorCode,  null );
    }

    public WebServiceError(int errorCode, String description ) {
        super( );
        this.mErrorCode = errorCode;

        this.mDescription = description;
    }
    
    public int getErrorCode( ) {
        return mErrorCode;
    }
    

    public String getDescription( ) {
        return mDescription;
    }
    
    public void setErrorCode( int errorCode ) {
        this.mErrorCode = errorCode;
    }
    

    
    public void setDescription( String description ) {
        this.mDescription = description;
    }
    
    @Override
    public String toString( ) {
        StringBuilder builder = new StringBuilder( );
        builder.append( "WebServiceError { mErrorCode=" );
        builder.append( mErrorCode );
               builder.append( " mDescription=" );
        builder.append( mDescription );
        builder.append( " mOperationCode=" );
        builder.append( mOperationCode );
        builder.append( " }" );
        return builder.toString( );
    }

    public int getmOperationCode() {
        return mOperationCode;
    }

    public void setmOperationCode(int mOperationCode) {
        this.mOperationCode = mOperationCode;
    }
}
