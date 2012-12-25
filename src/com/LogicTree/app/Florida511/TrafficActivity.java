// /////////////////////////////////////////////////////////////////////
// Copyright (C) 2012 Costas Kleopa.
// All Rights Reserved.
// 
// Costas Kleopa, costas.kleopa@gmail.com
//
// This source code is the confidential property of Costas Kleopa.
// All proprietary rights, including but not limited to any trade
// secrets, copyright, patent or trademark rights in and to this source
// code are the property of Costas Kleopa. This source code is not to
// be used, disclosed or reproduced in any form without the express
// written consent of Costas Kleopa.
// /////////////////////////////////////////////////////////////////////
package com.LogicTree.app.Florida511;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.ListActivity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.longevitysoft.android.util.Stringer;
import com.longevitysoft.android.xml.plist.PListXMLHandler;
import com.longevitysoft.android.xml.plist.PListXMLParser;
import com.longevitysoft.android.xml.plist.domain.Array;
import com.longevitysoft.android.xml.plist.domain.Dict;
import com.longevitysoft.android.xml.plist.domain.PList;
// added MapActivity so I can include both Map and Traffic list in the same page by using a ViewFlipper.
// Unfortunately I couldn't get it to work, so I rolled back the change.
public class TrafficActivity extends ListActivity /*,MapActivity  */ implements HostnameVerifier {

	private static String[] countries 			     = null;
	private static LocationManager locationManager   = null;
	private static LocationListener locationListener = null;
	private static PList incidents					 = null;
    protected SectionedAdapter adapter 				 = null;
	private View topBar								 = null;
//	private ViewFlipper vf 							 = null;
	private Criteria criteria;
	Stringer stringer 								 = new Stringer();
	private String TAG = "TrafficActivity";
	private ArrayList positionsArray;

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	//@Override
    protected boolean isRouteDisplayed() {
            return false;
    }

	protected OnClickListener mResourcesMapviewListener = new OnClickListener() {
        public void onClick(View v) {
    		ListView lv 	= (ListView) topBar.findViewById(R.id.list);
    	
    		// This is where we swap the traffic list view to the map view within the same tab. 
    		// Had issues getting it to work, so cleaned up the code to at least get this to show the traffic list.
    		/*
    		MapView mapView = (MapView) findViewById(R.id.map_view);
    		mapView.setBuiltInZoomControls(true);
    		
    		vf.setInAnimation(getApplicationContext(), R.anim.in_from_left);
    		vf.setOutAnimation(getApplicationContext(), R.anim.out_to_right);
    		vf.showNext();
    		/*
    	/*    		
            Intent intent =
                    new Intent(TrafficActivity.this.getApplication(), 
                    		MapViewActivity.class);
            startActivity(intent);
            */
        }
    };

    /* (non-Javadoc)
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    public boolean verify(String hostname, SSLSession session) {
            return true;
    }

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

    public void reloadData() {
    	Log.v(stringer.newBuilder().append(TAG).toString(),
			  stringer.newBuilder().append("ReloadData").toString());
    	
    	if (adapter == null) {
    		return;
    	}
    	
    	adapter.clear();
    	positionsArray = new ArrayList();
    	Array a = (Array) incidents.getRootElement();
		for (int i = 0; i < a.size(); i++) {
    		Dict d = (Dict) a.get(i);
    		String section = d.getConfiguration("profile").getValue();
    		Array events   = d.getConfigurationArray("events");
    		ArrayList<String> array = new ArrayList<String>();
    		String[] sEvents = new String[events.size()];
    		
    		positionsArray.add(new Integer(i));
    		
    		for (int j = 0; j < events.size(); j++) {
    			Dict event = (Dict) events.get(j);
    			if (event != null) {
    				array.add((event).getConfiguration("type").getValue());
    	    		positionsArray.add(event);
    			}
    		}
    		array.toArray(sEvents);
    		adapter.addSection(section, new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sEvents));
    	}
    	adapter.setIncidents(incidents);
    	adapter.setPositionArray(positionsArray);
    }
    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    public void onStart() {
    	super.onStart();
    	
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

    	// Find an available provider to use which matches the criteria
    	String provider = locationManager.getBestProvider(criteria, true);

    	// Update the UI using the last known locations
    	Location location = locationManager.getLastKnownLocation(provider);
    	if (location != null) {
    		makeUseOfNewLocation(location, true);
    	}

    	// Start listening for location changes
    	locationManager.requestLocationUpdates(provider, 
    			10000, // 1min
    			1000,  // 1km
    			locationListener);

    	// Register the listener with the Location Manager to receive location updates
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);

    }
	/**
	 * @param location
	 */
	private void makeUseOfNewLocation(Location location, boolean initTime) {
		new GetNewGPSLocationTask(this, initTime).execute(location);
	}
	
	/**
	 * @author costas
	 *
	 */
	public class GetNewGPSLocationTask extends AsyncTask<Location, Integer, Long> {
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		private TrafficActivity activity;
		boolean initTime;
		
		/**
		 * @param activity
		 */
		public GetNewGPSLocationTask(TrafficActivity activity, boolean initTime) {
			this.activity = activity;
			this.initTime = initTime;
		}
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Long doInBackground(Location... locations) {
			int count = locations.length;
			for (int i = 0; i < count; i++) {
				activity.useNewLocation(locations[i], initTime);
			}
			return new Long(0);
		}
		
		@Override
		protected void onPostExecute(Long result) {
			activity.reloadData();
			super.onPostExecute(result);
		}
	}
	/**
	 * @param location
	 */
	public void useNewLocation(Location location, boolean initTime) {
		System.out.println("Current Location. Lat: "+ location.getLatitude() + ", Long: "+ location.getLongitude());
		int range = 200;
		String ani = "4436291070";
		String urlToSendRequest = "https://flmobile.logictree.com/FL511/m?cmd=traffic-list&latitude="+location.getLatitude() + "&longitude="+location.getLongitude() + "&ani="+ ani +"&range="+ range +"&pl=Current+Location";
		
		if (incidents != null) {
			stopLocationManager();
			return;
		}
		
		try {
			HttpURLConnection http = null;
			URL url 			  = new URL(urlToSendRequest);
	        if (url.getProtocol().toLowerCase().equals("https")) {
	            trustAllHosts();
	                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
	                https.setHostnameVerifier(this);//DO_NOT_VERIFY);
	                http = https;
	        } else {
	                http = (HttpURLConnection) url.openConnection();
	        }	        
			InputStream stream    = http.getInputStream();
			PListXMLHandler pxml  = new PListXMLHandler();
			PListXMLParser parser = new PListXMLParser();
			parser.setHandler(pxml);
			parser.parse(stream);
			stream.close();

			incidents = pxml.getPlist();
		} catch (Exception ex) {
			String error = ex.toString();
			Log.e("useNewLocation","PLIST: "+ error, ex);
		}
		
		if (!initTime) {
			stopLocationManager();
		}
	}


    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		countries = getResources().getStringArray(R.array.incidents_array);

		adapter = new SectionedAdapter(this, incidents) {
			protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
				TextView result = null;
				if (convertView == null || convertView.getClass() == TextView.class || convertView.getClass() == LinearLayout.class) {
					result = (TextView) getLayoutInflater().inflate(R.layout.header2, null);
				} else if (convertView.getClass() == TextView.class) {
					result = (TextView) convertView;
				}
				
				result.setText(caption);
				result.setGravity(Gravity.LEFT);

				return(result);
			}
		};

		/*
		// code i used to have to show the top bar. i commented it out since it wasn't working too well.
     	topBar    				= getLayoutInflater().inflate(R.layout.top_header, null);
        TextView status   		= (TextView) topBar.findViewById(R.id.status);
        ImageButton trafficView = (ImageButton) topBar.findViewById(R.id.left_button);
		ListView lv 		    = (ListView) topBar.findViewById(R.id.list);
//		vf 						= (ViewFlipper) topBar.findViewById(R.id.view_flipper);
		
		trafficView.setOnClickListener(mResourcesMapviewListener);
		status.setText(R.string.traffic);
		setContentView(lv);
		//setContentView(this);
		 */
		
		ListView lv = getListView();

		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);
		lv.setClickable(true);
		lv.setFocusable(true);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setVisibility(View.VISIBLE);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
//			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (positionsArray != null && position < positionsArray.size()) {
					Dict event = (Dict) positionsArray.get(position);
					if (event != null) {
						Array  audio = event.getConfigurationArray("audio");
						ArrayList<String> prompts = new ArrayList<String>();
						for (int k = 0; k < audio.size(); k++) {
							String url = ((com.longevitysoft.android.xml.plist.domain.String) audio.get(k)).getValue();
							Log.i("audio", url);
							prompts.add(url);
						}
						MediaManager.getInstance(getApplicationContext()).playAudioFilesInBackground(prompts);
						Log.v("audio", "pos: "+ position + ", id: " + id + "");
					} else {
						Log.v("audio", "pos: "+ position + ", id: " + id + ", section ignored");
					}
				} else {
					Log.v("audio", "pos: "+ position + ", id: " + id + ", out of range, ignored");
				}
			}
		});
	
		
		// Request GPS Location.
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Set the criteria for selection a location provider
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				makeUseOfNewLocation(location, false);
			}
			
			/**
			 * parsePList - this is the function that parses all the plist that comes in from the backend.
			 * @param plist
			 */
			private void parsePList(PList plist) {
				Array a = (Array) plist	.getRootElement();
				for (int i = 0; i < a.size(); i++) {
					Dict d = (Dict) a.get(i);
					String section = d.getConfiguration("profile").getValue();
					Array events   = d.getConfigurationArray("events");

					for (int j = 0; j < events.size(); j++) {
						Dict event = (Dict) events.get(i);							
						String image 	   = event.getConfiguration("image").getValue();
						String highway 	   = event.getConfiguration("highway").getValue();
						String location    = event.getConfiguration("location").getValue();
						String description = event.getConfiguration("description").getValue();
						String type 	   = event.getConfiguration("type").getValue();
						String distance    = event.getConfiguration("distance").getValue();
						String details     = event.getConfiguration("details").getValue();
						Log.i("image", image);
						Array  audio       = event.getConfigurationArray("audio");
						for (int k = 0; k < audio.size(); k++) {
							String url = ((com.longevitysoft.android.xml.plist.domain.String) audio.get(k)).getValue();
							Log.i("audio", url);
						}
					}
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};
	}
	/**
	 * 
	 */
	public void stopLocationManager() {
		//locationManager.removeUpdates(locationListener);
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		if (positionsArray != null && position < positionsArray.size()) {
			Dict event = (Dict) positionsArray.get(position);
			if (event != null) {
				Array  audio = event.getConfigurationArray("audio");
				ArrayList<String> prompts = new ArrayList<String>();
				for (int k = 0; k < audio.size(); k++) {
					String url = ((com.longevitysoft.android.xml.plist.domain.String) audio.get(k)).getValue();
					Log.i("audio", url);
					prompts.add(url);
				}
				MediaManager.getInstance(getApplicationContext()).playAudioFilesInBackground(prompts);
				Log.v("audio", "pos: "+ position + ", id: " + id + "");
			} else {
				Log.v("audio", "pos: "+ position + ", id: " + id + ", section ignored");
			}
		} else {
			Log.v("audio", "pos: "+ position + ", id: " + id + ", out of range, ignored");
		}
	}
}

