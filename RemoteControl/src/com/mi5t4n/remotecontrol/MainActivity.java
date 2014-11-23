package com.mi5t4n.remotecontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
public class MainActivity extends ActionBarActivity implements OnTouchListener {
	// Motion constants
	public final static byte LEFT_UP = 101;
	public final static byte LEFT_DOWN = 102;
	public final static byte RIGHT_UP = 103;
	public final static byte RIGHT_DOWN = 104;
	public final static byte FORWARD_UP = 105;
	public final static byte FORWARD_DOWN = 106;
	public final static byte BACKWARD_UP = 107;
	public final static byte BACKWARD_DOWN = 108;
	public final static byte ACK = 117;

	// Intent request codes
	private static final int RESULT_OK = 401;
	private static final int REQUEST_BLUETOOTH_DEVICE_ACTIVITY = 402;
	private static final int REQUEST_ENABLE_BT = 403;

	// ConnectThread constants
	public static final int CONNECT_SUCCESS = 301;
	public static final int CONNECT_ERROR = 302;
	public static final int CONNECT_START = 303;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_CANCEL = 4;
	public static final int MESSAGE_UUIDS_START = 5;
	public static final int MESSAGE_UUIDS_SUCCESS = 7;
	public static final int MESSAGE_CLOSE_SUCCESS = 8;
	public static final int MESSAGE_STREAM_SUCCESS = 9;
	public static final int ERROR_MESSAGE_READ = 10;
	public static final int ERROR_MESSAGE_WRITE = 11;
	public static final int ERROR_MESSAGE_CANCEL = 12;
	public static final int ERROR_MESSAGE_UUIDS_START = 13;
	public static final int ERROR_MESSAGE_UUIDS_SUCCESS = 14;
	public static final int ERROR_MESSAGE_CLOSE = 15;
	public static final int ERROR_MESSAGE_STREAM = 16;

	BluetoothAdapter btAdapter;
	BluetoothDevice btDevice;
	ConnectThread btConnectThread;
	ConnectedThread btConnectedThread;
	
	Handler btThreadHandler;

	ImageButton ibForward, ibBackward, ibLeft, ibRight, ibArmLeft, ibArmRight;
	TextView tvDebug, tvConnectionStatus;

	boolean connectionSuccess = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		init();

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth Detected",
					Toast.LENGTH_LONG).show();
		}
		if (!btAdapter.isEnabled()) {
			Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(i, REQUEST_ENABLE_BT);
		}
		btThreadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case CONNECT_ERROR:
					addMessage("CONNECTION ERROR");
					tvConnectionStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btdisconnected,0);
					break;
				case CONNECT_SUCCESS:
					addMessage("CONNECTION SUCCESS");
					tvConnectionStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btconnected,0);
					break;
				case CONNECT_START:
					addMessage("CONNECTION START");
					break;
				case MESSAGE_READ:
					byte data = (byte) msg.obj;
					if (data == ACK)
						addMessage("ACK RECEIVED");
					break;
				case MESSAGE_WRITE:
					addMessage("MESSAGE WRITE : " + String.valueOf(msg.obj));
					break;
				case MESSAGE_CANCEL:
					addMessage("SOCKET CLOSE");
					tvConnectionStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btdisconnected,0);
					break;
				case ERROR_MESSAGE_READ:
					addMessage("MESSAGE READ ERROR");
					tvConnectionStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btdisconnected,0);
					break;
				case ERROR_MESSAGE_WRITE:
					addMessage("MESSAGE WRITE ERROR");
					tvConnectionStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.btdisconnected,0);
					break;
				}
			}
		};
	}

	private void init() {
		// TODO Auto-generated method stub

		tvDebug = (TextView) findViewById(R.id.tvDebug);
		tvDebug.setMovementMethod(new ScrollingMovementMethod());
		
		tvConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
		tvConnectionStatus.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.btdisconnected, 0);
		
		ibForward = (ImageButton) findViewById(R.id.ibForward);
		ibBackward = (ImageButton) findViewById(R.id.ibBackward);
		ibLeft = (ImageButton) findViewById(R.id.ibLeft);
		ibRight = (ImageButton) findViewById(R.id.ibRight);

		ibForward.setOnTouchListener(this);
		ibBackward.setOnTouchListener(this);
		ibLeft.setOnTouchListener(this);
		ibRight.setOnTouchListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			break;
		case R.id.connect:
			Intent intent = new Intent("android.intent.action.BLUETOOTHDEVICES");
			startActivityForResult(intent, REQUEST_BLUETOOTH_DEVICE_ACTIVITY);
			break;
		case R.id.quit:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_BLUETOOTH_DEVICE_ACTIVITY) {
			switch (resultCode) {
			case RESULT_OK:
				String tmp = data.getStringExtra("device_name") + "\n"
						+ data.getStringExtra("device_address");
				tvConnectionStatus.setText(tmp);
				btDevice = btAdapter.getRemoteDevice(data.getStringExtra("device_address"));
					ParcelUuid[] parcelUuids = btDevice.getUuids();
					addMessage(parcelUuids[0].toString());
					btConnectThread = null;
					btConnectedThread = null;
					btConnectThread = new ConnectThread(btDevice);
					btConnectThread.start();
				break;
			}
		} else if (requestCode == REQUEST_ENABLE_BT) {
			Toast.makeText(getApplicationContext(), "Bluetooth not enabled",
					Toast.LENGTH_LONG).show();
		}
	}

	// function to append a string to a TextView as a new line
	// and scroll to the bottom if needed
	private void addMessage(String msg) {
		// append the new string
		tvDebug.append(msg + "\n");
		// find the amount we need to scroll. This works by
		// asking the TextView's internal layout for the position
		// of the final line and then subtracting the TextView's height
		final int scrollAmount = tvDebug.getLayout().getLineTop(
				tvDebug.getLineCount())
				- tvDebug.getHeight();
		// if there is no need to scroll, scrollAmount will be <=0
		if (scrollAmount > 0)
			tvDebug.scrollTo(0, scrollAmount);
		else
			tvDebug.scrollTo(0, 0);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		switch (arg0.getId()) {
		case R.id.ibForward:
			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btConnectedThread.write(FORWARD_DOWN);
				break;
			case MotionEvent.ACTION_UP:
				btConnectedThread.write(FORWARD_UP);
				break;
			}
			break;
		case R.id.ibBackward:
			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btConnectedThread.write(BACKWARD_DOWN);
				break;
			case MotionEvent.ACTION_UP:
				btConnectedThread.write(BACKWARD_UP);

				break;
			}
			break;
		case R.id.ibLeft:
			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btConnectedThread.write(LEFT_DOWN);
				break;
			case MotionEvent.ACTION_UP:
				btConnectedThread.write(LEFT_UP);
				break;
			}
			break;
		case R.id.ibRight:
			switch (arg1.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btConnectedThread.write(RIGHT_DOWN);
				break;
			case MotionEvent.ACTION_UP:
				btConnectedThread.write(RIGHT_UP);
				break;
			}
		}
		return true;
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				btThreadHandler.obtainMessage(MESSAGE_UUIDS_START)
						.sendToTarget();
				ParcelUuid[] parcelUuids = mmDevice.getUuids();
				tmp = device.createRfcommSocketToServiceRecord(parcelUuids[0]
						.getUuid());
			} catch (IOException e) {
				btThreadHandler.obtainMessage(ERROR_MESSAGE_UUIDS_START)
						.sendToTarget();
			}
			btThreadHandler.obtainMessage(MESSAGE_UUIDS_SUCCESS).sendToTarget();
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			if (btAdapter.isDiscovering())
				btAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				btThreadHandler.obtainMessage(CONNECT_START).sendToTarget();
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				btThreadHandler.obtainMessage(CONNECT_ERROR).sendToTarget();
				try {
					mmSocket.close();
					btThreadHandler.obtainMessage(MESSAGE_CLOSE_SUCCESS)
							.sendToTarget();
				} catch (IOException closeException) {
					btThreadHandler.obtainMessage(ERROR_MESSAGE_CLOSE)
							.sendToTarget();
				}
				return;
			}
			// Do work to manage the connection (in a separate thread)
			btThreadHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
			manageConnectedSocket(mmSocket);
		}

		private void manageConnectedSocket(BluetoothSocket mmSocket2) {
			// TODO Auto-generated method stub
			btConnectedThread = null;
			btConnectedThread = new ConnectedThread(mmSocket2);
			btConnectedThread.start();

		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
				btThreadHandler.obtainMessage(MESSAGE_CLOSE_SUCCESS)
						.sendToTarget();
			} catch (IOException e) {
				btThreadHandler.obtainMessage(ERROR_MESSAGE_CLOSE)
						.sendToTarget();
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				btThreadHandler.obtainMessage(ERROR_MESSAGE_STREAM)
						.sendToTarget();
			}
			btThreadHandler.obtainMessage(MESSAGE_STREAM_SUCCESS)
					.sendToTarget();
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte data; // buffer store for the stream

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					data = (byte) mmInStream.read();
					// Send the obtained bytes to the UI activity
					// mHandler.obtainMessage(MESSAGE_READ, bytes, -1,
					// buffer).sendToTarget();
					btThreadHandler.obtainMessage(MESSAGE_READ, data)
							.sendToTarget();
				} catch (IOException e) {
					btThreadHandler.obtainMessage(ERROR_MESSAGE_READ)
							.sendToTarget();
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte data) {
			try {
				mmOutStream.write(data);
				btThreadHandler.obtainMessage(MESSAGE_WRITE, data)
						.sendToTarget();
			} catch (IOException e) {
				btThreadHandler.obtainMessage(ERROR_MESSAGE_WRITE)
						.sendToTarget();
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
				btThreadHandler.obtainMessage(MESSAGE_CLOSE_SUCCESS)
						.sendToTarget();
			} catch (IOException e) {
				btThreadHandler.obtainMessage(ERROR_MESSAGE_CLOSE)
						.sendToTarget();
			}
		}
	}
}
