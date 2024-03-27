package bk.test.rxbletestsample.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;

import bk.test.rxbletestsample.R;
import bk.test.rxbletestsample.databinding.ActivityMainBinding;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding binding;
    private PermissionSupport permissionSupport;
    private RxBleClient rxBleClient;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable scanDisposable;
    private Disposable connectDisposable;
    private RxBleConnection rxBleConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        permissionCheck();
        bindClickListener();
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();

        if (ID == binding.mainActivityStartScanBT.getId()) {//start scan
            startScan();
        } else if (ID == binding.mainActivityStopScanBT.getId()) {//stop scan
            stopScan();
        } else if (ID == binding.mainActivityDisconnectBT.getId()) {//disconnect device
            disconnectDevice();
        }else if(ID == binding.mainActivityDeleteLogBT.getId()){//delete log
            binding.mainActivityLogZoneET.getText().clear();
        }
    }

    private void connectToDevice(RxBleDevice device) {
        connectDisposable = device.establishConnection(false) // autoConnect를 false로 설정하여 즉시 연결을 시도합니다.
                .subscribe(
                        rxBleConnection -> {
                            addLogText("CONNECT_SUCCESS // " + device.getBluetoothDevice().getAddress());
                            this.rxBleConnection = rxBleConnection;
                        },
                        throwable -> {
                            addLogText("CONNECT_FAIL // " + throwable.getMessage());
                        }
                );

        // Disposable을 compositeDisposable에 추가
        compositeDisposable.add(connectDisposable);
    }

    private void disconnectDevice(){
        connectDisposable.dispose();
        compositeDisposable.clear();

        rxBleClient = null;
        rxBleConnection = null;

    }

    private void startScan(){

        if(rxBleClient == null){
            initClient();
        }

        addLogText("SCAN_START");
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        ScanFilter scanFilter = new ScanFilter.Builder().build();
        // BLE 장치 스캔 시작
        scanDisposable = rxBleClient.scanBleDevices(scanSettings,scanFilter)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(scanResult -> {//filter with manufacturerSpecificData
                    byte[] manufacturerSpecificData = scanResult.getScanRecord().getManufacturerSpecificData(0xffff);

                    if (manufacturerSpecificData != null) {
                        String manufacturerSpecificDataName = new String(manufacturerSpecificData);
                        return manufacturerSpecificDataName.equals("REMEDY");
                    } else {
                        return false; // Skip devices with null manufacturerSpecificData
                    }
                })
                .subscribe(
                        scanResult -> {//connect with first filtered device
                            addLogText(scanResult.getBleDevice().getMacAddress());
                            stopScan();
                            connectToDevice(scanResult.getBleDevice());


                        },
                        throwable -> {//error
                            addLogText("SCAN_ERROR // " + throwable.getMessage());
                        }
                );

        // Disposable을 compositeDisposable에 추가
        compositeDisposable.add(scanDisposable);
    }

    private void stopScan(){
        scanDisposable.dispose();
    }

    private void bindClickListener() {
        binding.mainActivityStartScanBT.setOnClickListener(this);
        binding.mainActivityStopScanBT.setOnClickListener(this);
        binding.mainActivityDisconnectBT.setOnClickListener(this);
        binding.mainActivityDeleteLogBT.setOnClickListener(this);
    }

    private void initClient() {
        if (this.rxBleClient == null) {
            rxBleClient = RxBleClient.create(this);
            addLogText("RXBLE_CLIENT_INIT");
        }
    }

    private void addLogText(String text){
        binding.mainActivityLogZoneET.append(text + "\n");
    }

    private void permissionCheck() {
        permissionSupport = new PermissionSupport(MainActivity.this, this);

        switch (permissionSupport.checkPermission()) {
            case PackageManager.PERMISSION_GRANTED:
                initClient();
                break;
            case PackageManager.PERMISSION_DENIED:
                permissionSupport.requestPermission();
                break;
            case PermissionSupport.PERMISSION_SELF_DENIED:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setTitle("permission_fail");
                builder.setMessage("permission_fail");
                builder.setCancelable(false);
                builder.setPositiveButton("exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });
                builder.show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionSupport != null) {
            if (permissionSupport.permissionResult(requestCode, permissions, grantResults)) {
                if (requestCode == 123) {
                    permissionCheck();
                }
            }
        }
    }

}