package com.example.myfirstapp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements Runnable {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	
	private String buffer;
	private Thread networkThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /** Called when the user clicks the Send button */
    public void startDiscovery(View view) {
    	// allow networking on main thread (temporary)
    	//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	//StrictMode.setThreadPolicy(policy); 
    	if (networkThread != null) networkThread.interrupt();
		networkThread = new Thread(this);
		networkThread.start();
    }

	@Override
	public void run() {
		try {
			int server_port = 12345;
			DatagramSocket s = new DatagramSocket();
	    	s.setBroadcast(true);
	    	s.setSoTimeout(200);
	    	int android_port = s.getLocalPort();
	    	
	    	JSONObject object = new JSONObject();
	    	try {
	    		object.put("ipaddress", getIPAddress());
	    		object.put("port", android_port);
	    	} catch (JSONException e) {
	    		Log.d("debug", e.toString());
	    	}
	    	
	    	// broadcast discovery packet using current network details
	    	InetAddress local = InetAddress.getByName("255.255.255.255");
	    	Log.d("debug", object.toString());
	    	Log.d("debug", local.getHostAddress().toString());
	    	
	    	int msg_length = object.toString().length();
	    	byte[] message = object.toString().getBytes();
	    	DatagramPacket p = new DatagramPacket(message, msg_length, local, server_port);
	    	s.send(p);
	    	s.close();
	    	
	    	addMessage("Starting Discovery");
	    	
	    	// receive packet
	    	byte[] buffer = new byte[255];
	    	DatagramSocket r = new DatagramSocket(android_port);
	        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	        r.receive(packet);
	        
	        InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(packet.getData()), Charset.forName("UTF-8"));

	        StringBuilder str = new StringBuilder();
	        int value;
	        while((value = input.read()) != -1)
	            str.append((char) value);
	        
	        Log.d("debug", str.toString());
	        
	        addMessage("Received: " + str);
	        
		} catch(SocketException e) {
			Log.d("debug", e.toString());
		} catch(IOException e) {
			Log.d("debug", e.toString());
		}
	}
    
    private void addMessage(String message) {
    	if (buffer == null)
    		buffer = message;
    	else
    		buffer += "\n" + message;
    	runOnUiThread(new Runnable() {

    	    @Override
    	    public void run() {
    	    	TextView text = (TextView) findViewById(R.id.text_output);
    	    	if (text != null) text.setText(buffer);
            }
	    });
    	
    }
    
    private String getIPAddress() throws IOException {
    	WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    	DhcpInfo myDhcpInfo = myWifiManager.getDhcpInfo();
    	if (myDhcpInfo == null) {
    		System.out.println("Could not get broadcast address");
    		return null;
    	}
    	int broadcast = myDhcpInfo.ipAddress;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
		quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads).getHostAddress();
    }
    
}