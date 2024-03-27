package bk.test.rxbletestsample.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;

public class PermissionSupport {

    private final Context context;
    private final Activity activity;

    private List permissionList;

    public static final int PERMISSION_SELF_DENIED = 99;

    private final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH
//            Manifest.permission.BLUETOOTH_SCAN
//            Manifest.permission.BLUETOOTH_ADMIN
    };

    @RequiresApi(Build.VERSION_CODES.S)
    private final String[] permissions31 = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private final String[] permissions33 = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADMIN
    };




    public PermissionSupport(Activity mActivity, Context mContext) {
        this.activity = mActivity;
        this.context = mContext;
    }

    //남은 권한 체크
    public int checkPermission() {
        int result;
        permissionList = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            for (String pm : permissions33) {
                result = ContextCompat.checkSelfPermission(context, pm);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,pm)) {//직접거절
                        return PERMISSION_SELF_DENIED;
                    }else {
                        permissionList.add(pm);
                    }
                }

            }
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            for (String pm : permissions31) {
                result = ContextCompat.checkSelfPermission(context, pm);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,pm)) {//직접거절
                        return PERMISSION_SELF_DENIED;
                    }else {
                        permissionList.add(pm);
                    }
                }

            }
        }else {
            for (String pm : permissions) {
                result = ContextCompat.checkSelfPermission(context, pm);
                if (result != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,pm)) {//직접거절
                        return PERMISSION_SELF_DENIED;
                    }else {
                        permissionList.add(pm);
                    }

                }
            }
        }

        if(permissionList.isEmpty()){
            return PackageManager.PERMISSION_GRANTED;
        }else {
            return PackageManager.PERMISSION_DENIED;
        }

    }//check

    //권한 허용 요청
    public void requestPermission() {
        ActivityCompat.requestPermissions(activity, (String[]) permissionList.toArray(new String[0]), 123);
    }

    //결과처리
    public boolean permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 123 && (grantResults.length > 0)) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {

                    return false;
                }
            }
        }//for
        return true;
    }//permissionResult


}
