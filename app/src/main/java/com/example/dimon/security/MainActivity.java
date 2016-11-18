package com.example.dimon.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.Toast;
import android.content.Intent;

import java.net.URISyntaxException;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
//import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.navi.NaviParaOption;


import com.baidu.mapapi.utils.OpenClientUtil;

public class MainActivity extends Activity {

    // 天安门坐标
    double mLat1;
    double mLon1;
    // 百度大厦坐标
    double mLat2 = 30.056858;
    double mLon2 = 119.308194;

    int flag = 0;
    private LocationManager locationManager;
    private String provider;




    private TextView sender;

    private TextView content;

    private IntentFilter receiveFilter;

    private MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        //获取地图控件引用
        //mMapView = (MapView) findViewById(R.id.bmapView);


        sender = (TextView) findViewById(R.id.sender);
        content = (TextView) findViewById(R.id.content);




        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(100);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, receiveFilter);




        TextView text = (TextView) findViewById(R.id.navi_info);
        text.setText(String.format("终点:(%f,%f)",
                mLat2, mLon2));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String>  providerList = locationManager.getProviders(true);
        if(providerList.contains(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
        }
        else if(providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else {
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null) {
            mLat1 = location.getLatitude();
            mLon1 = location.getLongitude();
        }
        locationManager.requestLocationUpdates(provider,5000, 1, locationListener);
    }

    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        unregisterReceiver(messageReceiver);
    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLat1 = location.getLatitude();
            mLon1 = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            String address = messages[0].getOriginatingAddress();
            String fullMessage = "";
            for (SmsMessage message : messages) {
                fullMessage += message.getMessageBody();
            }
            sender.setText(address);
            //content.setText(fullMessage);
            abortBroadcast();

            if(messages[])
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("导航到这里去");
            dialog.setMessage(fullMessage);
            dialog.setCancelable(false);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                            //startActivity(intent);

                            double lat = mLat1;
                            double lon = mLon1;
                            LatLng pt1 = new LatLng(lat, lon);
                            lat = mLat2;
                            lon = mLon2;
                            LatLng pt2 = new LatLng(lat, lon);
                            // 构建 导航参数
                            NaviParaOption para = new NaviParaOption();
                            para.startPoint(pt1);
                            para.startName("从这里开始");
                            para.endPoint(pt2);
                            para.endName("到这里结束");

                            try {

                                BaiduMapNavigation.openBaiduMapNavi(para, MainActivity.this);

                            } catch (BaiduMapAppNotSupportNaviException e) {
                                e.printStackTrace();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认", new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        OpenClientUtil.getLatestBaiduMapApp(MainActivity.this);
                                    }
                                });

                                builder.setNegativeButton("取消", new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                builder.create().show();
                            }
                            //try {
                            //  Intent intent = Intent.getIntent("intent://map/direction?+"destination=latlng:"+lat+","+lon+);
                            // startActivity(intent);   //启动调用
                            //}
                            //catch (URISyntaxException e) {
                            //  Log.e("intent", e.getMessage());
                            // }
                        }
                    }
            );
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();

        }
    }




    /*
    public void startNavi(View view)
    {
        double lat = mLat1;
        double lon = mLon1;
        LatLng pt1 = new LatLng(lat, lon);
        lat = mLat2;
        lon = mLon2;
        LatLng pt2 = new LatLng(lat, lon);
        // 构建 导航参数
        NaviParaOption para = new NaviParaOption();
        para.startPoint(pt1);
        para.startName("从这里开始");
        para.endPoint(pt2);
        para.endName("到这里结束");

        try {

            BaiduMapNavigation.openBaiduMapNavi(para, MainActivity.this);

        } catch (BaiduMapAppNotSupportNaviException e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    OpenClientUtil.getLatestBaiduMapApp(MainActivity.this);
                }
            });

            builder.setNegativeButton("取消", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
    }
    */
    /*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    } */
    }