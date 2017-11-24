/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class SerialPortFinder {

	public class Driver {
		Driver(String name, String root) {
			mDriverName = name;
			mDeviceRoot = root;
		}
		private String mDriverName;
		private String mDeviceRoot;
		Vector<File> mDevices = null;
		Vector<File> getDevices() {
			if (mDevices == null) {
				mDevices = new Vector<>();
				String targetStr="/dev";
				File dev = new File(targetStr);
				File[] files = dev.listFiles();
				if (files != null) {
					Log.d("tag", "dev :" + dev + "==" + Arrays.toString(files));
					for (File file : files) {
						if (file.getAbsolutePath().startsWith(mDeviceRoot)) {
							Log.d(TAG, "Found new device: " + file);
							mDevices.add(file);
						}
					}
				} else if (mDeviceRoot.startsWith(targetStr)) {
					File file = new File(mDeviceRoot);
					Log.d(TAG, "Found new device: " + file);
					mDevices.add(file);
				}
			}
			return mDevices;
		}
		public String getName() {
			return mDriverName;
		}
	}

	private static final String TAG = "SerialPort";

	private Vector<Driver> mDrivers = null;

	private Vector<Driver> getDrivers() throws IOException {
		if (mDrivers == null) {
			mDrivers = new Vector<>();
			LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
			String l;
			while((l = r.readLine()) != null) {
				// Issue 3:
				// Since driver name may contain spaces, we do not extract driver name with split()
				String drivername = l.substring(0, 0x15).trim();
				String[] w = l.split(" +");
				Log.d("SerialPortFinder","drivername :"+drivername+"====="+l);
				if ((w.length >= 5) && (w[w.length-1].equals("serial"))) {
					Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length-4]);
					mDrivers.add(new Driver(drivername, w[w.length-4]));
				}
			}
			r.close();
		}
		return mDrivers;
	}

	private ConcurrentHashMap<String,String> findAllDevices(){
		ConcurrentHashMap<String,String> devices = new ConcurrentHashMap<>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv = getDrivers().iterator();
			while(itdriv.hasNext()) {
				Driver driver = itdriv.next();
				for (File file : driver.getDevices()) {
					String deviceName = file.getName();
					String devicePath = file.getAbsolutePath();
					deviceName = String.format("%s (%s)", deviceName, driver.getName());
					Log.d("SerialPortFinder", "findAllDevices device name : " + deviceName + " , device path : " + devicePath);
					devices.put(deviceName, devicePath);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices;
	}

	public ConcurrentHashMap<String,String> getAllDevices(){
		return findAllDevices();
	}

	public String[] getAllDevicesName() {
		ConcurrentHashMap<String,String>allDevices=getAllDevices();
		return allDevices.keySet().toArray(new String[allDevices.size()]);
	}

	public String[] getAllDevicesPath() {
		ConcurrentHashMap<String,String>allDevices=getAllDevices();
		return allDevices.values().toArray(new String[allDevices.size()]);
	}
}
