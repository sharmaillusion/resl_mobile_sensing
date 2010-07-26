package usc.resl.harsh;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * This class registers itself to Sensor Service and collects data gathered from
 * the device. It displays map and the map is controlled using gyro data from
 * the sensor. The parameters are tweaked according to a particular sensor.
 * Tilting the sensor to either side or up and down, enables the map to move in
 * respective direction. Changing the yaw provides zoom in and zoom out function
 * corresponding direction * @author Ankit Sharma (ankit@usc.edu)
 * 
 */
public class ActivityDisplayData extends MapActivity {

	private static int THRESHOLD_GX = 20;
	private static int THRESHOLD_GY = 20;
	private static int THRESHOLD_GZ = 45;

	private Button btnReconnect;

	private SensorService mHRMService;
	private boolean isBound;

	private ServiceConnection mConnection;

	private MapView mapView;
	private MapController myController;

	// Flags to control zooming in and zooming out of the map
	private boolean flagZoomIn;
	private boolean flagZoomOut;

	private final Handler activityHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				float gx = msg.getData().getFloat(
						SensorService.CONST_GYROSCOPE_X);
				float gy = msg.getData().getFloat(
						SensorService.CONST_GYROSCOPE_Y);
				float gz = msg.getData().getFloat(
						SensorService.CONST_GYROSCOPE_Z);

				// Check if gx value is less than the threshold value, if yes,
				// set gx to 0 (map is still on x-axis)
				if ((gx < THRESHOLD_GX) && gx > -THRESHOLD_GX) {
					gx = 0;
				}
				// Else Add / subtract the threshold and value above threashold
				// is treated as acceleration in x-direction
				// Hence speed is dependent on the tilt
				else {
					gx -= ((gx / Math.abs(gx)) * THRESHOLD_GX);
				}

				// Check if gy value is less than the threshold value, if yes,
				// set gy to 0 (map is still on y-axis)
				if ((gy < THRESHOLD_GY) && gy > -THRESHOLD_GY) {

					gy = 0;
				}
				// Else add/subtract the threshold and value above threashold
				// is treated as acceleration in y-direction
				// Hence speed is dependent on the tilt
				else {
					gy -= ((gy / Math.abs(gy)) * THRESHOLD_GY);
				}

				// Scroll by the specified speed in x and y direction
				myController.scrollBy((int) (gx / 5), (int) (gy / 5));

				// Check for yaw ONLY if there is no movement in x an y
				// direction
				if ((gy == 0) && (gy == 0)) {
					// Check if yaw is less than the negative of threshold
					if (gz < -THRESHOLD_GZ) {
						// If yes, then check if the map hasnt been zoomed out
						// already
						if (!flagZoomOut) {
							// Zoom out the map and set the flag to be true
							// If we do not use the flag then the reading are
							// coming so fast that it would become impossible to
							// hold at one particular zoom level. Therefore, we
							// zoom out / in only one level at a time
							myController.zoomOut();
							flagZoomOut = true;
						}
					} 
					else 
					{
						flagZoomOut = false;
					}

					// Check if yaw is greater than the positive of threshold
					if (gz > THRESHOLD_GZ) {
						
						if (!flagZoomIn) {
							myController.zoomIn();
							flagZoomIn = true;
						}
					} else {
						flagZoomIn = false;
					}
				}

			} catch (Exception e) {
				Log.e("WRITE_ERROR", e.getMessage());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_data);

		// Extract device information from the intent extras

		Bundle extras = getIntent().getExtras();
		final String deviceDetails = extras
				.getString(SharedVariables.CONST_DEVICE_INFORMATION);

		mapView = (MapView) findViewById(R.id.map_view);
		mapView.setClickable(true);
		mapView.setFocusable(true);

		myController = mapView.getController();

		btnReconnect = (Button) findViewById(R.id.buttonDisplayDataReconnect);

		btnReconnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// Connect to the bluetooth device
				mHRMService.connectBluetooth();

				// Confirm that the device is connected
				if (mHRMService.isConnected()) {
					// Start reading data from the service
					mHRMService.runService();
					
					// Disable the button
					btnReconnect.setEnabled(false);

					Toast.makeText(ActivityDisplayData.this,
							"Data Reading Started..", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// This is called when the connection with the service has been
				// established, giving us the service object we can use to
				// interact with the service. Because we have bound to a
				// explicit
				// service that we know is running in our own process, we can
				// cast its IBinder to a concrete class and directly access it.
				mHRMService = ((SensorService.LocalBinder) service)
						.getService();

				// Initialize the service by providing device details to it
				mHRMService.initialize(deviceDetails, activityHandler);

				// Connect to bluetooth device
				mHRMService.connectBluetooth();

				// Confirm that the device is connected
				if (mHRMService.isConnected()) {
					
					// Start reading the data
					mHRMService.runService();

					// Disable the button
					btnReconnect.setEnabled(false);

					// Tell the user about this for our demo.
					Toast.makeText(ActivityDisplayData.this,
							"Data Reading Started..", Toast.LENGTH_SHORT)
							.show();
				}
			}

			public void onServiceDisconnected(ComponentName className) {
				// This is called when the connection with the service has been
				// unexpectedly disconnected -- that is, its process crashed.
				// Because it is running in our same process, we should never
				// see this happen.
				mHRMService = null;
				Toast.makeText(ActivityDisplayData.this,
						"Data Reading Stopped !", Toast.LENGTH_SHORT).show();
			}
		};

		// Bind the service
		doBindService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Unbind the service
		doUnbindService();
	}

	@Override
	protected void onResume() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();
	}

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(ActivityDisplayData.this, SensorService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
	}

	void doUnbindService() {
		if (isBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			isBound = false;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
