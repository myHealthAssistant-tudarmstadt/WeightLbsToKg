/* 
 * Copyright (C) 2014 TU Darmstadt, Hessen, Germany.
 * Department of Computer Science Databases and Distributed Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
 package de.tudarmstadt.dvs.myhealthassistant.myhealthhub.transformations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.Event;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Advertisement;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Subscription;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Unadvertisement;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Unsubscription;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.SensorReadingEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.WeightEventInKg;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.WeightEventInLbs;


/**
 * 
 * @author HieuHa
 *
 */
public class WeightLbsToKg implements IWeightLbsToKg {
private static String TAG = "WeightLbsToKg";
	
	private Context context;
	
	private BroadcastReceiver receiver;
	
	private int eventCounter;
	
	private static String RECEIVER = "de.tudarmstadt.dvs.myhealthassistant.eventreceiver";
	private static String MANAGEMENT = "de.tudarmstadt.dvs.myhealthassistant.management";

	/**
	 * Callback method
	 * 
	 * @param context
	 */
	public void bindAndroidContext(Context context) {
		Log.i(TAG, "Strike! Binding the android context.");
		
		this.context = context;
		this.eventCounter = 0;
		
		// create event receiver
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				onEventReceive(context, intent);
			}
		};

		// register receiver
		context.registerReceiver(receiver, new IntentFilter(SensorReadingEvent.WEIGHT_IN_LBS));
		
		// generate subscription
		Subscription sub = new Subscription(
				TAG+eventCounter++,  
				getTimestamp(), TAG, context.getPackageName(), 
				SensorReadingEvent.WEIGHT_IN_LBS);
		// publish subscription
		sendToChannel(sub, MANAGEMENT);	
		
        // Generate advertisement
		Advertisement adverisement = new Advertisement(
        		TAG+eventCounter++, 
        		getTimestamp(), 
        		TAG,
        		context.getPackageName(),
        		SensorReadingEvent.WEIGHT_IN_KG, 
        		"-");
		// publish advertisement
		sendToChannel(adverisement, MANAGEMENT);	
	}
	
	public void onEventReceive(Context context, Intent intent) {
		
		/* Get the event itself */
		WeightEventInLbs weightInLbs = (WeightEventInLbs)intent.getParcelableExtra(Event.PARCELABLE_EXTRA_EVENT);
		
		// Create transformation Event
		WeightEventInKg weightInKg = new WeightEventInKg(
				TAG+eventCounter++,	getTimestamp(),
				TAG, TAG, getTimestamp(),
				(int)(weightInLbs.getWeight()*0.45359237));

		// Send event
		sendToChannel(weightInKg, RECEIVER);
	}
	
	/**
	 * Callback method
	 * 
	 * @param context
	 */
	public void unbindAndroidContext(Context context) {
		Log.i(TAG, "2nd Strike! Unregistering the broadcastReceiver.");
		this.context.unregisterReceiver(receiver);
		
		// generate unadvertisement
		Unadvertisement unadverisement = new Unadvertisement(
        		TAG+eventCounter++, 
        		getTimestamp(), 
        		TAG,
        		context.getPackageName(),
        		SensorReadingEvent.WEIGHT_IN_KG);
		
		// publish unadvertisement
		sendToChannel(unadverisement, MANAGEMENT);
		
		
		// generate unsubscription
		Unsubscription unsub = new Unsubscription(
				TAG+eventCounter++,  
				getTimestamp(), TAG, context.getPackageName(), 
				SensorReadingEvent.WEIGHT_IN_LBS);
		
		// publish unsubscription
		sendToChannel(unsub, MANAGEMENT);	
	}
	
	/**
	 * Sends an event to a specific channel using the LocalBroadcastManager
	 * @param Event to send
	 * @param Channel on which the event is sent
	 */
	protected void sendToChannel(Event evt, String channel) {
    	Intent i = new Intent();
    	i.putExtra(Event.PARCELABLE_EXTRA_EVENT_TYPE, evt.getEventType());
    	i.putExtra(Event.PARCELABLE_EXTRA_EVENT, evt);
    	i.setAction(channel);
    	context.sendBroadcast(i);
    }
	
	   /**
     * Returns the current time as "yyyy-MM-dd hh:mm:ss". 
     * @return timestamp 
     */
	private String getTimestamp() {
		return (String) android.text.format.DateFormat.format(
				"yyyy-MM-dd hh:mm:ss", new java.util.Date());
	}

}