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

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.maps.OnSingleTapListener;
import com.readystatesoftware.maps.TapControlledMapView;

/**
 * @author costas
 *
 */
public class MapViewActivity extends MapActivity {

	TapControlledMapView mapView; // use the custom TapControlledMapView
	List<Overlay> mapOverlays;
	Drawable drawable;
	Drawable drawable2;
	SimpleItemizedOverlay itemizedOverlay;
	SimpleItemizedOverlay itemizedOverlay2;
	MapController controller;
	public double itemLang, itemLong, currLat, currLon;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map_view);
		mapView = (TapControlledMapView) findViewById(R.id.mapview);
		//mapView.setBuiltInZoomControls(true);
		
		Button traffic = (Button) findViewById(R.id.btnGoBack);
		traffic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MapViewActivity.this, TrafficActivity.class);
				startActivity(i);
			}
		});
		
		LocationManager locationManager;
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(context);
		//String provider = LocationManager.GPS_PROVIDER;

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationManager.getBestProvider(criteria, true);
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new mylocationlistener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
		
		
		
		// dismiss balloon upon single tap of MapView (iOS behavior) 
		mapView.setOnSingleTapListener(new OnSingleTapListener() {		
			@Override
			public boolean onSingleTap(MotionEvent e) {
				itemizedOverlay.hideAllBalloons();
				return true;
			}
		});
		
		
		
		if (savedInstanceState == null) {
			
			final MapController mc = mapView.getController();
			//mc.animateTo(point2);
			mc.setZoom(13);
			
		} else {
			
			// example restoring focused state of overlays
			int focused;
			focused = savedInstanceState.getInt("focused_1", -1);
			if (focused >= 0) {
				itemizedOverlay.setFocus(itemizedOverlay.getItem(focused));
			}
			focused = savedInstanceState.getInt("focused_2", -1);
			if (focused >= 0) {
				itemizedOverlay2.setFocus(itemizedOverlay2.getItem(focused));
			}
			
		}

		//MapView mapView = (MapView) findViewById(R.id.mapview);
		//mapView.setBuiltInZoomControls(true);
		
		

		//mapView.postInvalidate();

	}
    

	private class mylocationlistener implements LocationListener {
		public void onLocationChanged(final Location location) {
			if (location != null) {
				//Log.d("LOCATION CHANGED", location.getLatitude() + "");
				//Log.d("LOCATION CHANGED", location.getLongitude() + "");
				currLat = location.getLatitude();
				currLon = location.getLongitude();

				locationMe();
				
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	void locationMe(){
		
		
		mapOverlays = mapView.getOverlays();
		mapView.getOverlays().clear();
		mapOverlays.clear();
		// first overlay
		drawable = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new SimpleItemizedOverlay(drawable, mapView);
		// set iOS behavior attributes for overlay
		itemizedOverlay.setShowClose(false);
		itemizedOverlay.setShowDisclosure(true);
		itemizedOverlay.setSnapToCenter(false);
		
		GeoPoint point = new GeoPoint((int)(28.521383*1E6),(int)(-82.246652*1E6));
		OverlayItem overlayItem = new OverlayItem(point, "Tomorrow Never Dies (1997)", 
				"(M gives Bond his mission in Daimler car)");
		itemizedOverlay.addOverlay(overlayItem);
		
		GeoPoint point2 = new GeoPoint((int)(28.523003*1E6),(int)(-82.220306*1E6));
		OverlayItem overlayItem2 = new OverlayItem(point2, "GoldenEye (1995)", 
				"(Interiors Russian defence ministry council chambers in St Petersburg)");		
		itemizedOverlay.addOverlay(overlayItem2);
		
		mapOverlays.add(itemizedOverlay);
		
		// second overlay
		drawable2 = getResources().getDrawable(R.drawable.marker2);
		itemizedOverlay2 = new SimpleItemizedOverlay(drawable2, mapView);
		// set iOS behavior attributes for overlay
		itemizedOverlay2.setShowClose(false);
		itemizedOverlay2.setShowDisclosure(true);
		itemizedOverlay2.setSnapToCenter(false);
		
		GeoPoint point3 = new GeoPoint((int)(28.510936*1E6),(int)(-82.242279*1E6));
		OverlayItem overlayItem3 = new OverlayItem(point3, "Sliding Doors (1998)", null);
		itemizedOverlay2.addOverlay(overlayItem3);
		
	
		
		// create an overlay that shows our current location
		//FixedMyLocationOverlay myLocationOverlay = new FixedMyLocationOverlay(this, mapView);

		// add this overlay to the MapView and refresh it
		//mapView.getOverlays().add(myLocationOverlay);
		
		mapOverlays.add(itemizedOverlay2);
		GeoPoint point5 = new GeoPoint((int)(currLat*1E6),(int)(currLon*1E6));
		OverlayItem overlayItem5 = new OverlayItem(point5, "I'm Here",
				"Norwest Business Park");
		itemizedOverlay.addOverlay(overlayItem5);
		mapOverlays.add(itemizedOverlay);
		
		mapView.invalidate();
	}
    

	@Override
    protected boolean isRouteDisplayed() {
            return false;
    }
}
