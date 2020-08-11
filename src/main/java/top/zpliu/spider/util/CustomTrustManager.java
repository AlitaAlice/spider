package top.zpliu.spider.util;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class CustomTrustManager implements TrustManager, X509TrustManager {

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
        //don't check
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
        //don't check
    }
}