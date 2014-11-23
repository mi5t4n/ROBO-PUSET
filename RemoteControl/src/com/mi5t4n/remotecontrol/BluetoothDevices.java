package com.mi5t4n.remotecontrol;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothDevices extends Activity implements OnItemClickListener {
	// Intent request codes
	private static final int RESULT_OK = 401;
	private static final int REQUEST_ENABLE_BT =403;

	// Bluetooth variables and adapters
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private BluetoothAdapter btAdapter;
	private ListView lvPairedDevices;
	private Set<BluetoothDevice> pairedDevices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_devices);

		// Initializing the layout
		init();
		
		btAdapter=null;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		//Making sure the device has bluetooth
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth Detected",
					Toast.LENGTH_LONG).show();
		} else {
			//Turning on the bluetooth
			if(!btAdapter.isEnabled()) turnBTOn();
		}
	}

	@Override
	protected void onDestroy() {
		super.onPause();
		// Make sure we're not doing discovery anymore
		if (btAdapter != null) {
			btAdapter.cancelDiscovery();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void turnBTOn() {
		if (!btAdapter.isEnabled()) {
			Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(i, REQUEST_ENABLE_BT);
		}
		//Getting a set of paired devices present in the system.
		pairedDevices = btAdapter.getBondedDevices();
		//Filling the listview with name and mac address of the paired devices
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
			pairedDevicesArrayAdapter.notifyDataSetInvalidated();
		}

	}

	private void init() {
		// Setting ListView for paired devices
		lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
		lvPairedDevices.setOnItemClickListener(this);
		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 0);
		lvPairedDevices.setAdapter(pairedDevicesArrayAdapter);

		// Setting LIstView for available devices
		lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);
		lvPairedDevices.setOnItemClickListener(this);
		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 1);
		lvPairedDevices.setAdapter(pairedDevicesArrayAdapter);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_CANCELED) {
				finish();
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//Getting the bluetoothe devicce the user clicked in the listview
		BluetoothDevice tmp = (BluetoothDevice) pairedDevices.toArray()[arg2];
		//Returning the data to the MainAcitivity Class
		Intent intent = new Intent();
		intent.putExtra("device_name", tmp.getName());
		intent.putExtra("device_address", tmp.getAddress());
		setResult(RESULT_OK, intent);
		finish();

	}

}
