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

public class ActivityDisplayData extends MapActivity
{

	private static int THRESHOLD_GX = 20;
	private static int THRESHOLD_GY = 20;
	private static int THRESHOLD_GZ = 45;

	private Button btnReconnect;

	private SensorService mHRMService;
	private boolean isBound;

	private ServiceConnection mConnection;

	private int ax, ay, az;
	private int gx, gy, gz;
	private int mx, my, mz;

	private MapView mapView;
	private MapController myController;

	private boolean flagZoomIn;
	private boolean flagZoomOut;

	private final Handler activityHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			try
			{
				// tvInformation.setText(msg.getData().getString(
				// SensorService.CONST_MESSAGE_STRING));
				int ax = msg.getData().getInt(
						SensorService.CONST_ACCELERATION_X);
				int ay = msg.getData().getInt(
						SensorService.CONST_ACCELERATION_Y);
				int az = msg.getData().getInt(
						SensorService.CONST_ACCELERATION_Z);

				float gx = msg.getData().getFloat(
						SensorService.CONST_GYROSCOPE_X);
				float gy = msg.getData().getFloat(
						SensorService.CONST_GYROSCOPE_Y);
				float gz = msg.getData().getFloat(
						SensorService.CONST_GYROSCOPE_Z);

				int mx = msg.getData().getInt(SensorService.CONST_COMPASS_X);
				int my = msg.getData().getInt(SensorService.CONST_COMPASS_Y);
				int mz = msg.getData().getInt(SensorService.CONST_COMPASS_Z);

				// Log.e("DATA", "gx : " + gx + " , gy : " + gy + " , gz : " +
				// gz);

				if ((gx < THRESHOLD_GX) && gx > -THRESHOLD_GX)
				{
					gx = 0;
				} else
				{
					gx -= ((gx / Math.abs(gx)) * THRESHOLD_GX);
				}

				if ((gy < THRESHOLD_GY) && gy > -THRESHOLD_GY)
				{

					gy = 0;
				} else
				{
					gy -= ((gy / Math.abs(gy)) * THRESHOLD_GY);
				}

				myController.scrollBy((int) (gx / 5), (int) (gy / 5));
				
				if ((gy == 0) && (gy == 0))
				{
					if (gz < -THRESHOLD_GZ)
					{
						if (!flagZoomOut)
						{
							myController.zoomOut();
							flagZoomOut = true;
						}
					} else
					{
						flagZoomOut = false;
					}

					if (gz > THRESHOLD_GZ)
					{
						if (!flagZoomIn)
						{
							myController.zoomIn();
							flagZoomIn = true;
						}
					} else
					{
						flagZoomIn = false;
					}	
				}
				
				/*
				 * if (gx < -THRESHOLD_GX) { myController.scrollBy((int) (gx +
				 * THRESHOLD_GX), 0); }
				 * 
				 * if (gx > THRESHOLD_GX) { myController.scrollBy((int) (gx -
				 * THRESHOLD_GX), 0); }
				 * 
				 * if (gy < -THRESHOLD_GY) { myController.scrollBy(0, (int) (gy
				 * + THRESHOLD_GY)); }
				 * 
				 * if (gy > THRESHOLD_GY) { myController.scrollBy(0, (int) (gy -
				 * THRESHOLD_GY)); }
				 */

				

			} catch (Exception e)
			{
				Log.e("WRITE_ERROR", e.getMessage());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
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

		btnReconnect.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mHRMService.connectBluetooth();

				if (mHRMService.isConnected())
				{
					mHRMService.runService();
					btnReconnect.setEnabled(false);

					// Tell the user about this for our demo.
					Toast.makeText(ActivityDisplayData.this,
							"Data Reading Started..", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		mConnection = new ServiceConnection()
		{
			public void onServiceConnected(ComponentName className,
					IBinder service)
			{
				// This is called when the connection with the service has been
				// established, giving us the service object we can use to
				// interact with the service. Because we have bound to a
				// explicit
				// service that we know is running in our own process, we can
				// cast its IBinder to a concrete class and directly access it.
				mHRMService = ((SensorService.LocalBinder) service)
						.getService();

				mHRMService.initialize(deviceDetails, activityHandler);

				mHRMService.connectBluetooth();

				if (mHRMService.isConnected())
				{
					mHRMService.runService();

					btnReconnect.setEnabled(false);

					// Tell the user about this for our demo.
					Toast.makeText(ActivityDisplayData.this,
							"Data Reading Started..", Toast.LENGTH_SHORT)
							.show();
				}
			}

			public void onServiceDisconnected(ComponentName className)
			{
				// This is called when the connection with the service has been
				// unexpectedly disconnected -- that is, its process crashed.
				// Because it is running in our same process, we should never
				// see this happen.
				mHRMService = null;
				Toast.makeText(ActivityDisplayData.this,
						"Data Reading Stopped !", Toast.LENGTH_SHORT).show();
			}
		};

		doBindService();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		doUnbindService();
	}

	@Override
	protected void onResume()
	{
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		// Ideally a game should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onPause();
	}

	void doBindService()
	{
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(ActivityDisplayData.this, SensorService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
	}

	void doUnbindService()
	{
		if (isBound)
		{
			// Detach our existing connection.
			unbindService(mConnection);
			isBound = false;
		}
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
