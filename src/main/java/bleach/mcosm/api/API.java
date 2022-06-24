package bleach.mcosm.api;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class API {

    public static final String API_LINK = "https://overpass-api.de/api/interpreter?data=";

    public static String getApiLink(double minLat, double minLon, double maxLat, double maxLon, boolean encoded) {
        if (minLat > maxLat) {
            double tempLat = minLat;
            minLat = maxLat;
            maxLat = tempLat;
        }

        if (minLon > maxLon) {
            double tempLon = minLon;
            minLon = maxLon;
            maxLon = tempLon;
        }

        String coords = minLat + "," + minLon + "," + maxLat + "," + maxLon;
        String boundCoords = (minLat - 0.0002) + "," + (minLon - 0.0002) + "," + (maxLat + 0.0002) + "," + (maxLon + 0.0002);
        String link = "[out:json];(way(" + coords + ");node[natural=tree](" + coords + "););out geom(" + boundCoords + ");";

        try {
            return API_LINK + (encoded ? URLEncoder.encode(link, "utf-8") : link);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String call(URL url) throws IOException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/75.0.1");
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);

        return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
    }
}
