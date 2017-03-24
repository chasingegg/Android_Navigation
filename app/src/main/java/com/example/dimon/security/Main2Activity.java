package com.example.dimon.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.location.LocationManager;
import android.location.LocationListener;
import android.widget.Toast;
import android.content.Intent;

import java.io.File;
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

import static com.example.dimon.security.MainActivity.flag;
import static com.example.dimon.security.MainActivity.mLat1;
import static com.example.dimon.security.MainActivity.mLon1;

public class Main2Activity extends Activity {

    double mLat2;
    double mLon2;
    private TextView t1,t2,t3;
    String data;
    double des_lat, des_lon;
    String location;
    String number;
    String s0;
    Button check;
    Button recover;
    Button state;
    private SendStatusReceiver sendStatusReceiver;
    private IntentFilter sendFilter;
    private IntentFilter receiveFilter;

    private MessageReceiver messageReceiver;

    private MediaPlayer mediaPlayer1 = new MediaPlayer();
    private MediaPlayer mediaPlayer2 = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        t1= (TextView) findViewById(R.id.textView1);
        t2 = (TextView) findViewById(R.id.textView2);
        t3 = (TextView) findViewById(R.id.textView3);

        Intent intent = getIntent();
        data = intent.getStringExtra("param");
        int k=0;
        while (data.charAt(k)!=' ')
        {
            k=k+1;
        }

        s0 = data.substring(0, k);
        //Log.d("second", data);
        int i, j=0;
        for(i = 0; i < data.length(); i++)
        {
            if(data.charAt(i) == ' ')
            {
                //location = data.substring(0, i);
                j = i;
            }
            if(data.charAt(i) == ',')
            {
                des_lat = Double.parseDouble(data.substring(j+1, i));
                mLat2 = des_lat;
                j = i;
            }
            if(data.charAt(i) == '+')
            {
                des_lon = Double.parseDouble(data.substring(j+1, i));
                mLon2 = des_lon;
                number = data.substring(i+1);
            }
        }
        // receiveFilter = new IntentFilter();
        // receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //receiveFilter.setPriority(100);
        //messageReceiver = new Main2Activity.MessageReceiver();
        //registerReceiver(messageReceiver, receiveFilter);
        //ljh
        try {
            //File file = new File(Environment.getExternalStorageDirectory(), "Magic_Mullet.mp3");
            //mediaPlayer.setDataSource(file.getPath());
            mediaPlayer1 = MediaPlayer.create(Main2Activity.this, R.raw.safe);
            mediaPlayer2 = MediaPlayer.create(Main2Activity.this, R.raw.alarm);
            //mediaPlayer.prepare();

        } catch(Exception e) {
            e.printStackTrace();
        }


        //mediaPlayer.start();

        t1.setText(s0+"监测站");
        t2.setText("经纬度 \n"+des_lat+','+des_lon);
        t3.setText("号码 \n"+number);
        //hjl
        sendFilter = new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        sendStatusReceiver = new SendStatusReceiver();
        registerReceiver(sendStatusReceiver, sendFilter);
        //ljh
        receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(100);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, receiveFilter);
        //hjl
        check = (Button) findViewById(R.id.check);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();
                Intent sentIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent pi = PendingIntent.getBroadcast(Main2Activity.this, 0, sentIntent, 0);
                smsManager.sendTextMessage(number, null, "SILENCE", pi, null);

            }
        });
        //ljh
        recover = (Button) findViewById(R.id.recover);
        recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();
                Intent sentIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent pi = PendingIntent.getBroadcast(Main2Activity.this, 0, sentIntent, 0);
                smsManager.sendTextMessage(number, null, "GOON", pi, null);
            }
        });
        state = (Button) findViewById(R.id.state);
        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();
                Intent sentIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent pi = PendingIntent.getBroadcast(Main2Activity.this, 0, sentIntent, 0);
                smsManager.sendTextMessage(number, null, "STATUSCHECK", pi, null);
            }
        });
        //hjl
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(mediaPlayer1 != null) {
            mediaPlayer1.stop();
            mediaPlayer1.release();
        }
        if(mediaPlayer2 != null) {
            mediaPlayer2.stop();
            mediaPlayer2.release();
        }
        unregisterReceiver(sendStatusReceiver);
        unregisterReceiver(messageReceiver);
    }
    class MessageReceiver extends BroadcastReceiver {
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
            if(address.charAt(0) == '+')
                address = address.substring(3);
            if(address.charAt(0) == ' ')
                address = address.substring(1);

            //ljh

            if(address.equals(number)) {
                if (fullMessage.charAt(0) == 'R') {
                    mediaPlayer1.start();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Main2Activity.this);
                    dialog.setTitle(s0 + "监测站: 状态报告");
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
                if(fullMessage.charAt(0) == 'S' && fullMessage.charAt(1) == 'T' && fullMessage.charAt(7) == 'O') {
                    mediaPlayer1.start();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Main2Activity.this);
                    dialog.setTitle(s0 + "监测站: 状态报告");
                    dialog.setMessage("初始化成功！");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }
                    );
                    dialog.show();
                }
                if (fullMessage.charAt(0) == '#' && fullMessage.charAt(7) == '#' && fullMessage.charAt(1) == 'S') {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Main2Activity.this);
                    dialog.setTitle(s0 + "监测站: 状态报告");
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
                        mediaPlayer2.start();
                    }
                    else
                        mediaPlayer1.start();
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
                    mediaPlayer2.start();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Main2Activity.this);
                    dialog.setTitle(s0 + "监测站: 发现警情!");
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

                                        BaiduMapNavigation.openBaiduMapNavi(para, Main2Activity.this);

                                    } catch (BaiduMapAppNotSupportNaviException e) {
                                        e.printStackTrace();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                                        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
                                        builder.setTitle("提示");
                                        builder.setPositiveButton("确认", new OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                OpenClientUtil.getLatestBaiduMapApp(Main2Activity.this);
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
        //hjl
    }

    public void navi(View view) {
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

            BaiduMapNavigation.openBaiduMapNavi(para, this);

        } catch (BaiduMapAppNotSupportNaviException e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    OpenClientUtil.getLatestBaiduMapApp(Main2Activity.this);
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
    }


    class SendStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == RESULT_OK) {
                Toast.makeText(context, "短信发送成功", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(context, "短信发送失败", Toast.LENGTH_LONG)
                        .show();
            }
        }


    }
}
