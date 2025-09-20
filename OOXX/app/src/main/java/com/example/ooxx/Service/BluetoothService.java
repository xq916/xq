package com.example.ooxx.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final IBinder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String command = intent.getStringExtra("command");
            if ("connect".equals(command)) {
                String deviceAddress = intent.getStringExtra("deviceAddress");
                connectToDevice(deviceAddress);
            }
        }
        return START_STICKY;
    }

    private void connectToDevice(String deviceAddress) {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported");
            return;
        }

        // 检查蓝牙连接权限
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public void sendMove(int row, int col, int player) {
        if (connectedThread != null) {
            String message = "MOVE:" + row + "," + col + "," + player;
            connectedThread.write(message.getBytes());
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            mmSocket = null; // 初始化为null

            // 检查蓝牙连接权限
            if (ActivityCompat.checkSelfPermission(BluetoothService.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
                return;
            }

            try {
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        }

        public void run() {
            // 检查蓝牙扫描权限
            if (ActivityCompat.checkSelfPermission(BluetoothService.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_SCAN permission not granted");
                return;
            }

            // 检查socket是否创建成功
            if (mmSocket == null) {
                Log.e(TAG, "Socket is null, cannot connect");
                return;
            }

            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                connectedThread = new ConnectedThread(mmSocket);
                connectedThread.start();
            } catch (IOException connectException) {
                Log.e(TAG, "Could not connect to device", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when connecting", e);
            }
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
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

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating streams", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String message = new String(buffer, 0, bytes);
                    processMessage(message);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

        private void processMessage(String message) {
            if (message.startsWith("MOVE:")) {
                String[] parts = message.split(":")[1].split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                int player = Integer.parseInt(parts[2]);

                // 通过广播发送移动信息
                Intent intent = new Intent("BLUETOOTH_MOVE");
                intent.putExtra("row", row);
                intent.putExtra("col", col);
                intent.putExtra("player", player);
                sendBroadcast(intent);
            }
        }
    }
}
