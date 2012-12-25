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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.longevitysoft.android.util.Stringer;
import com.longevitysoft.android.xml.plist.domain.Dict;
import com.longevitysoft.android.xml.plist.domain.PList;

/**
 * @author costas
 *
 */
abstract public class SectionedAdapter extends BaseAdapter {

	private static final String TAG = "SectionedAdapter";
	Stringer stringer 				= new Stringer();
	TrafficActivity 	mLocalContext;

	abstract protected View getHeaderView(String caption,
			int index,
			View convertView,
			ViewGroup parent);

	private List<Section> sections=new ArrayList<Section>();
	private static int TYPE_SECTION_HEADER=0;
	private LayoutInflater mInflater;
	private Bitmap mIcon1;
	private PList incidents;

	private final ImageDownloader imageDownloader = new ImageDownloader();
	private ArrayList<Dict> positionsArray;

	/**
	 * @param context
	 * @param incidents
	 */
	public SectionedAdapter(Context context, PList incidents) {
		super();
		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
		this.incidents = incidents;
		mLocalContext = (TrafficActivity) context;
		// Icons bound to the rows.
		mIcon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.i95);
		positionsArray = null;
		imageDownloader.setMode(ImageDownloader.Mode.CORRECT);
	}

	/**
	 * @return
	 */
	public PList getIncidents() {
		return incidents;
	}

	/**
	 * @param incidents
	 */
	public void setIncidents(PList incidents) {
		this.incidents = incidents;

		notifyDataSetChanged();
	}

	/**
	 * @param caption
	 * @param adapter
	 */
	public void addSection(String caption, Adapter adapter) {
		sections.add(new Section(caption, adapter));
	}

	/**
	 * 
	 */
	public void clear() {
		sections.clear();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		for (Section section : this.sections) {
			if (position==0) {
				return(section);
			}

			int size=section.adapter.getCount()+1;

			if (position<size) {
				return(section.adapter.getItem(position-1));
			}

			position-=size;
		}

		return(null);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		int total=0;

		for (Section section : this.sections) {
			total+=section.adapter.getCount()+1; // add one for header
		}

		return(total);
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#getViewTypeCount()
	 */
	public int getViewTypeCount() {
		int total=1;	// one for the header, plus those from sections

		for (Section section : this.sections) {
			total+=section.adapter.getViewTypeCount();
		}

		return(total);
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	public int getItemViewType(int position) {
		int typeOffset=TYPE_SECTION_HEADER+1;	// start counting from here

		for (Section section : this.sections) {
			if (position==0) {
				return(TYPE_SECTION_HEADER);
			}

			int size=section.adapter.getCount()+1;

			if (position<size) {
				return(typeOffset+section.adapter.getItemViewType(position-1));
			}

			position-=size;
			typeOffset+=section.adapter.getViewTypeCount();
		}

		return(-1);
	}

	public boolean areAllItemsSelectable() {
		return(false);
	}

	public boolean isEnabled(int position) {
		return(getItemViewType(position)!=TYPE_SECTION_HEADER);
	}

	//	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionIndex=0;
		Section section;
		//for (Section section : this.sections) {
		Dict event;
		Object obj		= positionsArray.get(position);
		if (obj.getClass() == Dict.class) {
			event = (Dict) obj;
		} else {
			sectionIndex = ((Integer) obj).intValue();
			section = this.sections.get(sectionIndex);
			return (getHeaderView(section.caption, sectionIndex, convertView, parent));
		}

		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.heading);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.subText = (TextView) convertView.findViewById(R.id.subheading);
			//holder.arrow = (ImageButton) convertView.findViewById(R.id.arrowicon);
			//holder.arrowView = (View) convertView.findViewById(R.id.arrowview);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
			if (holder == null) {
				convertView = mInflater.inflate(R.layout.list_item_icon_text, null);
				holder 		= new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.heading);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.subText = (TextView) convertView.findViewById(R.id.subheading);
				//holder.arrow = (ImageButton) convertView.findViewById(R.id.arrowicon);
				//holder.arrowView = (View) convertView.findViewById(R.id.arrowview);
				convertView.setTag(holder);	                	
			}
		}

		//holder.arrow = (ImageButton) convertView.findViewById(R.id.arrowicon);
		//holder.arrowView.setOnClickListener(mLocalContext.mResourcesMapviewListener);

		if (incidents == null) {
			return null;
		}

		String type     = event.getConfiguration("type").getValue();
		String imgURL   = event.getConfiguration("image").getValue();
		String distance = event.getConfiguration("distance").getValue();
		String time     = event.getConfiguration("timestamp").getValue();

		holder.text.setText(type);
		holder.subText.setText(distance + time);

		imageDownloader.download(imgURL, (ImageView) holder.icon);

		holder.icon.setDrawingCacheEnabled(true);


		return convertView;

	}

	//@Override
	public long getItemId(int position) {
		return(position);
	}

	class Section {
		String caption;
		Adapter adapter;

		Section(String caption, Adapter adapter) {
			this.caption=caption;
			this.adapter=adapter;
		}
	}

	class ViewHolder {
		TextView text;
		TextView subText;
		ImageView icon;
		ImageButton arrow;
		View	arrowView;
	}

	public void setPositionArray(ArrayList<Dict> positionsArray) {
		this.positionsArray = positionsArray;

	}

}