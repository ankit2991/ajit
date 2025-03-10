package com.android.mms.transaction;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Config;

import com.android.mms.MmsConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import timber.log.Timber;

public class HttpUtils {

    public static final int HTTP_POST_METHOD = 1;
    public static final int HTTP_GET_METHOD = 2;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int MMS_READ_BUFFER = 4096;


    private static final String HDR_VALUE_ACCEPT_LANGUAGE;
    private static final String HDR_KEY_ACCEPT = "Accept";
    private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HDR_VALUE_ACCEPT =
            "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    private static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";

    static {
        HDR_VALUE_ACCEPT_LANGUAGE = getCurrentAcceptLanguage(Locale.getDefault());
    }

    private HttpUtils() {
    }

    public static byte[] httpConnection(Context context, long token,
                                        String url, byte[] pdu, int method, boolean isProxySet,
                                        String proxyHost, int proxyPort) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }

        Timber.v("httpConnection: params list");
        Timber.v("\ttoken\t\t= " + token);
        Timber.v("\turl\t\t= " + url);
        Timber.v("\tmethod\t\t= " + ((method == HTTP_POST_METHOD) ? "POST"
                : ((method == HTTP_GET_METHOD) ? "GET" : "UNKNOWN")));
        Timber.v("\tisProxySet\t= " + isProxySet);
        Timber.v("\tproxyHost\t= " + proxyHost);
        Timber.v("\tproxyPort\t= " + proxyPort);

        AndroidHttpClient client = null;

        try {
            URI hostUrl = new URI(url);
            HttpHost target = new HttpHost(
                    hostUrl.getHost(), hostUrl.getPort(),
                    HttpHost.DEFAULT_SCHEME_NAME);

            client = createHttpClient(context);
            HttpRequest req = null;
            switch (method) {
                case HTTP_POST_METHOD:
                    ProgressCallbackEntity entity = new ProgressCallbackEntity(
                            context, token, pdu);
                    // Set request content type.
                    entity.setContentType("application/vnd.wap.mms-message");

                    HttpPost post = new HttpPost(url);
                    post.setEntity(entity);
                    req = post;
                    break;
                case HTTP_GET_METHOD:
                    req = new HttpGet(url);
                    break;
                default:
                    Timber.e("Unknown HTTP method: " + method
                            + ". Must be one of POST[" + HTTP_POST_METHOD
                            + "] or GET[" + HTTP_GET_METHOD + "].");
                    return null;
            }

            HttpParams params = client.getParams();
            if (isProxySet) {
                ConnRouteParams.setDefaultProxy(
                        params, new HttpHost(proxyHost, proxyPort));
            }
            req.setParams(params);

            req.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
            {
                String xWapProfileTagName = MmsConfig.getUaProfTagName();
                String xWapProfileUrl = MmsConfig.getUaProfUrl();
                if (xWapProfileUrl != null) {
                    Timber.d("[HttpUtils] httpConn: xWapProfUrl=" + xWapProfileUrl);
                    req.addHeader(xWapProfileTagName, xWapProfileUrl);
                }
            }

            String extraHttpParams = MmsConfig.getHttpParams();

            if (extraHttpParams != null) {
                String line1Number = ((TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE))
                        .getLine1Number();
                String line1Key = MmsConfig.getHttpParamsLine1Key();
                String[] paramList = extraHttpParams.split("\\|");

                for (String paramPair : paramList) {
                    String[] splitPair = paramPair.split(":", 2);

                    if (splitPair.length == 2) {
                        String name = splitPair[0].trim();
                        String value = splitPair[1].trim();

                        if (line1Key != null) {
                            value = value.replace(line1Key, line1Number);
                        }
                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                            req.addHeader(name, value);
                        }
                    }
                }
            }
            req.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);

            HttpResponse response = client.execute(target, req);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) { // HTTP 200 is success.
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            byte[] body = null;
            if (entity != null) {
                try {
                    if (entity.getContentLength() > 0) {
                        body = new byte[(int) entity.getContentLength()];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            dis.readFully(body);
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Timber.e("Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                    if (entity.isChunked()) {
                        Timber.v("httpConnection: transfer encoding is chunked");
                        int bytesTobeRead = MmsConfig.getMaxMessageSize();
                        byte[] tempBody = new byte[bytesTobeRead];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            int bytesRead = 0;
                            int offset = 0;
                            boolean readError = false;
                            do {
                                try {
                                    bytesRead = dis.read(tempBody, offset, bytesTobeRead);
                                } catch (IOException e) {
                                    readError = true;
                                    Timber.e("httpConnection: error reading input stream" + e.getMessage());
                                    break;
                                }
                                if (bytesRead > 0) {
                                    bytesTobeRead -= bytesRead;
                                    offset += bytesRead;
                                }
                            } while (bytesRead >= 0 && bytesTobeRead > 0);
                            if (bytesRead == -1 && offset > 0 && !readError) {
                                body = new byte[offset];
                                System.arraycopy(tempBody, 0, body, 0, offset);
                                Timber.v("httpConnection: Chunked response length [" + offset + "]");
                            } else {
                                Timber.e("httpConnection: Response entity too large or empty");
                            }
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Timber.e("Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }
            return body;
        } catch (URISyntaxException e) {
            handleHttpConnectionException(e, url);
        } catch (IllegalStateException e) {
            handleHttpConnectionException(e, url);
        } catch (IllegalArgumentException e) {
            handleHttpConnectionException(e, url);
        } catch (SocketException e) {
            handleHttpConnectionException(e, url);
        } catch (Exception e) {
            handleHttpConnectionException(e, url);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }

    private static void handleHttpConnectionException(Exception exception, String url)
            throws IOException {
        Timber.e("Url: " + url + "\n" + exception.getMessage());
        IOException e = new IOException(exception.getMessage(), exception);
        throw e;
    }

    private static AndroidHttpClient createHttpClient(Context context) {
        String userAgent = MmsConfig.getUserAgent();
        AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, context);
        HttpParams params = client.getParams();
        HttpProtocolParams.setContentCharset(params, "UTF-8");

        int soTimeout = MmsConfig.getHttpSocketTimeout();

        Timber.d("[HttpUtils] createHttpClient w/ socket timeout " + soTimeout + " ms, " + ", UA=" + userAgent);
        HttpConnectionParams.setSoTimeout(params, soTimeout);
        return client;
    }

    public static String getCurrentAcceptLanguage(Locale locale) {
        StringBuilder buffer = new StringBuilder();
        addLocaleToHttpAcceptLanguage(buffer, locale);

        if (!Locale.US.equals(locale)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(ACCEPT_LANG_FOR_US_LOCALE);
        }

        return buffer.toString();
    }

    private static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            // Hebrew
            return "he";
        } else if ("in".equals(langCode)) {
            // Indonesian
            return "id";
        } else if ("ji".equals(langCode)) {
            // Yiddish
            return "yi";
        }
        return langCode;
    }

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder,
                                                      Locale locale) {
        String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }
}
