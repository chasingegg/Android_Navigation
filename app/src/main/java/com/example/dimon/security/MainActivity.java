package com.example.dimon.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    double mLat1;
    double mLon1;

    double mLat2;
    double mLon2;

    int flag = 0;
    private LocationManager locationManager;
    private String provider;
    private String name;

    private TextView sender;
    private String address;
    private TextView content;

    private TextView text;

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




        text = (TextView) findViewById(R.id.navi_info);


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




    public  void searchContacts(Context context){

        //Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, searchName);

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, address); //根据电话号码查找联系人

        String[] projection = new String[]{ContactsContract.Contacts._ID};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        String id = null;
        if (cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
        }
        cursor.close();
        if (id!=null) {
            String where = ContactsContract.Data._ID+"="+id;
            projection = new String[]{ContactsContract.Data.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor searchcCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, where, null, null);
            //Log.d(tag, searchcCursor.getCount()+"");
            int nameIndex = searchcCursor.getColumnIndex(projection[0]);
            int numberIndex = searchcCursor.getColumnIndex(projection[1]);
            while(searchcCursor.moveToNext()){
                name = searchcCursor.getString(nameIndex);
                String number = searchcCursor.getString(numberIndex);
                //Log.d(tag, number+":"+name);
                //sender.setText(name);
            }
            searchcCursor.close();
        }
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
            address = messages[0].getOriginatingAddress();
            String fullMessage = "";
            for (SmsMessage message : messages) {
                fullMessage += message.getMessageBody();
            }
            //sender.setText(address);
            //content.setText(fullMessage);
            abortBroadcast();

            searchContacts(context);


            int len = name.length();
            int i;
            String sub1, sub2;
            for(i = 0; i < len; i++)
            {
                if(name.charAt(i) == ',')
                    break;
            }
            sub1 = name.substring(0, i - 1);
            sub2 = name.substring(i + 1);

            mLat2 = Double.parseDouble(sub1);
            mLon2 = Double.parseDouble(sub2);

            text.setText(String.format("终点:(%f,%f)",
                    mLat2, mLon2));

            if(fullMessage.charAt(0) == '#' && fullMessage.charAt(6) == '#') {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("发现警情!");
                String s1 = "", s2="",s3="",s4="";

                if(fullMessage.charAt(19) == 't')
                   s1 = "门磁：有警情";
                else
                   s1 = "门磁：无警情";

                if(fullMessage.charAt(31) == 't')
                    s2 = "红外1：有警情";
                else
                    s2 = "红外1：无警情";


                if(fullMessage.charAt(43) == 't')
                    s3 = "红外2：有警情";
                else
                    s3 = "红外2：无警情";

                if(fullMessage.charAt(55) == 't')
                    s4 = "微波：有警情";
                else
                    s4 = "微波：无警情";


                dialog.setMessage(
                        s1 + "\n" + s2 + "\n" + s3 + "\n" + s4 + "\n是否导航到目标位置\n"
                );
                //dialog.setMessage(fullMessage);
                dialog.setCancelable(false);
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
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
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
            }
        }
    }

}