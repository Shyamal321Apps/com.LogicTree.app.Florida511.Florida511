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

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.TextView;

public class TravelTimesActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	TextView textview = new TextView(this);
        textview.setText("This is the Travel Times tab");
        setContentView(textview);
/*  
  		MapViewActivity mapViewActivity = new MapViewActivity(this);
    	setContentView(mapViewActivity);
  */  	
    	setContentView(R.id.maptablayout);
    
    }
}
