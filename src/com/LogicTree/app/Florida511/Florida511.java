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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TabHost;

public class Florida511 extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, MapViewActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("traffic").setIndicator("Traffic",
	                      res.getDrawable(R.drawable.ic_tab_traffic))
	                  .setContent(intent);
	    tabHost.addTab(spec);

/*	    // Do the same for the other tabs
  	    intent = new Intent().setClass(this, TravelTimesActivity.class);
	    spec = tabHost.newTabSpec("travel_times").setIndicator("Travel Times",
	                      res.getDrawable(R.drawable.ic_tab_travel_times))
	                  .setContent(intent);
	    tabHost.addTab(spec);
*/
	    intent = new Intent().setClass(this, TrafficActivity.class);
	    spec = tabHost.newTabSpec("travel_times").setIndicator("Travel Times",
	                      res.getDrawable(R.drawable.ic_tab_travel_times))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, FeedbackActivity.class);
	    spec = tabHost.newTabSpec("feedback").setIndicator("Feedback",
	                      res.getDrawable(R.drawable.ic_tab_feedback))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SettingsActivity.class);
	    spec = tabHost.newTabSpec("settings").setIndicator("Settings",
	                      res.getDrawable(R.drawable.ic_tab_settings))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    for(int i = 0; i <tabHost.getTabWidget().getChildCount(); i++)
	    {
	    	tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#000000"));
	    }    
	    	    
	    tabHost.setCurrentTab(0);
	    
	    
	}
}
