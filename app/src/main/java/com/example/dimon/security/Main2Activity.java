package com.example.dimon.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.utils.OpenClientUtil;

public class Main2Activity extends Activity {

    double mLat1 = 31.123;
    double mLon1 = 121.233;
    double mLat2 = 31.234;
    double mLon2 = 121.732;

    String data;
    double des_lat, des_lon;
    String location;
    String number;

    Button check;
    private SendStatusReceiver sendStatusReceiver;
    private IntentFilter sendFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        data = intent.getStringExtra("param");
        //Log.d("second", data);
        int i, j=0;
        for(i = 0; i < data.length(); i++)
        {
            if(data.charAt(i) == ' ')
            {
                location = data.substring(0, i);
                j = i;
            }
            if(data.charAt(i) == ',')
            {
                des_lat = Double.parseDouble(data.substring(j+1, i));
                j = i;
            }
            if(data.charAt(i) == '+')
            {
                des_lon = Double.parseDouble(data.substring(j+1, i));
                number = data.substring(i+1);
            }
        }

        sendFilter = new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        sendStatusReceiver = new SendStatusReceiver();
        registerReceiver(sendStatusReceiver, sendFilter);

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

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(sendStatusReceiver);
    }

    public void navi(View view)
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
