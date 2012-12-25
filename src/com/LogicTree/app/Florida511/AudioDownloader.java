///////////////////////////////////////////////////////////////////////
//Copyright (C) 2012 Costas Kleopa.
//All Rights Reserved.
//
//Costas Kleopa, costas.kleopa@gmail.com
//
//This source code is the confidential property of Costas Kleopa.
//All proprietary rights, including but not limited to any trade
//secrets, copyright, patent or trademark rights in and to this source
//code are the property of Costas Kleopa. This source code is not to
//be used, disclosed or reproduced in any form without the express
//written consent of Costas Kleopa.
///////////////////////////////////////////////////////////////////////
package com.LogicTree.app.Florida511;
/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.http.AndroidHttpClient;
import android.util.Log;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class AudioDownloader {
    private static final String LOG_TAG = "AudioDownloader";

    public enum Mode { NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT }
    private Mode mode = Mode.NO_ASYNC_TASK;
	private File cacheDir;
    
    public AudioDownloader(File cacheDir) {
    	this.cacheDir = cacheDir; 
    }
    
    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null audio will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public boolean download(String url, StringBuffer filename) {

        String file = convertURLtoFile(url);
        boolean succ = false;
        if (!hasExternalStoragePrivateFile(file)) {
            succ = downloadAudio(url, file);
        } else {
        	succ = true;
        }
        
        filename.append(cacheDir + File.separator + file);
        return succ;
    }


    /**
     * @param url
     * @return
     */
    private String convertURLtoFile (String url) {
    	if (url == null || url.endsWith("/")) {
    		return null;
    	}
    	int i = url.lastIndexOf("/");
    	if (i == -1) {
    		return url;
    	}
    	return url.substring(i + 1, url.length());
    }
    
    /**
     * @param Url
     * @param filename
     * @return
     */
    private boolean downloadAudio(String Url, String filename) {

        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = (mode == Mode.NO_ASYNC_TASK) ? new DefaultHttpClient() :
            AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(Url);
        boolean succ = false;
        try {
        	
        	HttpURLConnection http = null;
			URL url 			  = new URL(Url);
        	if (url.getProtocol().toLowerCase().equals("https")) {
	            trustAllHosts();
	                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
	                https.setHostnameVerifier(DO_NOT_VERIFY);
	                http = https;
	        } else {
	                http = (HttpURLConnection) url.openConnection();
	        }	        
        	InputStream stream = null;
			try {
				stream = http.getInputStream();
				createExternalStoragePrivateFile(filename, stream);
				succ = true;
			} finally {
                if (stream != null) {
                    stream.close();
                }
			}
        } catch (IOException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving audio from " + Url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + Url);
        } catch (Exception e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving audio from " + Url, e);
        } finally {
            if ((client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
        return succ;
    }

	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
	        public boolean verify(String hostname, SSLSession session) {
	                return true;
	        }
	};

	/**
	 * Trust every server - don't check for any certificate
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param filename
	 * @param is
	 */
	void createExternalStoragePrivateFile(String filename, InputStream is) {
	    // Create a path where we will place our private file on external
	    // storage.
	    File file = new File(cacheDir, filename);

	    try {
	    	BufferedInputStream bi = new BufferedInputStream(is);
	        OutputStream os = new FileOutputStream(file);
	        byte[] data = new byte[16384];
	        int count;
	        while ((count = bi.read(data)) != -1) {
	        	os.write(data, 0, count);
	        }
	        bi.close();
	        is.close();
	        os.close();
	    } catch (IOException e) {
	        // Unable to create file, likely because external storage is
	        // not currently mounted.
	        Log.w("ExternalStorage", "Error writing " + file, e);
	    }
	}

	/**
	 * @param filename
	 */
	void deleteExternalStoragePrivateFile(String filename) {
	    // Get path for the file on external storage.  If external
	    // storage is not currently mounted this will fail.
	    File file = new File(cacheDir, filename);
	    if (file != null) {
	        file.delete();
	    }
	}

	/**
	 * @param filename
	 * @return
	 */
	boolean hasExternalStoragePrivateFile(String filename) {
	    // Get path for the file on external storage.  If external
	    // storage is not currently mounted this will fail.
	    File file = new File(cacheDir, filename);
	    if (file != null) {
	        return file.exists();
	    }
	    return false;
	}
  
}