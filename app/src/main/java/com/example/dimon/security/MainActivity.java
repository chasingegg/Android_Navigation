package com.example.dimon.security;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.Toast;
import android.content.Intent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
//import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.navi.NaviParaOption;


import com.baidu.mapapi.utils.OpenClientUtil;


public class MainActivity extends Activity {

    static double mLat1;
    static double mLon1;

    double mLat2;
    double mLon2;

    static int flag = 0;
    private LocationManager locationManager;
    private String provider;
    private String name;

    private TextView sender;
    private TextView content;

    private TextView text;

    private IntentFilter receiveFilter;

    private MessageReceiver messageReceiver;

    private MediaPlayer mediaPlayer_ala = new MediaPlayer();
    private MediaPlayer mediaPlayer_zhen = new MediaPlayer();
    ListView contactsView;

    ArrayAdapter<String> adapter;

    List<String> contactsList = new ArrayList<String>();
    List<String> tempList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        sender = (TextView) findViewById(R.id.sender);
        //content = (TextView) findViewById(R.id.content);


        contactsView = (ListView) findViewById(R.id.contacts_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsView.setAdapter(adapter);
        readContacts();

        contactsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                String str = tempList.get(position);
                //Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                //String pos = Integer.toString(position);
                //Log.d("dasd", pos);
                intent.putExtra("param", str);

                startActivity(intent);
            }
        });

        try {
            //File file = new File(Environment.getExternalStorageDirectory(), "Magic_Mullet.mp3");
            //mediaPlayer.setDataSource(file.getPath());
            mediaPlayer_ala = MediaPlayer.create(MainActivity.this, R.raw.alarm2);
            mediaPlayer_zhen = MediaPlayer.create(MainActivity.this, R.raw.safe);
            //mediaPlayer.prepare();

        } catch(Exception e) {
            e.printStackTrace();
        }

        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(100);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, receiveFilter);

        text = (TextView) findViewById(R.id.navi_info);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            mLat1 = location.getLatitude();
            mLon1 = location.getLongitude();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
    }

    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer_ala != null) {
            mediaPlayer_ala.stop();
            mediaPlayer_ala.release();
        }
        if(mediaPlayer_zhen != null) {
            mediaPlayer_zhen.stop();
            mediaPlayer_zhen.release();
        }
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListener);
        }
        unregisterReceiver(messageReceiver);
    }



    private void readContacts() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            while (cursor.moveToNext()) {
                String temp = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                number = number.substring(0, 3) + number.substring(4, 8) + number.substring(9, 13);
                //contactsList.add("监测站" +  Integer.toString(n) + "   " + displayName + "   " + number);
                int len=temp.length();
                int i,j;
                for(i=0;i<len;i++){
                    if(temp.charAt(i)==',')
                        break;
                }
                for(j=i;j<len;j++){
                    if(temp.charAt(j)=='@')
                        break;
                }
                String displayName=temp.substring(0,j);
                String location=temp.substring(j+1);
                contactsList.add(location+"监测站"  + "   " + displayName + "   " + "正常");
                tempList.add(location + " " + displayName + "+" + number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /*
         * 根据电话号码取得联系人姓名
         */

    private void search(String address) {
        Cursor cursor = null;
        //int n = 0;
        try {
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                String number = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String temp;
                temp = number.substring(0, 3) + number.substring(4, 8) + number.substring(9, 13);

                if(address.equals(temp)) {

                    name = displayName;
                    sender.setText(temp + " " + name);
                    //sender.setText(name);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
            String address = messages[0].getOriginatingAddress();
            String fullMessage = "";
            for (SmsMessage message : messages) {
                fullMessage += message.getMessageBody();
            }
            //sender.setText(address);
            //content.setText(fullMessage);
            //abortBroadcast();

           // searchContacts(context);
            if(address.charAt(0) == '+')
                address = address.substring(3);
            if(address.charAt(0) == ' ')
                address = address.substring(1);

            search(address);


            int len = name.length();
            int i,j;
            //int len1 = 1, int len2 = 1;
            String sub1, sub2, location;
            for(i = 0; i < len; i++)
            {
                if(name.charAt(i) == ',')
                    break;
            }
            for(j=i;j<len;j++)
            {
                if(name.charAt(j)=='@')
                    break;
            }
            sub1 = name.substring(0, i);
            sub2 = name.substring(i+1,j);
            location = name.substring(j+1);

            mLat2 = Double.parseDouble(sub1);
            mLon2 = Double.parseDouble(sub2);

            text.setText(String.format("终点:(%f,%f)",
                    mLat2, mLon2));

            if(fullMessage.charAt(0) == 'S' && fullMessage.charAt(1) == 'T' && fullMessage.charAt(7) == 'O') {
                mediaPlayer_zhen.start();
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(location + "监测站: 状态报告");
                dialog.setMessage("初始化成功！");
                dialog.setCancelable(false);
                dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );
                dialog.show();
            }
            if (fullMessage.charAt(0) == 'R') {
                mediaPlayer_zhen.start();
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(location + "监测站: 状态报告");
                String s1 = "收到", s2 = "";

                if (fullMessage.charAt(9) == 'S')

                    s2 = "检修命令";

                else

                    s2 = "恢复命令";

                dialog.setMessage(
                        s1 + s2
                );


                //dialog.setMessage(fullMessage);
                dialog.setCancelable(false);
                dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );

                dialog.show();
            }
            if (fullMessage.charAt(0) == '#' && fullMessage.charAt(7) == '#' && fullMessage.charAt(1) == 'S') {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(location + "监测站: 状态报告");
                String s1 = "", s2 = "",s3="",s4="";
                int flag = 0;
                if (fullMessage.charAt(21) == 't') {
                    s1 = "门磁1：有警情";
                    flag = 1;
                }
                else
                    s1 = "门磁1：无警情";

                if (fullMessage.charAt(35) == 't') {
                    s2 = "门磁2：有警情";
                    flag = 1;
                }
                else
                    s2 = "门磁2：无警情";

                if(fullMessage.charAt(46) == 't') {
                    s3 = "红外传感器：有警情";
                    flag = 1;
                }
                else
                    s3 = "红外传感器：无警情";
                if(fullMessage.charAt(58) == 't') {
                    s4 = "微波人体检测：有警情";
                    flag = 1;
                }
                else
                    s4 = "微波人体检测：无警情";
                if(flag == 1)
                {
                    mediaPlayer_ala.start();
                }
                else
                    mediaPlayer_zhen.start();
                dialog.setMessage(
                        s1 + "\n" + s2 + "\n" + s3 + '\n' + s4
                );
                dialog.setCancelable(false);
                dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }
                );

                dialog.show();
            }
            if(fullMessage.charAt(0) == '#' && fullMessage.charAt(6) == '#' && fullMessage.charAt(1) == 'A') {
                mediaPlayer_ala.start();
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle(location + "监测站: 发现警情!");
                String s1 = "", s2="", s3="", s4="";

                if(fullMessage.charAt(20) == 't')
                   s1 = "门磁1：有警情";
                else
                   s1 = "门磁1：无警情";

                if(fullMessage.charAt(34) == 't')
                    s2 = "门磁2：有警情";
                else
                    s2 = "门磁2：无警情";

                if(fullMessage.charAt(45) == 't')
                    s3 = "红外传感器：有警情";
                else
                    s3 = "红外传感器：无警情";
                if(fullMessage.charAt(57) == 't')
                    s4 = "微波人体检测：有警情";
                else
                    s4 = "微波人体检测：无警情";
                dialog.setMessage(
                        s1 + "\n" + s2 + "\n" + s3 + "\n" + s4 + "\n\n"+ "\n是否导航到目标位置\n"
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