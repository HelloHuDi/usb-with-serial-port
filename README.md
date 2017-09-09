## 使用kotlin编写，提供android系统下usb转串口及串口(UART,RS232)通信方式

## 参考串口通信源码： https://github.com/cepr/android-serialport-api

## 参考usb转串口源码：https://github.com/mik3y/usb-serial-for-android

## android studio 引入，注意：usbserialport只实现了简单的读写，更深度化的使用可查看 [usb-serial-port-measure](MEASURE.md)

```
dependencies {
      compile 'com.hd:usbserialport:0.2.0'
  }
```

## 用法：

### 1.在application 初始化：
```
public class AIOApp extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            DeviceMeasureController.INSTANCE.init(this,BuildConfig.DEBUG);
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


