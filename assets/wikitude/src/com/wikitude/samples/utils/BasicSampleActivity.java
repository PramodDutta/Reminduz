package com.wikitude.samples.utils;

import java.io.IOException;

import android.app.Activity;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Toast;

import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.SensorAccuracyChangeListener;
import com.wikitude.samples.location.ILocationProvider;
import com.wikitude.samples.location.LocationProvider;
import com.wikitude.sdksamples.R;

public abstract class BasicSampleActivity extends Activity {

	/**
	 * holds the AR-view
	 */
	private ArchitectView					architectView;
	
	/**
	 * sensor accuracy listener in case you want to display calibration hints
	 */
	private SensorAccuracyChangeListener	sensorAccuracyListener;
	
	/**
	 * last known location of the user, used internally for content-loading after user location was fetched
	 */
	@SuppressWarnings("unused")
	private Location lastKnownLocaton;

	/**
	 * sample location strategy
	 */
	private ILocationProvider				locationProvider;
	
	/**
	 * location listener receives location updates and must forward them to the architectView
	 */
	private LocationListener locationListener;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate( final Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		/* pressing volume up/down should cause music volume changes */
		this.setVolumeControlStream( AudioManager.STREAM_MUSIC );

		/* set samples content view */
		this.setContentView( R.layout.sample_cam );

		/* set AR-view for life-cycle notifications etc. */
		this.architectView = (ArchitectView)this.findViewById( R.id.architectView );

		/* pass SDK key if you have one, this one is only valid for this package identifier and must not be used somewhere else */
		final ArchitectConfig config = new ArchitectConfig( Constants.WIKITUDE_SDK_KEY );

		/* first mandatory life-cycle notification */
		this.architectView.onCreate( config );

		this.sensorAccuracyListener = new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, Height = 3 */
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && BasicSampleActivity.this != null && !BasicSampleActivity.this.isFinishing() ) {
					Toast.makeText( BasicSampleActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
				}
			}
		};

		this.locationListener = new LocationListener() {

			@Override
			public void onStatusChanged( String provider, int status, Bundle extras ) {
			}

			@Override
			public void onProviderEnabled( String provider ) {
			}

			@Override
			public void onProviderDisabled( String provider ) {
			}

			@Override
			public void onLocationChanged( final Location location ) {
				if (location!=null) {
					BasicSampleActivity.this.lastKnownLocaton = location;
				if ( BasicSampleActivity.this.architectView != null ) {
					if ( location.hasAltitude() ) {
						BasicSampleActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
					} else {
						BasicSampleActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAccuracy() );
					}
				}
				}
			}
		};
		
		this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
		this.locationProvider = new LocationProvider( this, this.locationListener );
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ( this.architectView != null ) {
			this.architectView.onResume();
		}

		if ( this.locationProvider != null ) {
			this.locationProvider.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if ( this.architectView != null ) {
			this.architectView.onPause();
		}
		if ( this.locationProvider != null ) {
			this.locationProvider.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if ( this.architectView != null ) {
			if ( this.sensorAccuracyListener != null ) {
				this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
			this.architectView.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if ( this.architectView != null ) {
			this.architectView.onLowMemory();
		}
	}


	@Override
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate( savedInstanceState );
		if ( this.architectView != null ) {
			this.architectView.onPostCreate();
		}

		try {
			this.architectView.load( getARchitectWorldPath() );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	

	/**
	 * path to the architect-file (AR-Experience HTML) to launch
	 * @return
	 */
	public abstract String getARchitectWorldPath();

	

}