package com.atul;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * A java client which implements multipart form submit over https and authentication implemented with only 
 * jdk classes and no external dependencies.
 * 
 * @author Atul Soman, atulsm@gmail.com
 *
 */
public class MultipartPure {
	
	private static final String ip = "192.168.1.1";
	private static final String port = "443";
	private static final String userName = "admin";
	private static final String password = "admin";
	
	private static String body = "{\"key1\":\"val1\", \"key2\":\"val2\"}";
	private static String subdata1 = "@@ -2,3 +2,4 @@\r\n";
	private static String subdata2 = "<data>subdata2</data>";
	
	public static void main(String[] args) throws Exception{		
		String url = "https://" + ip + ":" + port + "/dataupload";
		String token = "Basic "+ Base64.getEncoder().encodeToString((userName+":"+password).getBytes());
		
		MultipartBuilder multipart = new MultipartBuilder(url,token);		
        multipart.addFormField("entity", "main", "application/json",body);
        multipart.addFormField("attachment", "subdata1", "application/octet-stream",subdata1);
        multipart.addFormField("attachment", "subdata2", "application/octet-stream",subdata2);        
        List<String> response = multipart.finish();         
         
        for (String line : response) {
            System.out.println(line);
        }		
		
	}
		
    static {
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {					
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}				
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						// Do nothing. Just allow them all.				
					}				
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						// Do nothing. Just allow them all.						
					}
				}
        };

        HostnameVerifier trustAllHostnames = new HostnameVerifier() {			
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private static class MultipartBuilder {
        private final String boundary;
        private static final String NEW_LINE = "\r\n";
        private HttpURLConnection conn;
        private String charset = "UTF-8";
        private OutputStream outputStream;
        private PrintWriter writer;
     
        public MultipartBuilder(String requestURL, String token) throws IOException {             
            // creates a unique boundary based on time stamp
            boundary = "---" + System.currentTimeMillis() + "---";
             
            URL url = new URL(requestURL);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);     
            conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("Authorization", "Basic "+token);
            
            outputStream = conn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),true);
        }
     
        public void addFormField(String name, String fileName, String contentType, String value) {
            writer.append("--" + boundary).append(NEW_LINE);
            writer.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"")
                    .append(NEW_LINE);
            writer.append("Content-Type: ").append(contentType).append(NEW_LINE);
            writer.append(NEW_LINE);
            writer.append(value).append(NEW_LINE);
            writer.flush();
        }
              
        public List<String> finish() throws IOException {
            List<String> response = new ArrayList<String>();
     
            writer.append(NEW_LINE).flush();
            writer.append("--" + boundary + "--").append(NEW_LINE);
            writer.close();
     
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
                conn.disconnect();
            } else {
                throw new IOException("Server returned status: " + status);
            }     
            return response;
        }
    }
}

