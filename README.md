<p align="center">
	<img width="72" height="72" src="art/icon.png"/>
</p>
<h3 align="center">usbserialport</h3>
<p align="center">
<a href="https://github.com/HelloHuDi/usb-with-serial-port/releases" target="_blank"><img src="https://img.shields.io/badge/release-v0.2.5-blue.svg"></img></a>
</p>

## 提供android系统下usb转串口及串口(UART,RS232)通信方式

## [参考串口通信源码](https://github.com/cepr/android-serialport-api)
## [参考usb转串口源码](https://github.com/mik3y/usb-serial-for-android)
## [usb转串口及串口设备测试工具工程](https://github.com/HelloHuDi/usbSerialPortTools)
## [测试工具apk下载](https://raw.githubusercontent.com/HelloHuDi/usbSerialPortTools/master/app-release.apk)

## android studio 添加

```
dependencies {
      implementation 'com.hd:usbserialport:last-version'
  }
```
## 注意：usbserialport只实现了简单的读写，更深度化的使用可查看 [usb-serial-port-measure](MEASURE.md)

## 用法：

### 1.在application 初始化：
```
public class AIOApp extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            DeviceMeasureController.init(this,BuildConfig.DEBUG);
        }
  }
```
### 2.扫描设备
```
  DeviceMeasureController.INSTANCE.scanSerialPort();//扫描串口
  
  DeviceMeasureController.INSTANCE.scanUsbPort();//扫描usb转串口
```

### 3.开始测量

#### 3.1 usb转串口测量
```
//测量所有设备
  @Override
     protected void onResume() {
         super.onResume();
         DeviceMeasureController.INSTANCE.measure(DeviceMeasureController.INSTANCE.scanUsbPort(),new UsbMeasureParameter(), new UsbMeasureListener() {
             @Override
             public void measuring(UsbSerialPort usbSerialPort, byte[] data) {
                 //处理返回数据
             }
 
             @Override
             public void write(UsbSerialPort usbSerialPort) {
                 //允许持续性写入数据
                 try {
                     usbSerialPort.write(new byte[]{(byte) 0xff, (byte) 0xff},1000);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
 
             @Override
             public void measureError(String message) {
                
             }
         });
     }
```
```
   //测量单个目标设备 
    @Override
    protected void onResume() {
        super.onResume();
        //根据实际选择情况
        UsbSerialPort port=DeviceMeasureController.INSTANCE.scanUsbPort().get(0).getPorts().get(0);
        DeviceMeasureController.INSTANCE.measure(port, new UsbMeasureParameter(115200,8,1,0), new UsbMeasureListener() {
            @Override
            public void measuring(UsbSerialPort usbSerialPort, byte[] data) {
                //处理返回数据
            }

            @Override
            public void write(UsbSerialPort usbSerialPort) {
                //允许持续性写入数据
                try {
                    usbSerialPort.write(new byte[]{(byte) 0xff, (byte) 0xff},1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void measureError(String message) {
                
            }
        });
    } 

```

#### 3.2 串口测量
```
//测量所有设备
   @Override
    protected void onResume() {
        super.onResume();
        DeviceMeasureController.INSTANCE.measure(null, new SerialPortMeasureParameter(), new SerialPortMeasureListener() {
            @Override
            public void measuring(String path, byte[] data) {
                //处理返回数据
            }

            @Override
            public void write(OutputStream outputStream) {
                //允许持续性写入数据
                try {
                    outputStream.write(new byte[]{(byte) 0xff, (byte) 0xff});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
                
            @Override
            public void measureError(String message) {

            }
        });
    }
```
```
   //测量单个目标设备 
     @Override
        protected void onResume() {
            super.onResume();
            String targetDevicePath="/dev/ttyS3";
            DeviceMeasureController.INSTANCE.measure(new SerialPortMeasureParameter(targetDevicePath,115200,0), new SerialPortMeasureListener() {
                @Override
                public void measuring(String path, byte[] data) {
                    //处理返回数据
                }

                @Override
                public void write(OutputStream outputStream) {
                    //允许持续性写入数据
                    try {
                        outputStream.write(new byte[]{(byte) 0xff, (byte) 0xff});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                @Override
                public void measureError(String message) {
    
                }
            });
        }


```
### 3.测量阶段发送指令
```
DeviceMeasureController.INSTANCE.write()
```

### 4.停止测量
```
DeviceMeasureController.INSTANCE.stop()
```

### License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

