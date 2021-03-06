package usc.resl.harsh;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * This service class connects to bluetooth device and receives messages from BluetoothConnection classes using serviceHandler
 * The data is then parsed using firefly object and then passed in processed for to the activity handler. 
 * If any data is to be recorded, use filefly object to log the data within the handler
 * 
 * @author Ankit Sharma (ankit@usc.edu)
 *
 */
public class SensorService extends Service
{
	public static final String CONST_GYROSCOPE_X = "GX";
	public static final String CONST_GYROSCOPE_Y = "GY";
	public static final String CONST_GYROSCOPE_Z = "GZ";
	public static final String CONST_ACCELERATION_X = "AX";
	public static final String CONST_ACCELERATION_Y = "AY";
	public static final String CONST_ACCELERATION_Z = "AZ";
	public static final String CONST_COMPASS_X = "CX";
	public static final String CONST_COMPASS_Y = "CY";
	public static final String CONST_COMPASS_Z = "CZ";

	private HarshSensor myFireFlyDevice;

	private BluetoothConnection myBluetoothConnection;

	private Handler activityHandler;

	/**
	 * Class for clients to access.
	 */
	public class LocalBinder extends Binder
	{
		SensorService getService()
		{
			return SensorService.this;
		}
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	private final Handler serviceHandler = new Handler()
	{
		/**
		 * Handles the message received from bluetooth connection class
		 * This function is called by bluetooth connection class every time a packet is received
		 */
		public void handleMessage(Message message)
		{
			try
			{
				String messageString = message.getData().getString(
						BluetoothConnection.MESSAGE_READ);

				messageString = messageString.replace("!ANG:", "");
				
				Log.i("WRITE_SUCCESS", "Trying to write : " + messageString);
				
				// Parse the string
				myFireFlyDevice.parseMessage(messageString);
				
				// Send the obtained message to the UI Activity
				Message messageActivity = activityHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putFloat(CONST_GYROSCOPE_X, myFireFlyDevice.getGyroX());
				b.putFloat(CONST_GYROSCOPE_Y, myFireFlyDevice.getGyroY());
				b.putFloat(CONST_GYROSCOPE_Z, myFireFlyDevice.getGyroZ());
				b.putInt(CONST_ACCELERATION_X, myFireFlyDevice.getAccX());
				b.putInt(CONST_ACCELERATION_Y, myFireFlyDevice.getAccY());
				b.putInt(CONST_ACCELERATION_Z, myFireFlyDevice.getAccZ());
				b.putInt(CONST_COMPASS_X, myFireFlyDevice.getCompX());
				b.putInt(CONST_COMPASS_Y, myFireFlyDevice.getCompY());
				b.putInt(CONST_COMPASS_Z, myFireFlyDevice.getCompZ());
				messageActivity.setData(b);
				activityHandler.sendMessage(messageActivity);

				// Write a log for this				
				//myFireFlyDevice.writeLog();

				Log.i("WRITE_SUCCESS", "Log written...");

			} catch (Exception e)
			{
				Log.e("WRITE_ERROR", "Unable to write to activity and log : " + e.getMessage());
			}
		}
	};

	@Override
	public void onCreate()
	{

	}

	/**
	 * Initialize the bluetooth connection by passing the details of the device to be connected
	 * @param deviceDetails
	 * @param handler
	 */
	public void initialize(String deviceDetails, Handler handler)
	{
		String[] splitString = deviceDetails.split("\n");

		myFireFlyDevice = new HarshSensor(splitString[0], splitString[1]);

		// Create an object to establish Bluetooth connection
		myBluetoothConnection = new BluetoothConnection(myFireFlyDevice
				.getDeviceMacAddress(), serviceHandler);

		// Get the device name and address
		myFireFlyDevice.fileOpen();

		activityHandler = handler;
	}

	@Override
	public void onDestroy()
	{
		// Disconnect the connection
		myBluetoothConnection.disconnect();
		
		// Close the file in which log is written
		myFireFlyDevice.fileClose();

		Log.i("LocalService", "Service closed");

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	/**
	 * Connects to the bluetooth device. This call should always be preceded by a call to initialize function
	 */
	public void connectBluetooth()
	{
		try
		{
			// Create a connection
			myBluetoothConnection.createConnection();

		} catch (Exception e)
		{
			Log.i("BLUETOOTH_CONNECTION", e.getMessage());
		}
	}

	/**
	 * Checks if device is connected or not
	 * @return true or false indicating the connection with the device
	 */
	public boolean isConnected()
	{
		return myBluetoothConnection.isDeviceConnected();
	}

	/**
	 * Start reading the data
	 */
	public void runService()
	{
		new Thread(myBluetoothConnection).start();
	}
}

