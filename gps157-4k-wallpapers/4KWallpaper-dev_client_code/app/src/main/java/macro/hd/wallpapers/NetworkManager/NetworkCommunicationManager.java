package macro.hd.wallpapers.NetworkManager;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.IModel;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by hungama1 on 26/4/16.
 */
public abstract class NetworkCommunicationManager<T extends IModel> implements IWebService<T>{

    private Context context;

    private String apiLink;

    IWebServiceCallback callback_new;

    private boolean isNeedToHeaderSet;
    public static final int RESPONSE_CONTENT_NOT_MODIFIED_304 = 304;
    public static final int RESPONSE_SERVER_ERROR_500 = 500;
    public static final int RESPONSE_SUCCESS_200 = 200;//
    public static final int RESPONSE_CONTENT_NOT_AVAILABLE_REQUEST_404 = 404;
    public static final int RESPONSE_UNAUTHORISED_400 = 403;
    public static final int RESPONSE_FORBIDDEN_403 = 403;
    public static final int RESPONSE_NO_CONTENT_204 = 204;

    int operationCode;
    SettingStore settingStore;


	private boolean isUserIdPass;
    FormBody.Builder builder  ;
    public NetworkCommunicationManager(Context context, String apiLink, IWebServiceCallback callback) {
        this.context = context;
        this.apiLink = apiLink;
        this.callback_new = callback;
        builder= new FormBody.Builder();
//        addAppIdandSessionIdQueryFields(apiLink);
        settingStore= SettingStore.getInstance(context);
        addRequestParameters("c_code",settingStore.getLocation());
        addRequestParameters("user_id",settingStore.getUserID());
        addRequestParameters("app_version", CommonFunctions.appVerstion(context));
    }



    public void setOperationCode(int operationCode){
        this.operationCode=operationCode;
    }

    private void addAppIdandSessionIdQueryFields(String key,String value) {

        StringBuilder builder = new StringBuilder();
        builder.append(apiLink);
        if (!TextUtils.isEmpty(apiLink) && !apiLink.contains(WebServiceConstants.SEPARATOR_QUERY_START)) {
            builder.append(WebServiceConstants.SEPARATOR_QUERY_START);
        } else {
            builder.append(WebServiceConstants.SEPARATOR_QUERY_SERIES);
        }


        builder.append(key);
        builder.append(WebServiceConstants.SEPARATOR_QUERY_ASSIGNMENT);
        builder.append(value);

        apiLink = builder.toString();

    }

    private HashMap<String, String> testHashMap = new HashMap<>();

    public void addRequestParameters(String key,String value){
        Logger.e("Post","key:"+key+" value:"+value);
        builder.add(key,value);
//        addAppIdandSessionIdQueryFields(key,value);

        if (Logger.isDebuggable) {
            testHashMap.put(key, value);
        }
    }

    T data = null;
    ThreadPoolManager mPool;
    int responseCode;
    String response;
    public void execute() {
        callback_new.onStartLoading();
        mPool = ThreadPoolManager.getInstance();//mPool.submit(new Callable<Object>())
        mPool.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    Logger.e("apiLink",apiLink);
                    OkHttpClient.Builder client = new OkHttpClient.Builder();
                    client.connectTimeout(20, TimeUnit.SECONDS);
                    client.readTimeout(20, TimeUnit.SECONDS);
                    client.writeTimeout(20, TimeUnit.SECONDS);
                    Request.Builder requestBuilder = new Request.Builder();

                        Calendar calendar = Calendar.getInstance();
                        long timeInMillis = calendar.getTimeInMillis();

//                        String hashKey = "kqbKtqCg0hXKckem7xDEMlXvhoI=";//CommonFunctions.getHashKey(context);
                        String hashKey = CommonFunctions.getHashKey(context);

                        hashKey = CommonFunctions.md5(timeInMillis + CommonFunctions.getHardwareId(context) + hashKey);
                        addRequestParameters("app_secret_key", hashKey);
                        addRequestParameters("time_in_millis", "" + timeInMillis);
                        addRequestParameters("user_device_serial_no", "" + CommonFunctions.getHardwareId(context));

                        if (Logger.isDebuggable) {
                            printAPIlog();
                        }

                        RequestBody formBody = builder.build();
                        requestBuilder.post(formBody);


                    try {
                        client.cache(new Cache(context.getCacheDir(), 20 * 1024 * 1024)); // 10 MB
                        client.addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request request = chain.request();
                                if (CommonFunctions.isNetworkAvailable(context)) {
                                    request = request.newBuilder().header("Cache-Control", "public, max-age=" + 600).build();
                                } else {
                                    request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
                                }
                                return chain.proceed(request);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    URL url = new URL(apiLink);
                    requestBuilder.url(url);
                    Response responseOk = client.build().newCall(requestBuilder.build()).execute();


//                    Headers responseHeaders = responseOk.headers();
//                    for (int i = 0; i < responseHeaders.size(); i++) {
//                        Log.e("Headers now ", responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }

                    responseCode = responseOk.code();
                    response = responseOk.body().string();
                    Logger.e("response",""+response);
                    Logger.e("responseCode",""+responseCode);

                    if (responseCode == RESPONSE_SUCCESS_200) {
//                        EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"Success","200");
                        data = doRetrieveData();
                        handler.post(runSuccess);

                    }else if(responseCode == RESPONSE_CONTENT_NOT_AVAILABLE_REQUEST_404 || responseCode == RESPONSE_UNAUTHORISED_400){
                        EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"UnAuthorize",""+responseCode);
                        String errorMessage = getErrorMessage(response);
                        if(!TextUtils.isEmpty(errorMessage))
                            sendErrorMessageToHanlderForCast(ErrorType.NO_CONNECTIVITY, errorMessage, null);
                        else
                            sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_server_error), null);
                    }else  if (!CommonFunctions.isNetworkAvailable(context)) {
                        sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_no_network), null);
                    }else{
                        sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_server_error), null);
                    }
                } catch (MalformedURLException exception) {
                    // Bad Url.
                    EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"MalformedURLException",""+exception.toString());
                    exception.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
                            context.getString(R.string.error_msg_invalid_url), exception);
                } catch (SocketTimeoutException exception) {
                    EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"SocketTimeoutException",""+exception.toString());
                    exception.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_socket_timeout), exception);
                } catch (UnknownHostException e) {
                    String apiname="";
                    try{
                        apiname=apiLink.substring(apiLink.lastIndexOf("/")+1);
//                        apiname+=" "+Common.getDeviceName();
                    }catch (Exception exx){ };
                    Logger.e("apiname",""+apiname);

                    EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"UnknownHostException",""+e.toString()+" "+apiname);
                    e.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_no_network), e);
                } catch (UnknownServiceException e) {
                    e.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_no_network), e);
                } catch (IOException exception) {
                    // File cannot be accessed or is corrupted..
                    EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"IOException",""+exception.toString());
                    exception.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.INTERNAL_SERVER_APPLICATION_ERROR, context.getString(R.string.error_msg_server_error), exception);
                } catch (Exception e) {
                    EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"Exception",""+e.toString());
                    e.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_no_network), e);
                } catch (Error e) {
                    EventManager.sendEvent(EventManager.LBL_COMMUNICATION,"Error",""+e.toString());
                    e.printStackTrace();
                    sendErrorMessageToHanlder(ErrorType.NO_CONNECTIVITY, context.getString(R.string.error_msg_server_error), null);
                    //handler.post(runFail);
                }
            }
        });
    }

    private void printAPIlog() {
        if (!TextUtils.isEmpty(apiLink) && testHashMap != null && testHashMap.size() > 0) {
            String parameters = "";
            for (String key : testHashMap.keySet()) {
                if (!TextUtils.isEmpty(parameters)) {
                    parameters = parameters + "&";
                }
                parameters = parameters + key + "=" + testHashMap.get(key);
            }
            System.out.println("API Call: " + apiLink + "?" + parameters);
        }
    }

    WebServiceError webServiceError;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runSuccess = new Runnable() {
        @Override
        public void run() {
            callback_new.onSuccess( data,operationCode );
        }
    };

    private void sendErrorMessageToHanlder(ErrorType errorType, String message, Exception e){
        String errorMsg;
        /*if(e == null){
            errorMsg = message;
        }else{
            errorMsg = e.toString();
        }*/


        errorMsg = message;
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("ErrorMsg", errorMsg);
        msg.setData(b);
         webServiceError= WebServiceErrorHelper.getError(e);
        webServiceError.setmOperationCode(operationCode);
        handler_err.sendMessage(msg);
    }

    private void sendErrorMessageToHanlderForCast(ErrorType errorType, String message, Exception e){
        String errorMsg;
        errorMsg = message;
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("ErrorMsg", errorMsg);
        msg.setData(b);
        webServiceError= WebServiceErrorHelper.getError(e);
        webServiceError.setmOperationCode(operationCode);
        handler_err.sendMessage(msg);
    }

    private String getErrorMessage(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("msg");
        }catch (Exception e){
        }
        return null;
    }


    private void enableHttpCaching(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                File httpCacheDir = new File(context.getApplicationContext()
                        .getCacheDir(), "http");
                long httpCacheSize = 20 * 1024 * 1024; // 10 MiB
                HttpResponseCache.install(httpCacheDir, httpCacheSize);
            } catch (IOException e) {
                Logger.i("", "OVER ICS: HTTP response cache failed:" + e);
            }
        }
    }

    /**
     * Parse data returned from the server. Invokes the {@link #onResponseReceived(String)}
     * callback.
     */
    protected T doRetrieveData() throws IOException, Exception {
        return (T) onResponseReceived(response);
    }

    // Handle Message in handleMessage method of your controller
    Handler  handler_err = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String errorMsg = b.getString("ErrorMsg");
            webServiceError.setDescription(errorMsg);
            webServiceError.setmOperationCode(operationCode);
            webServiceError.setErrorCode(responseCode);
            Logger.i("URL","URL:"+apiLink);
            callback_new.onError(webServiceError);
        }
    };

    /*Runnable runFail = new Runnable() {
        @Override
        public void run() {
            callback.onFail(v, isParentAPI);
        }
    };*/



    public boolean isNeedToHeaderSet() {
        return isNeedToHeaderSet;
    }

    public void setNeedToHeaderSet(boolean needToHeaderSet) {
        isNeedToHeaderSet = needToHeaderSet;
    }

	public boolean isUserIdPass() {
		return isUserIdPass;
	}

	public void setUserIdPass(boolean userIdPass) {
		isUserIdPass = userIdPass;
	}


	//-----------------------Listner--------------//
    public interface CommunicationListnerNew< T extends IModel>  {

        void onSuccess(T response, int operationCode);
        void onStartLoading();
        void onFail(WebServiceError errorMsg);
    }


    public enum ErrorType implements Serializable {

        /**
         * Error which indicates that there is no connection to Internet or has
         * been timeout.
         */
        NO_CONNECTIVITY,

        /**
         * Error which indicates that the given parameters to the request's
         * service are invalid.
         */
        INVALID_REQUEST_PARAMETERS,

        /**
         * Error which indicates that the given token given to request is
         * invalid.
         */
        EXPIRED_REQUEST_TOKEN,

        /**
         * Error which indicates that the retrieved server response is invalid
         * OR the communication protocols among the client / server was broken.
         */
        INTERNAL_SERVER_APPLICATION_ERROR,

        /**
         * Error which indicates that the whole operation was cancelled.
         */
        OPERATION_CANCELLED,

        CONTENT_NOT_AVAILABLE;

    }


    public static X509TrustManager provideX509TrustManager() {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            TrustManager[] trustManagers = factory.getTrustManagers();
            return (X509TrustManager) trustManagers[0];
        } catch (NoSuchAlgorithmException | KeyStoreException exception) {
            Log.e("", "not trust manager available", exception);
        }

        return null;
    }


    public static SSLSocketFactory provideSSLSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException exception) {
            Log.e("", "not tls ssl socket factory available", exception);
        }

        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            X509TrustManager manager=provideX509TrustManager();

            OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
//            okHttpClient.SslSocketFactory(sslSocketFactory);
            okHttpClient.sslSocketFactory(provideSSLSocketFactory(manager),manager);
            okHttpClient.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String getResponse(final Context context, String apiLink){

        String response = "";
        int responseCode;
        try {
        OkHttpClient.Builder client = getUnsafeOkHttpClient();//new OkHttpClient();
        client.connectTimeout(15000, TimeUnit.MILLISECONDS);
        client.readTimeout(15000, TimeUnit.MILLISECONDS);
        Request.Builder requestBuilder = new Request.Builder();
        client.cache(new Cache(context.getCacheDir(), 20 * 1024 * 1024L));
            client.addInterceptor(new Interceptor() {
                @Override public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    if (CommonFunctions.isNetworkAvailable(context)) {
                        request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                    } else {
                        request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
                    }
                    return chain.proceed(request);
                }
            });
        URL url = new URL(apiLink);
        requestBuilder.url(url);
        Response responseOk = client.build().newCall(requestBuilder.build()).execute();
        responseCode = responseOk.code();
        response = responseOk.body().string();
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public String getUrlParameters(String uri)
            throws UnsupportedEncodingException {
        String params="";
        for (String param : uri.split("&")) {
            String pair[] = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = "";
            if (pair.length > 1) {
                value = URLDecoder.decode(pair[1], "UTF-8");
            }
            params+=new String(value);
        }
        return params;
    }

}
