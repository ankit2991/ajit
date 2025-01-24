package macro.hd.wallpapers.NetworkManager;

import macro.hd.wallpapers.Model.IModel;

import org.json.JSONException;

import java.io.InputStream;
import java.net.HttpURLConnection;

public interface IWebService< T extends IModel> extends MutablePriorityElement {


    /**
     * The path component of the URL for this web service.
     */
    public String getPath();
    
    /**
     * The query component of the URL for this web service.
     */
    public String getQuery();
    
    /**
     * The request method for this web service. Typically GET or POST.
     * 
     * @return One of the REQUEST_METHOD_* values defined in {@link WebServiceConstants}.
     */
    public String getRequestMethod();

    /**
     * Add a cookie to the web service request.
     */
    public void addCookie(String name, String value);
    
    /**
     * Remove a cookie from cookie manager.
     */
    public void removeCookie(String name, String value);

    /**
     * Removes all cookies.
     */
    public void removeAllCookies();
    
    /**
     * Sets the request header field with given name to use the given value.
     */
    public void setRequestHeaderField(String name, String value);
    
    /**
     * The header value for given response field after successfully establishing a connection.
     */
    public String getResponseHeaderField(String name);
    
    /**
     * Callback method which gets invoked before a connection is established.<br />
     * Override this method to add cookies or request header fields.
     */
    public void onPreRequest() throws Exception;
    
    /**
     * Callback method which gets invoked when connection is established.<br />
     * Override this method to check the given status code or any response header fields.<br />
     * 
     * @param The
     *            HTTP status code returned by the server. One of the HTTP_* values defined in
     *            {@link HttpURLConnection} class.
     * @param The
     *            status message returned by the server.
     * @throws HttpStatusException
     *             The connection will be retried if {@link HttpStatusException#canRetry()} returns
     *             true.
     */
    public void onCheckStatusCode(int statusCode, String statusMessage)
            throws HttpStatusException;
    
    /**
     * Callback method when connection with server has been established.<br />
     * Override this method to read any response readers returned by server.
     */
    public void onConnected() throws Exception;
    
    /**
     * The data to send as part of the request message body. Typically used for POST requests.
     * 
     * @return A {@link InputStream} which encapsulates the data.
     */
    public InputStream onDataRequested();
    
    /**
     * Override this method to parse the response message body.
     */
    public IModel onResponseReceived(String in) throws JSONException, Exception;
    
    /**
     * Override this method to convert any exception during web service call to a
     * {@link WebServiceError} object.
     */
    public WebServiceError onErrorReceived(Throwable cause);
}
