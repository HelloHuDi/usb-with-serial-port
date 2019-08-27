<p align="center">
	<img width="72" height="72" src="art/icon.png"/>
</p>
<h3 align="center">usb-serial-port-measure</h3>
<p align="center">
<a href="https://github.com/HelloHuDi/usb-with-serial-port/releases" target="_blank"><img src="https://img.shields.io/badge/release-v0.3.4-blue.svg"></img></a>
</p>

**usb-serial-port-measure是对usbserialport的深度封装，力求用最少代码完成usb及串口设备的测量**

**android studio 引入：**

```
dependencies { 
     implementation 'com.hd:usb-serial-port-measure:last-version@aar'
     implementation 'com.hd:usbserialport:last-version'
  }
```

**用法：**

**1.在application 初始化：**

```
public class AIOApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AIODeviceMeasure.INSTANCE.init(this, BuildConfig.DEBUG, new AIOComponent() {
            @Override
            public MeasureParameter getMeasureParameter(Context context, int type) {
                //根据类型返回测量参数 SerialPortMeasureParameter  or UsbMeasureParameter
                return null;
            }

            @Override
            public Parser getParser(int type) {
                //根据类型返回测量数据解析类
                return null;
            }

            @Override
            public UsbSerialPort getUsbSerialPort(Context context, int type) {
                //根据类型返回UsbSerialPort ,针对usb 设备
                return null;
            }

            @Override
            public String getSerialPortPath(Context context, int type) {
                //根据类型返回串口设备地址 ,针对串口设备
                return null;
            }

            @Override
            public List<byte[]> getInitializationInstructInstruct(int type) {
                //根据类型返回针对设备初始化指令
                return null;
            }

            @Override
            public List<byte[]> getReleaseInstruct(int type) {
                //根据类型返回针对设备结束指令
                return null;
            }
        });
    }
}

```

**2.开始测量**
```
    @Override
    protected void onResume() {
        super.onResume();
        L.INSTANCE.d("onResume");
        AIODeviceMeasure.INSTANCE.with(/*设备标示id，根据该标示查找解析类*/, new ReceiveResultListener() {
            @Override
            public void receive(@NonNull ParserResult parserResult) {
                L.INSTANCE.d("receive : "+parserResult.toString());
            }

            @Override
            public void error(@NonNull String msg) {
                L.INSTANCE.d("error : "+msg);
            }
        }).startMeasure();
    }

```

**3.解析类必须继承Parser类**
```
public class TestParser extends Parser {

    @Override
    public void asyncWrite() {
        super.asyncWrite();
        //处于while循环中
        getWriteComplete().set(true);//跳出循环
    }

    @Override
    public void parser(@NotNull byte[] data) {
        //实现解析
        complete();//完成测量
        error();//测量错误
    }
}

```

**4.停止测量(测量错误及完成测量后默认停止)**
```
    @Override
    protected void onPause() {
        super.onPause();
        L.INSTANCE.d("onPause");
        AIODeviceMeasure.INSTANCE.stopMeasure();
    }

```

**License**

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





