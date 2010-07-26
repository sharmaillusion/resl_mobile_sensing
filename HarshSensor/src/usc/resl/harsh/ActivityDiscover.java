package usc.resl.harsh;

import java.util.Iterator;
import java.util.Set;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityDiscover extends ListActivity {
	BluetoothAdapter mBluetoothAdapter;

	// All codes that relate to BlueTooth start with 1*
	private static final int CODE_REQUEST_ENABLE_BLUETOOTH = 10;

	private ProgressDialog mProgressDialog;
	private ArrayAdapter<String> mArrayAdapter;

	Button btnRefresh;

	// Register the BroadcastReceiver
	IntentFilter filterDeviceFound = new IntentFilter(
			BluetoothDevice.ACTION_FOUND);
	IntentFilter filterDiscoveryStarted = new IntentFilter(
			BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	IntentFilter filterDiscoveryFinished = new IntentFilter(
			BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery is started
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Display the progress dialog
				mProgressDialog = ProgressDialog.show(ActivityDiscover.this,
						"Discovering Bluetooth Devices",
						"Please wait while other bluetooth devices in range are being searched. "
								+ "Press back to cancel discovery", true, true,
						new OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								// Cancel the discovery. Just to be
								// safe
								mBluetoothAdapter.cancelDiscovery();
							}
						});
			}

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (device != null) {
					String name = null;
					String address = null;

					// Get device name and address
					name = device.getName();
					address = device.getAddress();

					// Check if they are not null, display them
					if (name != null && address != null) {
						if (name.startsWith("FireFly")) {
							Toast.makeText(ActivityDiscover.this,
									name + "\n" + address, Toast.LENGTH_SHORT)
									.show();

							// Add the name and address to an array adapter to
							// show
							// in a
							// listView
							mArrayAdapter.add(name + "\n" + address);
						}
					}
				}
			}

			// When discovery is finished
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if (mProgressDialog != null) {
					// Display the progress dialog
					mProgressDialog.dismiss();
				}

				// Cancel the discovery. Just to be safe
				mBluetoothAdapter.cancelDiscovery();
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discover);

		// Create a new array adapter
		mArrayAdapter = new ArrayAdapter<String>(ActivityDiscover.this,
				android.R.layout.simple_list_item_1);

		// Set this adapter to the list view
		setListAdapter(mArrayAdapter);

		// Get refresh button from activity layout
		btnRefresh = (Button) findViewById(R.id.buttonDiscoverRefresh);

		// Set on click listener. to search all devices in range
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Clear previous data from adapter
				ActivityDiscover.this.mArrayAdapter.clear();

				// Start the discovery
				ActivityDiscover.this.mBluetoothAdapter.startDiscovery();
			}
		});

		// Get default bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If adapter is null or not presens, display appropriate message
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Device does not support bluetooth",
					Toast.LENGTH_SHORT).show();

			// Finish this activity
			this.finish();
		}

		// If Bluetooth adapter is not turned on, request to turn it on
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent,
					CODE_REQUEST_ENABLE_BLUETOOTH);
		}

		// Register a receiver to start listening for discovery start
		registerReceiver(mReceiver, filterDiscoveryStarted);

		// Clear the array and get bonded pairs
		ActivityDiscover.this.mArrayAdapter.clear();

		// Get all bonded devices (or paired devices)
		// Paired devices are the devices such that both devices know the pass
		// key of the other device
		Set<BluetoothDevice> mySet = ActivityDiscover.this.mBluetoothAdapter
				.getBondedDevices();

		// If any devices are found, display them, else start a new discovery
		if (mySet.size() > 0) {
			Iterator<BluetoothDevice> myIterator = mySet.iterator();

			while (myIterator.hasNext()) {
				BluetoothDevice myDevice = myIterator.next();
				mArrayAdapter.add(myDevice.getName() + "\n"
						+ myDevice.getAddress());
			}
		} else {
			// Start discovery if no bonded pair is avaiable
			ActivityDiscover.this.mBluetoothAdapter.startDiscovery();
		}

		// Register a receiver if a device is found
		registerReceiver(mReceiver, filterDeviceFound);

		// Register a receiver if device discovery is finished
		registerReceiver(mReceiver, filterDiscoveryFinished);
	}

	@Override
	protected void onResume() {
		// Since we disabled these buttons while going to display data activity,
		// we need to enable them when coming back
		// Set the list view to be enabled
		getListView().setEnabled(true);

		// Set the refresh button to be enabled
		ActivityDiscover.this.btnRefresh.setEnabled(true);

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// Unregister the receiver from bluetooth broadcast receiver. Do not
		// forget to write this.
		unregisterReceiver(mReceiver);

		// Cancel any discovery, just to be sure
		mBluetoothAdapter.cancelDiscovery();

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_REQUEST_ENABLE_BLUETOOTH) {
			if (resultCode == RESULT_OK) {

			} else if (resultCode == RESULT_CANCELED) {
				this.finish();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Disable the listview
		l.setEnabled(false);

		// Disable refresh button
		ActivityDiscover.this.btnRefresh.setEnabled(false);

		// Create new intent
		Intent newIntent = new Intent(ActivityDiscover.this,
				ActivityDisplayData.class);
		newIntent.putExtra(SharedVariables.CONST_DEVICE_INFORMATION, l
				.getItemAtPosition(position).toString());

		// Start the activity
		startActivity(newIntent);

		super.onListItemClick(l, v, position, id);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
}