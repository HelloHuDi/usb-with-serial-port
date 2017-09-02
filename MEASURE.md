### usb-serial-port-measure是对usbserialport的深度封装，力求用最少代码完成usb及串口设备的测量

## android studio 引入(默认依赖usbserialport)：

```
dependencies {
     compile 'com.hd:usb-serial-port-measure:0.1'
  }
```

## 用法：

### 1.在application 初始化：

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

### 2.开始测量
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
### 3.解析类必须继承Parser类

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

### 4.停止测量(测量错误及完成测量后默认停止)
```
    @Override
    protected void onPause() {
        super.onPause();
        L.INSTANCE.d("onPause");
        AIODeviceMeasure.INSTANCE.stopMeasure();
    }

```





