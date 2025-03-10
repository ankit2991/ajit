package com.android.mms.service_alt;

import android.content.Context;
import android.text.TextUtils;

import com.android.mms.service_alt.exception.MmsHttpException;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Internal;
import com.squareup.okhttp.internal.huc.HttpURLConnectionImpl;
import com.squareup.okhttp.internal.huc.HttpsURLConnectionImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class MmsHttpClient {

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String HEADER_USER_AGENT = "User-Agent";

    private static final String HEADER_VALUE_ACCEPT =
            "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    private static final String HEADER_VALUE_CONTENT_TYPE_WITH_CHARSET =
            "application/vnd.wap.mms-message; charset=utf-8";
    private static final String HEADER_VALUE_CONTENT_TYPE_WITHOUT_CHARSET =
            "application/vnd.wap.mms-message";
    private static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";
    private static final Pattern MACRO_P = Pattern.compile("##(\\S+)##");
    private final Context mContext;
    private final SocketFactory mSocketFactory;
    private final MmsNetworkManager mHostResolver;
    private final ConnectionPool mConnectionPool;

    public MmsHttpClient(Context context, SocketFactory socketFactory, MmsNetworkManager hostResolver,
                         ConnectionPool connectionPool) {
        mContext = context;
        mSocketFactory = socketFactory;
        mHostResolver = hostResolver;
        mConnectionPool = connectionPool;
    }

    private static void logHttpHeaders(Map<String, List<String>> headers) {
        final StringBuilder sb = new StringBuilder();
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final String key = entry.getKey();
                final List<String> values = entry.getValue();
                if (values != null) {
                    for (String value : values) {
                        sb.append(key).append('=').append(value).append('\n');
                    }
                }
            }
            Timber.v("HTTP: headers\n" + sb);
        }
    }

    private static void checkMethod(String method) throws MmsHttpException {
        if (!METHOD_GET.equals(method) && !METHOD_POST.equals(method)) {
            throw new MmsHttpException(0/*statusCode*/, "Invalid method " + method);
        }
    }

    public static String getCurrentAcceptLanguage(Locale locale) {
        final StringBuilder buffer = new StringBuilder();
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

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        final String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            final String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }

    private static String resolveMacro(Context context, String value,
                                       MmsConfig.Overridden mmsConfig) {
        if (TextUtils.isEmpty(value)) {
            return value;
        }
        final Matcher matcher = MACRO_P.matcher(value);
        int nextStart = 0;
        StringBuilder replaced = null;
        while (matcher.find()) {
            if (replaced == null) {
                replaced = new StringBuilder();
            }
            final int matchedStart = matcher.start();
            if (matchedStart > nextStart) {
                replaced.append(value, nextStart, matchedStart);
            }
            final String macro = matcher.group(1);
            final String macroValue = mmsConfig.getHttpParamMacro(context, macro);
            if (macroValue != null) {
                replaced.append(macroValue);
            } else {
                Timber.w("HTTP: invalid macro " + macro);
            }
            nextStart = matcher.end();
        }
        if (replaced != null && nextStart < value.length()) {
            replaced.append(value.substring(nextStart));
        }
        return replaced == null ? value : replaced.toString();
    }

    public byte[] execute(String urlString, byte[] pdu, String method, boolean isProxySet,
                          String proxyHost, int proxyPort, MmsConfig.Overridden mmsConfig)
            throws MmsHttpException {
        Timber.d("HTTP: " + method + " " + urlString
                + (isProxySet ? (", proxy=" + proxyHost + ":" + proxyPort) : "")
                + ", PDU size=" + (pdu != null ? pdu.length : 0));
        checkMethod(method);
        HttpURLConnection connection = null;
        try {
            Proxy proxy = null;
            if (isProxySet) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            }
            final URL url = new URL(urlString);
            connection = openConnection(url, proxy);
            connection.setDoInput(true);
            connection.setConnectTimeout(mmsConfig.getHttpSocketTimeout());
            connection.setRequestProperty(HEADER_ACCEPT, HEADER_VALUE_ACCEPT);
            connection.setRequestProperty(
                    HEADER_ACCEPT_LANGUAGE, getCurrentAcceptLanguage(Locale.getDefault()));
            final String userAgent = mmsConfig.getUserAgent();
            Timber.i("HTTP: User-Agent=" + userAgent);
            connection.setRequestProperty(HEADER_USER_AGENT, userAgent);
            final String uaProfUrlTagName = mmsConfig.getUaProfTagName();
            final String uaProfUrl = mmsConfig.getUaProfUrl();
            if (uaProfUrl != null) {
                Timber.i("HTTP: UaProfUrl=" + uaProfUrl);
                connection.setRequestProperty(uaProfUrlTagName, uaProfUrl);
            }
            addExtraHeaders(connection, mmsConfig);
            if (METHOD_POST.equals(method)) {
                if (pdu == null || pdu.length < 1) {
                    Timber.e("HTTP: empty pdu");
                    throw new MmsHttpException(0/*statusCode*/, "Sending empty PDU");
                }
                connection.setDoOutput(true);
                connection.setRequestMethod(METHOD_POST);
                if (mmsConfig.getSupportHttpCharsetHeader()) {
                    connection.setRequestProperty(HEADER_CONTENT_TYPE,
                            HEADER_VALUE_CONTENT_TYPE_WITH_CHARSET);
                } else {
                    connection.setRequestProperty(HEADER_CONTENT_TYPE,
                            HEADER_VALUE_CONTENT_TYPE_WITHOUT_CHARSET);
                }
                logHttpHeaders(connection.getRequestProperties());
                connection.setFixedLengthStreamingMode(pdu.length);
                final OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                out.write(pdu);
                out.flush();
                out.close();
            } else if (METHOD_GET.equals(method)) {
                logHttpHeaders(connection.getRequestProperties());
                connection.setRequestMethod(METHOD_GET);
            }
            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            Timber.d("HTTP: " + responseCode + " " + responseMessage);
            logHttpHeaders(connection.getHeaderFields());
            if (responseCode / 100 != 2) {
                throw new MmsHttpException(responseCode, responseMessage);
            }
            final InputStream in = new BufferedInputStream(connection.getInputStream());
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            final byte[] buf = new byte[4096];
            int count = 0;
            while ((count = in.read(buf)) > 0) {
                byteOut.write(buf, 0, count);
            }
            in.close();
            final byte[] responseBody = byteOut.toByteArray();
            Timber.d("HTTP: response size="
                    + (responseBody != null ? responseBody.length : 0));
            return responseBody;
        } catch (MalformedURLException e) {
            Timber.e(e, "HTTP: invalid URL " + urlString);
            throw new MmsHttpException(0/*statusCode*/, "Invalid URL " + urlString, e);
        } catch (ProtocolException e) {
            Timber.e(e, "HTTP: invalid URL protocol " + urlString);
            throw new MmsHttpException(0/*statusCode*/, "Invalid URL protocol " + urlString, e);
        } catch (IOException e) {
            Timber.e(e, "HTTP: IO failure");
            throw new MmsHttpException(0/*statusCode*/, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(URL url, final Proxy proxy) throws MalformedURLException {
        final String protocol = url.getProtocol();
        OkHttpClient okHttpClient;
        if (protocol.equals("http")) {
            okHttpClient = new OkHttpClient();
            okHttpClient.setFollowRedirects(false);
            okHttpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
            okHttpClient.setProxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    if (proxy != null) {
                        return Arrays.asList(proxy);
                    } else {
                        return new ArrayList<Proxy>();
                    }
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, IOException failure) {

                }
            });
            okHttpClient.setAuthenticator(new com.squareup.okhttp.Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    return null;
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    return null;
                }
            });
            okHttpClient.setConnectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT));
            okHttpClient.setConnectionPool(new ConnectionPool(3, 60000));
            okHttpClient.setSocketFactory(SocketFactory.getDefault());
            Internal.instance.setNetwork(okHttpClient, mHostResolver);

            if (proxy != null) {
                okHttpClient.setProxy(proxy);
            }

            return new HttpURLConnectionImpl(url, okHttpClient);
        } else if (protocol.equals("https")) {
            okHttpClient = new OkHttpClient();
            okHttpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
            HostnameVerifier verifier = HttpsURLConnection.getDefaultHostnameVerifier();
            okHttpClient.setHostnameVerifier(verifier);
            okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
            okHttpClient.setProxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return Arrays.asList(proxy);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress address, IOException failure) {

                }
            });
            okHttpClient.setAuthenticator(new com.squareup.okhttp.Authenticator() {
                @Override
                public Request authenticate(Proxy proxy, Response response) throws IOException {
                    return null;
                }

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                    return null;
                }
            });
            okHttpClient.setConnectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT));
            okHttpClient.setConnectionPool(new ConnectionPool(3, 60000));
            Internal.instance.setNetwork(okHttpClient, mHostResolver);

            return new HttpsURLConnectionImpl(url, okHttpClient);
        } else {
            throw new MalformedURLException("Invalid URL or unrecognized protocol " + protocol);
        }
    }

    private void addExtraHeaders(HttpURLConnection connection, MmsConfig.Overridden mmsConfig) {
        final String extraHttpParams = mmsConfig.getHttpParams();
        if (!TextUtils.isEmpty(extraHttpParams)) {
            String[] paramList = extraHttpParams.split("\\|");
            for (String paramPair : paramList) {
                String[] splitPair = paramPair.split(":", 2);
                if (splitPair.length == 2) {
                    final String name = splitPair[0].trim();
                    final String value = resolveMacro(mContext, splitPair[1].trim(), mmsConfig);
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                        connection.setRequestProperty(name, value);
                    }
                }
            }
        }
    }
}
