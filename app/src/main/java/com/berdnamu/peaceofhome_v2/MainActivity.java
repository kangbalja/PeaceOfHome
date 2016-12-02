package com.berdnamu.peaceofhome_v2;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    int streamId;

    EditText keywordView;

    TMapView tmapview;

    // 데이터베이스
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    // 출발지 위도, 경도
    double startLatitude;
    double startLongitude;

    // 목적지 위도, 경도
    double desLatitude;
    double desLongitude;

    // CCTV1, CCTV2 위도, 경도
    double cctv1Latitude;
    double cctv1Longitude;
    double cctv2Latitude;
    double cctv2Longitude;

    // SMS 퍼미션
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 스플래시
        startActivity(new Intent(this, Splash.class));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 타이틀 글자 숨기기
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 데이터베이스 헬퍼 호출
        myHelper = new myDBHelper(this);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationIcon(R.drawable.top_btn_bar);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Tmap 지도 시작

        tmapview = new TMapView(this);

        tmapview.setSKPMapApiKey("400ac9ff-ba53-3d7a-8578-58fd260a2571");

        // 현재위치로 표시될 좌표의 위도, 경도 설정 (금천구청역)
        tmapview.setLocationPoint(126.89436360000002, 37.4557965);

        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(15);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(false);
        tmapview.setTrackingMode(true);

        // 출발지, 도착지, 경유지 아이콘
        Bitmap start = BitmapFactory.decodeResource(this.getResources(),R.drawable.map_pin_start);
        Bitmap end = BitmapFactory.decodeResource(this.getResources(),R.drawable.map_pin_arrival);
        Bitmap pass = BitmapFactory.decodeResource(this.getResources(),R.drawable.map_pin_camera);
        tmapview.setTMapPathIcon(start, end, pass);

        // 현재위치 위도 경도 반환
        TMapPoint tpoint = tmapview.getLocationPoint();
        startLatitude = tpoint.getLatitude();
        startLongitude = tpoint.getLongitude();

        // 로그
        //Log.d("Latitude", Double.toString(startLatitude));
        //Log.d("Longtitude", Double.toString(startLongitude));

//        // 테스트 경로
//        TMapData tmapdata = new TMapData();
//        TMapPoint startpoint = new TMapPoint(37.5248, 126.93);
//        TMapPoint endpoint = new TMapPoint(37.4601, 128.0428);
//
//
//        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint,
//                new TMapData.FindPathDataListenerCallback() {
//                    @Override
//                    public void onFindPathData(TMapPolyLine polyLine) {
//                        tmapview.addTMapPath(polyLine);
//                    }
//                });

//        // 지도에 서클 추가
//        TMapCircle tcircle = new TMapCircle();
//
//        tcircle.setRadius(300);
//        tcircle.setLineColor(Color.BLUE);
//        tcircle.setAreaAlpha(50);
//        tcircle.setCircleWidth((float)10);
//        tcircle.setRadiusVisible(true);
//
//        TMapPoint startpoint = new TMapPoint(37.5248, 126.93);
//        tcircle.setCenterPoint(startpoint);
//
//        tmapview.addTMapCircle("TestID", tcircle);

        // 지도에 라인을 추가한다.
        //TMapPolyLine polyLine = new TMapPolyLine();
        //polyLine.setLineColor(Color.RED);
        //polyLine.setLineWidth(5);

//        TMapPoint startpoint = new TMapPoint(37.4557965, 126.89436360000002);
//        TMapPoint endpoint = new TMapPoint(37.452862, 126.898575);
//
//        // 경유지
//        TMapPoint point1 = new TMapPoint(37.45712, 126.898581);
//        TMapPoint point2 = new TMapPoint(37.455578, 126.900544);
//        TMapPoint point3 = new TMapPoint(37.453722, 126.899976);
//        //TMapPoint point4 = new TMapPoint(37.541080713272095, 126.99874675273895);
//
//        ArrayList<TMapPoint> passList = new ArrayList<TMapPoint>();
//
//        passList.add(point1);
//        passList.add(point2);
//        passList.add(point3);
//        //passList.add(point4);
//
//        TMapData tmapdata = new TMapData();
//
//        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint, passList, 0,new TMapData.FindPathDataListenerCallback() {
//            @Override
//            public void onFindPathData(TMapPolyLine tMapPolyLine) {
//                tMapPolyLine.setLineColor(Color.RED);
//                tmapview.addTMapPath(tMapPolyLine);
//            }
//        });
        // 지도 경로 끝





        RelativeLayout container = (RelativeLayout) findViewById(R.id.map_view);

        container.addView(tmapview);

        // Tmap 지도 끝

        // 검색어 가져오기
        keywordView = (EditText) findViewById(R.id.edit_keyword);

        // 검색 버튼 (길찾기 시작)
        ImageButton search_btn = (ImageButton) findViewById(R.id.btn_search);
        search_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                final String searchData = keywordView.getText().toString();

                TMapData tmapdata_search = new TMapData();

                // 주소검색
                tmapdata_search.findAddressPOI(searchData, new TMapData.FindAddressPOIListenerCallback() {
                    @Override
                    public void onFindAddressPOI(ArrayList<TMapPOIItem> arrayList) {

                        // 1개 검색결과 사용
                        TMapPOIItem item = arrayList.get(0);

                        Log.d("POI Name : ", item.getPOIName().toString());
                        Log.d("Address: ", item.getPOIAddress().replace("null", ""));
                        Log.d("Point: ", item.getPOIPoint().toString());
                        desLatitude = item.getPOIPoint().getLatitude(); // 위도
                        desLongitude = item.getPOIPoint().getLongitude(); // 경도
                        Log.d("desLatitude : ", String.valueOf(item.getPOIPoint().getLatitude()));
                        Log.d("desLongitude : ", String.valueOf(item.getPOIPoint().getLongitude()));

//                        for (int i = 0; i < arrayList.size(); i++) {
//                            TMapPOIItem item = arrayList.get(i);
//
//                            Log.d("POI Name : ", item.getPOIName().toString());
//                            Log.d("Address: ", item.getPOIAddress().replace("null", ""));
//                            Log.d("Point: ", item.getPOIPoint().toString());
//                            item.getPOIPoint().getLatitude();
//                            item.getPOIPoint().getLongitude();
//                        }


                        // 출발지에서 가까운 CCTV 경유지로 추가 (cctv1)
                        sqlDB = myHelper.getReadableDatabase();
                        Cursor cursor;
                        cursor = sqlDB.rawQuery("SELECT *  FROM cctvTBL ORDER BY ABS(" + startLatitude + " - gc_mapx) + ABS(" + startLongitude + " - gc_mapy) ASC LIMIT 1;", null);

                        while (cursor.moveToNext()) {
                            Log.d("start cctv db no:", cursor.getString(0));
                            Log.d("start cctv db x:", cursor.getString(1));
                            Log.d("start cctv db title:", cursor.getString(2));
                            Log.d("start cctv db name:", cursor.getString(3));
                            Log.d("start cctv db desc:", cursor.getString(4));
                            Log.d("start cctv db y:", cursor.getString(5));
                            Log.d("start cctv db address:", cursor.getString(6));

                            cctv1Latitude = Double.valueOf(cursor.getString(1)).doubleValue();
                            cctv1Longitude = Double.valueOf(cursor.getString(5)).doubleValue();

                        }

                        cursor.close();
                        sqlDB.close();


                        // 목적지에서 가까운 CCTV 경유지로 추가 (cctv2)
                        sqlDB = myHelper.getReadableDatabase();
                        Cursor cursor_des;
                        cursor_des = sqlDB.rawQuery("SELECT *  FROM cctvTBL ORDER BY ABS(" + desLatitude + " - gc_mapx) + ABS(" + desLongitude + " - gc_mapy) ASC LIMIT 1;", null);

                        while (cursor_des.moveToNext()) {
                            Log.d("des cctv db no:", cursor_des.getString(0));
                            Log.d("des cctv db x:", cursor_des.getString(1));
                            Log.d("des cctv db title:", cursor_des.getString(2));
                            Log.d("des cctv db name:", cursor_des.getString(3));
                            Log.d("des cctv db desc:", cursor_des.getString(4));
                            Log.d("des cctv db y:", cursor_des.getString(5));
                            Log.d("des cctv db address:", cursor_des.getString(6));

                            cctv2Latitude = Double.valueOf(cursor_des.getString(1)).doubleValue();
                            cctv2Longitude = Double.valueOf(cursor_des.getString(5)).doubleValue();
                        }

                        cursor_des.close();
                        sqlDB.close();

                        // 보행 길찾기 시작
                        TMapPoint startpoint = new TMapPoint(startLatitude, startLongitude);
                        TMapPoint endpoint = new TMapPoint(desLatitude, desLongitude);

                        // 경유지
                        TMapPoint point1 = new TMapPoint(cctv1Latitude, cctv1Longitude);
                        TMapPoint point2 = new TMapPoint(cctv2Latitude, cctv2Longitude);

                        // 디버깅
                        Log.d("FIANL startLatitude : ", String.valueOf(startLatitude));
                        Log.d("FIANL startLongitude : ", String.valueOf(startLongitude));
                        Log.d("FIANL desLatitude : ", String.valueOf(desLatitude));
                        Log.d("FIANL desLongitude : ", String.valueOf(desLongitude));
                        Log.d("FIANL cctv1Latitude : ", String.valueOf(cctv1Latitude));
                        Log.d("FIANL cctv1Longitude : ", String.valueOf(cctv1Longitude));
                        Log.d("FIANL cctv2Latitude : ", String.valueOf(cctv2Latitude));
                        Log.d("FIANL cctv2Longitude : ", String.valueOf(cctv2Longitude));


//                        TMapPoint startpoint = new TMapPoint(37.4557965, 126.89436360000002);
//                        TMapPoint endpoint = new TMapPoint(37.452862, 126.898575);
//
//                        // 경유지
//                        TMapPoint point1 = new TMapPoint(37.45712, 126.898581);
//                        TMapPoint point2 = new TMapPoint(37.455578, 126.900544);

                        ArrayList<TMapPoint> passList = new ArrayList<TMapPoint>();

                        passList.add(point1);
                        passList.add(point2);

                        TMapData tmapdata = new TMapData();

                        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint, endpoint, passList, 0,new TMapData.FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                                tMapPolyLine.setLineColor(Color.RED);
                                tmapview.addTMapPath(tMapPolyLine);
                            }
                        });

                    }
                });



            }
        });

        // CCTV 위치정보 json 파싱 시작
        // 테이블 생성 (초기1번)
        sqlDB = myHelper.getWritableDatabase();
        myHelper.onUpgrade(sqlDB, 1, 2);
        sqlDB.close();

        sqlDB = myHelper.getWritableDatabase();
        //sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '1', '37.4759766', '독산로102길 125 영남초교', '어린이보호 CCTV', '관리번호: SN103', '126.90681789999996', '대한민국 서울특별시 금천구 독산로102길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '1','37.4759766','독산로102길 125 영남초교','어린이보호 CCTV','관리번호: SN103','126.90681789999996','대한민국 서울특별시 금천구 독산로102길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '2','37.470488','독산로78다길 53','어린이보호 CCTV','관리번호: SN104','126.906568','대한민국 서울특별시 금천구 독산로78다길 53');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '3','37.4682073','벚꽃로18길 33','어린이보호 CCTV','관리번호: SN105','126.89078949999998','대한민국 서울특별시 금천구 벚꽃로18길 33');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '4','37.46444','시흥대로104길 48','어린이보호 CCTV','관리번호: SN106','126.9003315','대한민국 서울특별시 금천구 시흥대로104길 48');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '5','37.4666305','독산로64길 107','어린이보호 CCTV','관리번호: SN107','126.90800779999995','대한민국 서울특별시 금천구 독산로64길 107');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '6','37.462335','독산로50길 60 흥일초교','어린이보호 CCTV','관리번호: SN108','126.90740000000005','대한민국 서울특별시 금천구 독산로50길 60');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '7','37.4599209','독산로36나길 7','어린이보호 CCTV','관리번호: SN109','126.90767019999998','대한민국 서울특별시 금천구 독산로36나길 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '8','37.4554309','금하로11길 40','어린이보호 CCTV','관리번호: SN110','126.90415710000002','대한민국 서울특별시 금천구 금하로11길 40');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '9','37.4537223','탑골로5길 52','어린이보호 CCTV','관리번호: SN111','126.91252889999998','대한민국 서울특별시 금천구 탑골로5길 52');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '10','37.4520329','탑골로35','어린이보호 CCTV','관리번호: SN112','126.91322130000003','대한민국 서울특별시 금천구 탑골로 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '11','37.4585946','한내로69-16','어린이보호 CCTV','관리번호: SN113','126.88835549999999','대한민국 서울특별시 금천구 한내로 69-16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '12','37.4477781','독산로2길 19 백산초교','어린이보호 CCTV','관리번호: SN114','126.90423599999997','대한민국 서울특별시 금천구 독산로2길 19');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '13','37.4493523','시흥대로47길 45 문일중고','어린이보호 CCTV','관리번호: SN115','126.89842309999994','대한민국 서울특별시 금천구 시흥대로47길 45');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '14','37.4503783','금하로773','어린이보호 CCTV','관리번호: SN116','126.9157351','대한민국 서울특별시 금천구 금하로 773');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '15','37.4482719','금하로30길 51','어린이보호 CCTV','관리번호: SN117','126.9164515','대한민국 서울특별시 금천구 금하로30길 51');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '16','37.4559011','시흥대로63길 72','자전거관리 CCTV','관리번호: BC101','126.89581559999999','대한민국 서울특별시 금천구 시흥대로63길 72');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '17','37.4235786','안양천 자전거보관소(육교)','자전거관리 CCTV','관리번호: BC102','126.89834240000004','대한민국 경기도 안양시 만안구 석수동 안양천교');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '18','37.4235786','안양천 화장실앞(육교)','자전거관리 CCTV','관리번호: BC103','126.89834240000004','대한민국 경기도 안양시 만안구 석수동 안양천교');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '19','37.4235786','안양천 육교','자전거관리 CCTV','관리번호: BC104','126.89834240000004','대한민국 경기도 안양시 만안구 석수동 안양천교');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '20','37.911063','가산로77 기업은행','주정차단속 CCTV','관리번호: N101','127.78140810000002','대한민국 서울특별시 금천구 가산로 77');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '21','37.8384424','가산로122 성지병원','주정차단속 CCTV','관리번호: N102','127.16704479999998','대한민국 서울특별시 금천구 가산로 122');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '22','37.4783093','가산로146  놀부집항아리갈비','주정차단속 CCTV','관리번호: N103','126.89102460000003','대한민국 서울특별시 금천구 가산로 146');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '23','37.4713269','가산로20 고기굽는마을','주정차단속 CCTV','관리번호: N104','126.90074040000002','대한민국 서울특별시 금천구 가산로 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '24','35.8284724','범안로1236 현대자동차독산점맞은편','주정차단속 CCTV','관리번호: N105','128.6552358','대한민국 서울특별시 금천구 범안로 1236');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '25','37.4766066','문성로48','주정차단속 CCTV','관리번호: N106','126.90895709999995','대한민국 서울특별시 금천구 문성로 48');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '26','37.479371','가산디지털2로 129','주정차단속 CCTV','관리번호: N107','126.87854709999999','대한민국 서울특별시 금천구 가산디지털2로 129');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '27','37.4596667','독산로139 반도책서점','주정차단속 CCTV','관리번호: N108','126.90474640000002','대한민국 서울특별시 금천구 독산로 139');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '28','37.464637','독산로199 선정형외과','주정차단속 CCTV','관리번호: N109','126.9025193','대한민국 서울특별시 금천구 독산로 199');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '29','37.4727845','독산로291 천지랜드한증막','주정차단속 CCTV','관리번호: N110','126.90303779999999','대한민국 서울특별시 금천구 독산로 291');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '30','37.4755163','가산로113 두산아파트','주정차단속 CCTV','관리번호: N111','126.89170360000003','대한민국 서울특별시 가산동 113 가산두산위브아파트');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '31','35.8284724','범안로1194 24시감자탕','주정차단속 CCTV','관리번호: N112','128.6552358','대한민국 서울특별시 금천구 범안로 1194');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '32','37.4564343','독산로31길 13','방범 CCTV','관리번호: T128','126.90431319999993','대한민국 서울특별시 금천구 독산로31길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '33','37.4781195','남부순환로1304-11','스쿨존 CCTV','관리번호: S101','126.89644870000006','대한민국 서울특별시 금천구 남부순환로 1304-11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '34','35.8284724','범안로11길 3 두산초교 독산역방향','스쿨존 CCTV','관리번호: S102','128.6552358','대한민국 서울특별시 금천구 범안로 11길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '35','37.4585946','한내로 69-16','스쿨존 CCTV','관리번호: S103','126.88835549999999','대한민국 서울특별시 금천구 한내로 69-16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '36','37.4655142','시흥대로104길 31','스쿨존 CCTV','관리번호: S104','126.90005689999998','대한민국 서울특별시 금천구 시흥대로104길 31');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '37','37.4671961','독산로54길 115','스쿨존 CCTV','관리번호: S105','126.90768760000003','대한민국 서울특별시 금천구 독산로54길 115');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '38','37.4759766','독산로102길 108 영남초교','스쿨존 CCTV','관리번호: S106','126.90681789999996','대한민국 서울특별시 금천구 독산로102길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '39','37.4740055','시흥대로146길 19','스쿨존 CCTV','관리번호: S107','126.89985100000001','대한민국 서울특별시 금천구 시흥대로146길 19');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '40','37.4717252','독산로78다길 89','스쿨존 CCTV','관리번호: S108','126.90832599999999','대한민국 서울특별시 금천구 독산로78다길 89');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '41','37.4717252','독산로78다길 89','스쿨존 CCTV','관리번호: S109','126.90832599999999','대한민국 서울특별시 금천구 독산로78다길 89');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '42','37.4493334','시흥대로47길 43-1 문백초교 정문','스쿨존 CCTV','관리번호: S110','126.90021630000001','대한민국 서울특별시 금천구 시흥대로47길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '43','37.4546436','독산로23길 26','스쿨존 CCTV','관리번호: S111','126.90495750000002','대한민국 서울특별시 금천구 독산로23길 26');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '44','37.4533858','탑골로3길 50','스쿨존 CCTV','관리번호: S112','126.91405510000004','대한민국 서울특별시 금천구 탑골로3길 50');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '45','37.4482719','금하로30길 51','스쿨존 CCTV','관리번호: S113','126.9164515','대한민국 서울특별시 금천구 금하로30길 51');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '46','37.4418157','시흥대로24길 57','스쿨존 CCTV','관리번호: S114','126.90830299999993','대한민국 서울특별시 금천구 시흥대로24길 57');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '47','37.4602724','독산로36길 74-9','스쿨존 CCTV','관리번호: S115','126.90837050000004','대한민국 서울특별시 금천구 독산로36길 74-9');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '48','37.4600314','독산로50다길 84','스쿨존 CCTV','관리번호: S116','126.90841799999998','대한민국 서울특별시 금천구 독산로50다길 84');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '49','37.4620543','독산로44다길 23','스쿨존 CCTV','관리번호: S117','126.90628279999998','대한민국 서울특별시 금천구 독산로44다길 23');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '50','37.462335','독산로50길 60 흥일초교','스쿨존 CCTV','관리번호: S118','126.90740000000005','대한민국 서울특별시 금천구 독산로50길 60');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '51','37.4540189','탑골로5길 53','스쿨존 CCTV','관리번호: S119','126.91215499999998','대한민국 서울특별시 금천구 탑골로5길 53');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '52','37.4468638','시흥대로40가길 14 백산초교 골목길','스쿨존 CCTV','관리번호: S120','126.9059436','대한민국 서울특별시 금천구 시흥대로40가길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '53','37.4769966','남부순환로112길 55','쓰레기무단투기 CCTV','관리번호: T101','126.89305530000001','대한민국 서울특별시 금천구 남부순환로112길 55');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '54','37.482905','벚꽃로46길 5','쓰레기무단투기 CCTV','관리번호: T102','126.88274000000001','대한민국 서울특별시 금천구 벚꽃로46길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '55','37.4643641','시흥대로101길 14-8','쓰레기무단투기 CCTV','관리번호: T103','126.89644509999994','대한민국 서울특별시 금천구 시흥대로101길 14-8');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '56','37.4750849','시흥대로149길 43','쓰레기무단투기 CCTV','관리번호: T104','126.89558999999997','대한민국 서울특별시 금천구 시흥대로149길 43');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '57','37.4638132','독산로53길 23','쓰레기무단투기 CCTV','관리번호: T105','126.90221029999998','대한민국 서울특별시 금천구 독산로53길 23');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '58','37.4604975','시흥대로84나길 5','쓰레기무단투기 CCTV','관리번호: T106','126.90173770000001','대한민국 서울특별시 금천구 시흥대로84나길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '59','37.4750744','독산로99길 35','쓰레기무단투기 CCTV','관리번호: T107','126.90133000000003','대한민국 서울특별시 금천구 독산로99길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '60','37.4744365','독산로 87길 46','쓰레기무단투기 CCTV','관리번호: T108','126.90269369999998','대한민국 서울특별시 금천구 독산로87길 46');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '61','37.4697069','시흥대로126길 20','쓰레기무단투기 CCTV','관리번호: T109','126.8999751','대한민국 서울특별시 금천구 시흥대로126길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '62','37.4679799','독산로70라길 17-4','쓰레기무단투기 CCTV','관리번호: T110','126.90504899999996','대한민국 서울특별시 금천구 독산로70라길 17-4');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '63','37.4726302','독산로86길 7','쓰레기무단투기 CCTV','관리번호: T111','126.90378510000005','대한민국 서울특별시 금천구 독산로86길 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '64','37.47933039999999','남부순환로126가길 19','쓰레기무단투기 CCTV','관리번호: T112','126.90710060000003','대한민국 서울특별시 금천구 남부순환로126가길 19');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '65','37.4525343','시흥대로56길 22','쓰레기무단투기 CCTV','관리번호: T113','126.90293129999998','대한민국 서울특별시 금천구 시흥대로56길 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '66','37.4517265','시흥대로54길 6','쓰레기무단투기 CCTV','관리번호: T114','126.9032618','대한민국 서울특별시 금천구 시흥대로54길 6');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '67','37.4521744','탑골로60','쓰레기무단투기 CCTV','관리번호: T115','126.91567750000001','대한민국 서울특별시 금천구 탑골로 60');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '68','37.4512434','금하로29길 30','쓰레기무단투기 CCTV','관리번호: T116','126.91300219999994','대한민국 서울특별시 금천구 금하로29길 30');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '69','37.4389068','시흥대로12길 28','쓰레기무단투기 CCTV','관리번호: T117','126.90505689999997','대한민국 서울특별시 금천구 시흥대로12길 28');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '70','37.4438489','시흥대로36길 40','쓰레기무단투기 CCTV','관리번호: T118','126.90581780000002','대한민국 서울특별시 금천구 시흥대로36길 40');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '71','37.4625002','독산로38길 81','쓰레기무단투기 CCTV','관리번호: T119','126.90500229999998','대한민국 서울특별시 금천구 독산로38길 81');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '72','37.4606444','독산로50마길 14-6','쓰레기무단투기 CCTV','관리번호: T120','126.91128470000001','대한민국 서울특별시 금천구 독산로50마길 14-6');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '73','37.4500322','독산로10길 32','쓰레기무단투기 CCTV','관리번호: T121','126.90761169999996','대한민국 서울특별시 금천구 독산로10길 32');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '74','37.4531922','탑골로5가길 5','쓰레기무단투기 CCTV','관리번호: T122','126.91181430000006','대한민국 서울특별시 금천구 탑골로5가길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '75','37.4571362','독산로41길 89','쓰레기무단투기 CCTV','관리번호: T123','126.90149640000004','대한민국 서울특별시 금천구 독산로41길 89');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '76','37.4540023','시흥대로61길 5 길목호프','쓰레기무단투기 CCTV','관리번호: T124','126.90022999999996','대한민국 서울특별시 금천구 시흥대로61길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '77','37.4503591','은행나무로10길 12-1','쓰레기무단투기 CCTV','관리번호: T129','126.90631970000004','대한민국 서울특별시 금천구 은행나무로10길 12-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '78','37.4620301','독산로50길 163-10 명성빌라 앞','쓰레기무단투기 CCTV','관리번호: T130','126.90929949999997','대한민국 서울특별시 금천구 독산로50길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '79','37.4372283','시흥대로6길 35-25','쓰레기무단투기 CCTV','관리번호: T131','126.90459320000002','대한민국 서울특별시 금천구 시흥대로6길 35-25');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '80','37.4505249','탑골로4길 67 탑골식당 앞','쓰레기무단투기 CCTV','관리번호: T132','126.91502200000002','대한민국 서울특별시 금천구 탑골로4길 67');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '81','37.4511413','시흥대로48길 13','쓰레기무단투기 CCTV','관리번호: T133','126.90350769999998','대한민국 서울특별시 금천구 시흥대로48길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '82','37.4685742','독산로73길 10-34','쓰레기무단투기 CCTV','관리번호: T134','126.90161280000006','대한민국 서울특별시 금천구 독산로73길 10-34');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '83','37.4645478','독산로62길 3','쓰레기무단투기 CCTV','관리번호: T136','126.90310840000006','대한민국 서울특별시 금천구 독산로62길 3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '84','37.47321520000001','시흥대로145길 11','쓰레기무단투기 CCTV','관리번호: T137','126.89578370000004','대한민국 서울특별시 금천구 시흥대로145길 11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '85','37.4727921','두산로9가길 19-4','쓰레기무단투기 CCTV','관리번호: T138','126.89159219999999','대한민국 서울특별시 금천구 두산로9가길 19-4');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '86','35.7083656','독산로54길 102-98 금천체육공원 관리사무소','공원방범 CCTV','관리번호: A101','128.25492629999996','대한민국 서울특별시 금천구 독산로54길 102-98');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '87','37.4632087','독산로54길 6','공원방범 CCTV','관리번호: A102','126.90726970000003','대한민국 서울특별시 금천구 독산로54길 6');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '88','36.4269583','무아래 어린이공원','공원방범 CCTV','관리번호: A103','127.38857250000001','대한민국 서울특별시 금천구 가산동 234-29');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '89','37.4755747','시흥대로149가길 6','공원방범 CCTV','관리번호: A104','126.89554769999995','대한민국 서울특별시 금천구 시흥대로149가길 6');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '90','37.3550352','파랑새 어린이공원','공원방범 CCTV','관리번호: A105','127.10640739999996','대한민국 서울특별시 금천구 독산동 1095-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '91','36.4269583','한내로 55 참새 어린이공원','공원방범 CCTV','관리번호: A106','127.38857250000001','대한민국 서울특별시 금천구 독산동 1088-2');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '92','37.463629','독산 근린공원','공원방범 CCTV','관리번호: A107','126.88420380000002','대한민국 서울특별시 금천구 독산동 1086 독산근린공원');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '93','37.4696846','무궁화 어린이공원','공원방범 CCTV','관리번호: A108','126.92350540000007','대한민국 서울특별시 관악구 신림동 무궁화어린이공원');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '94','37.462935','시흥대로90길 60-3 무지개 어린이공원','공원방범 CCTV','관리번호: A109','126.9009522','대한민국 서울특별시 금천구 독산동 시흥대로90길 60-3 무지개상상어린이공원');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '95','37.4601219','시흥대로86길 20','공원방범 CCTV','관리번호: A110','126.9002941','대한민국 서울특별시 금천구 시흥대로86길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '96','37.4442955','시흥대로36길 33','그린파킹 CCTV','관리번호: G129','126.90558750000002','대한민국 서울특별시 금천구 시흥대로36길 33');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '97','37.4432354','시흥대로36길 62','그린파킹 CCTV','관리번호: G130','126.9064234','대한민국 서울특별시 금천구 시흥대로36길 62');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '98','37.4423258','시흥대로28길 58','그린파킹 CCTV','관리번호: G131','126.9070491','대한민국 서울특별시 금천구 시흥대로28길 58');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '99','37.4412429','시흥대로24길 56','그린파킹 CCTV','관리번호: G132','126.90745700000002','대한민국 서울특별시 금천구 시흥대로24길 56');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '100','37.4436907','시흥대로36길 18-5','그린파킹 CCTV','관리번호: G134','126.90527989999998','대한민국 서울특별시 금천구 시흥대로36길 18-5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '101','37.4395852','시흥대로16길 20','그린파킹 CCTV','관리번호: G135','126.90534200000002','대한민국 서울특별시 금천구 시흥대로16길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '102','37.4427747','시흥대로32길 11','그린파킹 CCTV','관리번호: G136','126.90521979999994','대한민국 서울특별시 금천구 시흥대로32길 11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '103','37.4556845','독산로26길 14','그린파킹 CCTV','관리번호: G137','126.90634049999994','대한민국 서울특별시 금천구 독산로26길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '104','37.4580844','독산로32길 44','그린파킹 CCTV','관리번호: G138','126.90765510000005','대한민국 서울특별시 금천구 독산로32길 44');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '105','37.4726346','두산로9가길 19-3','그린파킹 CCTV','관리번호: G139','126.8914125','대한민국 서울특별시 금천구 두산로9가길 19-3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '106','37.4652099','독산로60길 40','그린파킹 CCTV','관리번호: G140','126.90503179999996','대한민국 서울특별시 금천구 독산로60길 40');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '107','37.4656845','독산로62길 43 주택앞','그린파킹 CCTV','관리번호: G141','126.90491070000007','대한민국 서울특별시 금천구 독산로62길 43');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '108','35.8284724','범안로 1144 독산역 자전거보관소','자전거도서관 CCTV','관리번호: BC105','128.6552358','대한민국 서울특별시 금천구 범안로 1144');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '109','37.4661275','금천구 범안로 1144','자전거도서관 CCTV','관리번호: BC106','126.88904539999998','대한민국 서울특별시 금천구 범안로 1144');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '110','37.4424324','시흥대로28길 15','방범 CCTV','관리번호: P274','126.90474340000003','대한민국 서울특별시 금천구 시흥대로28길 15');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '111','37.4436397','시흥대로36길 18-13','방범 CCTV','관리번호: P275','126.9057067','대한민국 서울특별시 금천구 시흥대로36길 18-13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '112','37.4652168','시흥대로105길 8','방범 CCTV','관리번호: P276','126.89686329999995','대한민국 서울특별시 금천구 시흥대로105길 8');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '113','37.4758971','시흥대로 473','방범 CCTV','관리번호: P277','126.89804609999999','대한민국 서울특별시 금천구 시흥대로 473');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '114','37.4818785','벚꽃로38길 5','방범 CCTV','관리번호: P278','126.88329909999993','대한민국 서울특별시 금천구 벚꽃로38길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '115','37.4780024','가산로 147-7','방범 CCTV','관리번호: P279','126.89039150000008','대한민국 서울특별시 금천구 가산로 147-7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '116','37.4736969','가산로5길 77','방범 CCTV','관리번호: P280','126.89044949999993','대한민국 서울특별시 금천구 가산로5길 77');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '117','37.4726618','가산로7길 103','방범 CCTV','관리번호: P281','126.88836420000007','대한민국 서울특별시 금천구 가산로7길 103');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '118','37.4728558','시흥대로 440','방범 CCTV','관리번호: P282-1','126.8988832','대한민국 서울특별시 금천구 시흥대로 440');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '119','37.4826143','벚꽃로44길 9','방범 CCTV','관리번호: T125','126.88316609999992','대한민국 서울특별시 금천구 벚꽃로44길 9');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '120','37.47648179999999','시흥대로151길 46','방범 CCTV','관리번호: T126','126.89658239999994','대한민국 서울특별시 금천구 시흥대로151길 46');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '121','37.4590357','독산로 107길 13 구룡공원 내','방범 CCTV','관리번호: T127','126.90422650000005','대한민국 서울특별시 금천구 독산로');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '122','37.4496839','독산로3길 19','방범 CCTV','관리번호: P160','126.90394939999999','대한민국 서울특별시 금천구 독산로3길 19');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '123','37.4471846','시흥대로39길 52-11','방범 CCTV','관리번호: P161','126.900488','대한민국 서울특별시 금천구 시흥대로39길 52-11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '124','37.4345646','시흥대로2길 32','방범 CCTV','관리번호: P162','126.9051541','대한민국 서울특별시 금천구 시흥대로2길 32');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '125','37.4350043','시흥대로2길 60','방범 CCTV','관리번호: P163','126.90651720000005','대한민국 서울특별시 금천구 시흥대로2길 60');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '126','37.4359959','시흥대로2나길 28','방범 CCTV','관리번호: P164','126.90529600000002','대한민국 서울특별시 금천구 시흥대로2나길 28');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '127','37.4527834','시흥대로56길 40 금빛공원 뒤','방범 CCTV','관리번호: P165','126.90384310000002','대한민국 서울특별시 금천구 시흥대로56길 40');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '128','37.4609919','독산로44길 9','방범 CCTV','관리번호: P166','126.90529790000005','대한민국 서울특별시 금천구 독산로44길 9');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '129','37.4645607','시흥대로104길 10','방범 CCTV','관리번호: P167','126.89829050000003','대한민국 서울특별시 금천구 시흥대로104길 10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '130','37.4539377','독산로24다길 44','방범 CCTV','관리번호: P168','126.9086208','대한민국 서울특별시 금천구 독산로24다길 44');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '131','37.4670965','시흥대로110길 21','방범 CCTV','관리번호: P169','126.89986750000003','대한민국 서울특별시 금천구 시흥대로110길 21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '132','37.4748541','독산로99길 7','방범 CCTV','관리번호: P170','126.90282550000006','대한민국 서울특별시 금천구 독산로99길 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '133','37.4506891','은행나무로21','방범 CCTV','관리번호: P171','126.90478350000001','대한민국 서울특별시 금천구 은행나무로 21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '134','37.4778365','독산로107길 13','방범 CCTV','관리번호: P172','126.90272540000001','대한민국 서울특별시 금천구 독산로107길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '135','37.4763121','시흥대로150길 56','방범 CCTV','관리번호: P173','126.90187489999994','대한민국 서울특별시 금천구 시흥대로150길 56');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '136','37.4740151','독산로93길 18','방범 CCTV','관리번호: P174','126.90223300000002','대한민국 서울특별시 금천구 독산로93길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '137','37.4713326','독산로281-24','방범 CCTV','관리번호: P175','126.90251190000003','대한민국 서울특별시 금천구 독산로 281-24');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '138','37.4773403','남부순환로126길 69','방범 CCTV','관리번호: P176','126.90761799999995','대한민국 서울특별시 금천구 남부순환로126길 69');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '139','37.477956','독산로106길 31','방범 CCTV','관리번호: P177','126.90560900000002','대한민국 서울특별시 금천구 독산로106길 31');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '140','37.4737131','독산로92길 16','방범 CCTV','관리번호: P178','126.90425540000001','대한민국 서울특별시 금천구 독산로92길 16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '141','37.4681147','독산동 971','공원방범 CCTV','관리번호: A122','126.89819369999998','대한민국 서울특별시 금천구 독산동');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '142','37.4675444','독산동 산193-3','공원방범 CCTV','관리번호: A123','126.9074564','대한민국 서울특별시 금천구 독산동 산193-3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '143','37.4577162','시흥동 871-24','공원방범 CCTV','관리번호: A124','126.90185410000003','대한민국 서울특별시 금천구 시흥동 871-24');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '144','37.4348163','시흥동 970-3','공원방범 CCTV','관리번호: A125','126.90489619999994','대한민국 서울특별시 금천구 시흥동 970-3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '145','37.4492857','시흥동 817','공원방범 CCTV','관리번호: A126','126.91046619999997','대한민국 서울특별시 금천구 시흥동');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '146','37.4481155','시흥동 925-32','공원방범 CCTV','관리번호: A127','126.9085675','대한민국 서울특별시 금천구 시흥동 925-32');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '147','37.4492857','시흥동 731','공원방범 CCTV','관리번호: A128','126.91046619999997','대한민국 서울특별시 금천구 시흥동');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '148','37.462974','시흥대로90길63','어린이보호 CCTV','관리번호: C123','126.90072029999999','대한민국 서울특별시 금천구 시흥대로90길 63');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '149','37.47749659999999','남부순환로128길71','어린이보호 CCTV','관리번호: C124','126.90893510000001','대한민국 서울특별시 금천구 남부순환로128길 71');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '150','37.472211','독산로84길27','방범 CCTV','관리번호: P283','126.90483019999999','대한민국 서울특별시 금천구 독산로84길 27');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '151','37.4708141','독산로80길23-4','방범 CCTV','관리번호: P284','126.90470359999995','대한민국 서울특별시 금천구 독산로80길 23-4');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '152','37.4456743','시흥대로 37길 10','방범 CCTV','관리번호: P285','126.90298959999995','대한민국 서울특별시 금천구 시흥대로37길 10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '153','37.4827926','가산동 28-2','공원방범 CCTV','관리번호: A120','126.88448070000004','대한민국 서울특별시 금천구 가산동 28-2');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '154','37.46563260000001','독산동 378-4','공원방범 CCTV','관리번호: A121','126.90630639999995','대한민국 서울특별시 금천구 독산동 378-4');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '155','37.4783869','독산로353 캐슬PC방','주정차단속 CCTV','관리번호: N113','126.90358679999997','대한민국 서울특별시 금천구 독산로 353');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '156','37.4508071','금하로717','주정차단속 CCTV','관리번호: N114','126.90965289999997','대한민국 서울특별시 금천구 금하로 717');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '157','37.4680578','독산로238-1','주정차단속 CCTV','관리번호: N115','126.90237100000001','대한민국 서울특별시 금천구 독산로 238-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '158','37.4525867','금하로668','주정차단속 CCTV','관리번호: N116','126.90430029999993','대한민국 서울특별시 금천구 금하로 668');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '159','37.4548885','금하로635','주정차단속 CCTV','관리번호: N117','126.90183780000006','대한민국 서울특별시 금천구 금하로 635');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '160','35.8284724','범안로1209 유창빌딩','주정차단속 CCTV','관리번호: N118','128.6552358','대한민국 서울특별시 금천구 범안로 1209');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '161','37.6031991','범안로1138 중앙하이츠아파트','주정차단속 CCTV','관리번호: N119','127.09795680000002','대한민국 서울특별시 중랑구 신내동 479 중앙하이츠아파트');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '162','37.4801705','가산디지털1로 165','주정차단속 CCTV','관리번호: N120','126.8811402','대한민국 서울특별시 금천구 가산디지털1로 165');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '163','37.4766347','디지털로10길 22','주정차단속 CCTV','관리번호: N121','126.88829179999993','대한민국 서울특별시 금천구 디지털로10길 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '164','37.4487897','독산로16 성광교회','주정차단속 CCTV','관리번호: N122','126.90478970000004','대한민국 서울특별시 금천구 독산로 16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '165','37.4515704','금하로694','주정차단속 CCTV','관리번호: N123','126.90713159999995','대한민국 서울특별시 금천구 금하로 694');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '166','37.4577933','독산로118 세광어린이집','주정차단속 CCTV','관리번호: N124','126.90550159999998','대한민국 서울특별시 금천구 독산로 118');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '167','37.47620819999999','문성로14-1','주정차단속 CCTV','관리번호: N125','126.90523989999997','대한민국 서울특별시 금천구 문성로 14-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '168','37.4767561','독산로335-1','주정차단속 CCTV','관리번호: N126','126.90340370000001','대한민국 서울특별시 금천구 독산로 335-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '169','37.4707734','가산로7 예수비전교회 부근','주정차단속 CCTV','관리번호: N127','126.90197060000002','대한민국 서울특별시 금천구 가산로 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '170','35.8284724','범안로1256 독산역길끝','주정차단속 CCTV','관리번호: N128','128.6552358','대한민국 서울특별시 금천구 범안로 1246');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '171','37.4509954','독산로41 은행나무길확장도로','주정차단속 CCTV','관리번호: N129','126.90575100000001','대한민국 서울특별시 금천구 독산로 41');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '172','37.4500189','은행나무로12','주정차단속 CCTV','관리번호: N130','126.90383489999999','대한민국 서울특별시 금천구 은행나무로 12');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '173','37.4787914','디지털로193','주정차단속 CCTV','관리번호: N131','126.88805300000001','대한민국 서울특별시 금천구 디지털로 193');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '174','37.4665624','독산로70길 9','그린파킹 CCTV','관리번호: G101','126.90231259999996','대한민국 서울특별시 금천구 독산로70길 9');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '175','37.4671168','독산로70가길 17','그린파킹 CCTV','관리번호: G102','126.90263679999998','대한민국 서울특별시 금천구 독산로70가길 17');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '176','37.46692549999999','독산로70길 23-7','그린파킹 CCTV','관리번호: G103','126.90298889999997','대한민국 서울특별시 금천구 독산로70길 23-7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '177','37.4690511','독산로74길 21-13','그린파킹 CCTV','관리번호: G104','126.90406570000004','대한민국 서울특별시 금천구 독산로74길 21-13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '178','37.4700658','독산로78다길 39','그린파킹 CCTV','관리번호: G105','126.9059241','대한민국 서울특별시 금천구 독산로78다길 39');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '179','37.4684445','독산로72길 11-16','그린파킹 CCTV','관리번호: G106','126.90306959999998','대한민국 서울특별시 금천구 독산로72길 11-16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '180','37.4685075','독산로74길 20','그린파킹 CCTV','관리번호: G107','126.90355019999992','대한민국 서울특별시 금천구 독산로74길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '181','37.46932899999999','독산로78다길 22','그린파킹 CCTV','관리번호: G108','126.90517180000006','대한민국 서울특별시 금천구 독산로78다길 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '182','37.470488','독산로78다길 53','그린파킹 CCTV','관리번호: G109','126.906568','대한민국 서울특별시 금천구 독산로78다길 53');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '183','37.4717135','독산로80길 11-21','그린파킹 CCTV','관리번호: G110','126.90375440000002','대한민국 서울특별시 금천구 독산로80길 11-21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '184','37.4715256','독산로82길 18','그린파킹 CCTV','관리번호: G111','126.90476330000001','대한민국 서울특별시 금천구 독산로82길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '185','37.4715913','독산로82길 8','그린파킹 CCTV','관리번호: G112','126.90417560000003','대한민국 서울특별시 금천구 독산로82길 8');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '186','37.4714574','독산로82길 28','그린파킹 CCTV','관리번호: G113','126.90538270000002','대한민국 서울특별시 금천구 독산로82길 28');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '187','37.4713566','독산로82길 42','그린파킹 CCTV','관리번호: G114','126.90617099999997','대한민국 서울특별시 금천구 독산로82길 42');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '188','37.4691401','독산로76길 10','그린파킹 CCTV','관리번호: G115','126.90326060000006','대한민국 서울특별시 금천구 독산로76길 10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '189','37.4652069','범안로12길 29','그린파킹 CCTV','관리번호: G116','126.8920445','대한민국 서울특별시 금천구 범안로12길 29');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '190','37.4654767','범안로12길 21-11','그린파킹 CCTV','관리번호: G117','126.89256950000003','대한민국 서울특별시 금천구 범안로12길 21-11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '191','37.4438016','시흥대로38길 4','그린파킹 CCTV','관리번호: G118','126.9048037','대한민국 서울특별시 금천구 시흥대로38길 4');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '192','37.4431668','시흥대로30길 19','그린파킹 CCTV','관리번호: G119','126.90445690000001','대한민국 서울특별시 금천구 시흥대로30길 19');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '193','37.4421056','시흥대로26길 11','그린파킹 CCTV','관리번호: G120','126.90458430000001','대한민국 서울특별시 금천구 시흥대로26길 11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '194','37.4412812','시흥대로20길 21','그린파킹 CCTV','관리번호: G121','126.90420080000001','대한민국 서울특별시 금천구 시흥대로20길 21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '195','37.4401783','시흥대로18길 10','그린파킹 CCTV','관리번호: G122','126.90410050000002','대한민국 서울특별시 금천구 시흥대로18길 10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '196','37.4391753','시흥대로12길 13','그린파킹 CCTV','관리번호: G123','126.90427669999997','대한민국 서울특별시 금천구 시흥대로12길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '197','37.4430697','시흥대로28길 35-16','그린파킹 CCTV','관리번호: G124','126.90613310000003','대한민국 서울특별시 금천구 시흥대로28길 35-16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '198','37.4416623','시흥대로26길 36','그린파킹 CCTV','관리번호: G125','126.90575579999995','대한민국 서울특별시 금천구 시흥대로26길 36');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '199','37.44075249999999','시흥대로22길 24','그린파킹 CCTV','관리번호: G126','126.9057838','대한민국 서울특별시 금천구 시흥대로22길 24');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '200','37.439802','시흥대로16길 30','그린파킹 CCTV','관리번호: G127','126.90588909999997','대한민국 서울특별시 금천구 시흥대로16길 30');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '201','37.4405504','시흥대로18길 39','그린파킹 CCTV','관리번호: G128','126.90585229999999','대한민국 서울특별시 금천구 시흥대로18길 39');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '202','37.5136206','푸른골 어린이공원','공원방범 CCTV','관리번호: A111','126.75196529999994','대한민국 서울특별시 금천구 독산동 1038-9');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '203','36.4269583','목화 어린이공원','공원방범 CCTV','관리번호: A112','127.38857250000001','대한민국 서울특별시 금천구 독산동');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '204','36.4269583','쌈지 어린이공원','공원방범 CCTV','관리번호: A113','127.38857250000001','대한민국 서울특별시 금천구 189-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '205','37.4588052','독산로39길 16','공원방범 CCTV','관리번호: A114','126.90424800000005','대한민국 서울특별시 금천구 독산로39길 16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '206','35.2010134','비둘기 어린이공원','공원방범 CCTV','관리번호: A115','126.89740140000003','대한민국 서울특별시 금천구 시흥동');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '207','37.44696','시흥대로38길 61 금천 폭포공원','공원방범 CCTV','관리번호: A116','126.90395519999993','대한민국 서울특별시 금천구 시흥대로38길 61');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '208','37.7510418','송록 어린이공원','공원방범 CCTV','관리번호: A117','127.07737120000001','대한민국 서울특별시 금천구 시흥동 791-35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '209','36.4269583','꾸러기 어린이공원','공원방범 CCTV','관리번호: A118','127.38857250000001','대한민국 서울특별시 금천구 시흥동');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '210','37.4797951','까치 어린이공원','공원방범 CCTV','관리번호: A119','126.95952929999998','대한민국 서울특별시 관악구 봉천동 까치어린이공원');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '211','37.479638','독산로108길 37','어린이보호 CCTV','관리번호: C101','126.90596660000005','대한민국 서울특별시 금천구 독산로108길 37');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '212','37.4642576','시흥대로98길 45 독산유치원 골목 끝','어린이보호 CCTV','관리번호: C102','126.9002878','대한민국 서울특별시 금천구 시흥대로98길 45');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '213','37.4755779','가산로111','어린이보호 CCTV','관리번호: C103','126.89290779999999','대한민국 서울특별시 금천구 가산로 111');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '214','37.4677401','정심 어린이집','어린이보호 CCTV','관리번호: C104','126.90728480000007','대한민국 서울특별시 금천구 독산4동 938-5 구립정심어린이집');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '215','37.4695115','독산로54길 251 개미 어린이집','어린이보호 CCTV','관리번호: C105','126.9071222','대한민국 서울특별시 금천구 독산로54길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '216','37.4467177','시흥대로40길 93 자연유아 어린이집','어린이보호 CCTV','관리번호: C106','126.90824539999994','대한민국 서울특별시 금천구 시흥대로40길 93');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '217','37.46045489999999','독산로47가길 42','어린이보호 CCTV','관리번호: C107','126.90210120000006','대한민국 서울특별시 금천구 독산로47가길 42');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '218','37.4757763','가산로9길 35 모아래 어린이집','어린이보호 CCTV','관리번호: C108','126.89023670000006','대한민국 서울특별시 금천구 가산로9길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '219','37.4488534','독산로1길 3 조형어린이집 골목끝','어린이보호 CCTV','관리번호: C109','126.90349249999997','대한민국 서울특별시 금천구 독산로1길 3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '220','37.4548284','금하로3길 32','어린이보호 CCTV','관리번호: C110','126.89760819999992','대한민국 서울특별시 금천구 금하로3길 32');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '221','35.8284724','범안로12길 63-3 기쁨 어린이집','어린이보호 CCTV','관리번호: C111','128.6552358','대한민국 서울특별시 금천구 범안로 12길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '222','37.4520105','금하로23길20','어린이보호 CCTV','관리번호: C112','126.90883410000003','대한민국 서울특별시 금천구 금하로23길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '223','37.4494847','시흥대로41길 59 양문어린이집 부근','어린이보호 CCTV','관리번호: C113','126.90142070000001','대한민국 서울특별시 금천구 시흥대로41길 59');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '224','37.4742758','독산로94가길 17','어린이보호 CCTV','관리번호: C114','126.90508150000005','대한민국 서울특별시 금천구 독산로94가길 17');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '225','37.4768604','시흥대로152길 35','어린이보호 CCTV','관리번호: C115','126.90095339999993','대한민국 서울특별시 금천구 시흥대로152길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '226','37.4572046','시흥대로72길 13 세나유치원','어린이보호 CCTV','관리번호: C116','126.90023289999999','대한민국 서울특별시 금천구 시흥대로72길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '227','37.4597922','시흥대로84길 12 맞은편','어린이보호 CCTV','관리번호: C117','126.90030230000002','대한민국 서울특별시 금천구 시흥대로84길 12');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '228','37.477391','시흥대로150길 79','어린이보호 CCTV','관리번호: C118','126.90249449999999','대한민국 서울특별시 금천구 시흥대로150길 79');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '229','37.4566304','시흥대로68길 36','어린이보호 CCTV','관리번호: C119','126.90175699999997','대한민국 서울특별시 금천구 시흥대로68길 36');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '230','37.4516123','탑골로10길','어린이보호 CCTV','관리번호: C120','126.91489109999997','대한민국 서울특별시 금천구 탑골로10길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '231','37.4402343','시흥대로12길 82 새길어린이집','어린이보호 CCTV','관리번호: C121','126.90742309999996','대한민국 서울특별시 금천구 시흥대로12길 82');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '232','37.4589501','독산로36길 14','어린이보호 CCTV','관리번호: C122','126.9061709','대한민국 서울특별시 금천구 독산로36길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '233','37.4773168','시흥대로153길 36','어린이보호 CCTV','관리번호: SN101','126.89655879999998','대한민국 서울특별시 금천구 시흥대로153길 36');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '234','37.4749231','시흥대로148길 22','어린이보호 CCTV','관리번호: SN102','126.90007390000005','대한민국 서울특별시 금천구 시흥대로148길 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '235','37.4724983','독산로54길 237','방범 CCTV','관리번호: P179','126.90608420000001','대한민국 서울특별시 금천구 독산로54길 237');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '236','37.4473602','독산로8길 67','방범 CCTV','관리번호: P268','126.9079524','대한민국 서울특별시 금천구 독산로8길 67');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '237','37.4522732','탑골로3길 5','방범 CCTV','관리번호: P269','126.91132360000006','대한민국 서울특별시 금천구 탑골로3길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '238','37.4522015','금하로14길 20-13','방범 CCTV','관리번호: P270','126.90367190000006','대한민국 서울특별시 금천구 금하로14길 20-13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '239','37.4556432','시흥대로62가길 21','방범 CCTV','관리번호: P271','126.90285649999998','대한민국 서울특별시 금천구 시흥대로62가길 21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '240','37.45193769999999','시흥대로50길 25-7','방범 CCTV','관리번호: P272','126.90349489999994','대한민국 서울특별시 금천구 시흥대로50길 25-7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '241','37.4571868','독산로33길 34-8','방범 CCTV','관리번호: P273','126.90335720000007','대한민국 서울특별시 금천구 독산로33길 34-8');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '242','37.466504','독산로64길 70','방범 CCTV','관리번호: P102','126.9059297','대한민국 서울특별시 금천구 독산로64길 70');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '243','37.46689','독산로68길 60','방범 CCTV','관리번호: P103','126.90499709999994','대한민국 서울특별시 금천구 독산로68길 60');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '244','37.46500510000001','독산로58길 42','방범 CCTV','관리번호: P104','126.90529249999997','대한민국 서울특별시 금천구 독산로58길 42');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '245','37.46470619999999','독산로60길 24','방범 CCTV','관리번호: P105','126.90425119999997','대한민국 서울특별시 금천구 독산로60길 24');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '246','37.463778','독산로56길 18','방범 CCTV','관리번호: P106','126.90423650000002','대한민국 서울특별시 금천구 독산로56길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '247','37.4660906','독산로68길 15','방범 CCTV','관리번호: P107','126.90264730000001','대한민국 서울특별시 금천구 독산로68길 15');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '248','37.4648116','독산로67길 19','방범 CCTV','관리번호: P108','126.90152769999997','대한민국 서울특별시 금천구 독산로67길 19');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '249','37.46361419999999','시흥대로94길 72','방범 CCTV','관리번호: P109','126.90134510000007','대한민국 서울특별시 금천구 시흥대로94길 72');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '250','37.4641416','시흥대로100길 35','방범 CCTV','관리번호: P110','126.8992518','대한민국 서울특별시 금천구 시흥대로100길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '251','37.4614375','시흥대로84길 59','방범 CCTV','관리번호: P111','126.90176350000001','대한민국 서울특별시 금천구 시흥대로84길 59');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '252','37.4611096','시흥대로88길 21','방범 CCTV','관리번호: P112','126.90021860000001','대한민국 서울특별시 금천구 시흥대로88길 21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '253','37.4622456','독산로50길 141','방범 CCTV','관리번호: P113','126.91196689999992','대한민국 서울특별시 금천구 독산로50길 141');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '254','37.46137239999999','독산로40길 78','방범 CCTV','관리번호: P114','126.90863590000003','대한민국 서울특별시 금천구 독산로40길 78');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '255','37.4615193','독산로44다길 9','방범 CCTV','관리번호: P115','126.90639680000004','대한민국 서울특별시 금천구 독산로44다길 9');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '256','37.4603945','독산로36길 53','방범 CCTV','관리번호: P116','126.90734799999995','대한민국 서울특별시 금천구 독산로36길 53');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '257','37.4607874','독산로50다길 32','방범 CCTV','관리번호: P117','126.91056830000002','대한민국 서울특별시 금천구 독산로50다길 32');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '258','37.4584004','독산로32가길 26','방범 CCTV','관리번호: P118','126.90665360000002','대한민국 서울특별시 금천구 독산로32가길 26');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '259','37.4586071','독산로32나길 36','방범 CCTV','관리번호: P119','126.90767129999995','대한민국 서울특별시 금천구 독산로32나길 36');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '260','37.4561463','독산로28길 22','방범 CCTV','관리번호: P120','126.90697580000005','대한민국 서울특별시 금천구 독산로28길 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '261','37.4549829','독산로24길 14','방범 CCTV','관리번호: P121','126.90666829999998','대한민국 서울특별시 금천구 독산로24길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '262','37.4554279','독산로24길 51','방범 CCTV','관리번호: P122','126.90890089999993','대한민국 서울특별시 금천구 독산로24길 51');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '263','37.4541995','독산로24다길 22','방범 CCTV','관리번호: P123','126.90741100000002','대한민국 서울특별시 금천구 독산로24다길 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '264','37.45291599999999','독산로20길 18','방범 CCTV','관리번호: P124','126.90794840000001','대한민국 서울특별시 금천구 독산로20길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '265','37.4532812','독산로22길 49','방범 CCTV','관리번호: P125','126.90895019999993','대한민국 서울특별시 금천구 독산로22길 49');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '266','37.4522943','금하로21길 17','방범 CCTV','관리번호: P126','126.90804449999996','대한민국 서울특별시 금천구 금하로21길 17');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '267','37.4521036','금하로23나길 33','방범 CCTV','관리번호: P127','126.91062529999999','대한민국 서울특별시 금천구 금하로23나길 33');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '268','37.45172489999999','금하로23가길 25','방범 CCTV','관리번호: P128','126.9101991','대한민국 서울특별시 금천구 금하로23가길 25');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '269','37.4529457','독산로22가길 6','방범 CCTV','관리번호: P129','126.91068429999996','대한민국 서울특별시 금천구 독산로22가길 6');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '270','37.4505761','은행나무로10길 5','방범 CCTV','관리번호: P130','126.90679460000001','대한민국 서울특별시 금천구 은행나무로10길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '271','37.4502134','독산로10길 37','방범 CCTV','관리번호: P131','126.90801149999993','대한민국 서울특별시 금천구 독산로10길 37');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '272','37.4500242','독산로10길 56','방범 CCTV','관리번호: P132','126.90898049999998','대한민국 서울특별시 금천구 독산로10길 56');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '273','37.4473174','금하로24길 82','방범 CCTV','관리번호: P133','126.90907629999992','대한민국 서울특별시 금천구 금하로24길 82');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '274','37.4487684','독산로8길 37','방범 CCTV','관리번호: P134','126.90745830000003','대한민국 서울특별시 금천구 독산로8길 37');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '275','37.4473174','금하로24길 82','방범 CCTV','관리번호: P135','126.90907629999992','대한민국 서울특별시 금천구 금하로24길 82');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '276','37.4473859','시흥대로40길 42','방범 CCTV','관리번호: P136','126.90596069999992','대한민국 서울특별시 금천구 시흥대로40길 42');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '277','37.4845446','벚꽃로56길 30','방범 CCTV','관리번호: P137','126.88242839999998','대한민국 서울특별시 금천구 벚꽃로56길 30');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '278','37.47182069999999','가산로3길 113','방범 CCTV','관리번호: P138','126.88937609999993','대한민국 서울특별시 금천구 가산로3길 113');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '279','37.4656065','범안로12가길 20','방범 CCTV','관리번호: P139','126.89266480000003','대한민국 서울특별시 금천구 범안로12가길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '280','37.4704649','독산로80길 35','방범 CCTV','관리번호: P140','126.90494160000003','대한민국 서울특별시 금천구 독산로80길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '281','37.4523581','독산로14길 3','방범 CCTV','관리번호: P141','126.90696200000002','대한민국 서울특별시 금천구 독산로14길 3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '282','37.4677741','독산로70길 84','방범 CCTV','관리번호: P142','126.90607850000003','대한민국 서울특별시 금천구 독산로70길 84');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '283','37.4683903','시흥대로118길 23','방범 CCTV','관리번호: P143','126.90004870000007','대한민국 서울특별시 금천구 시흥대로118길 23');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '284','37.472065','시흥대로136길 24','방범 CCTV','관리번호: P144','126.90046400000005','대한민국 서울특별시 금천구 시흥대로136길 24');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '285','37.4761498','시흥대로150길 41','방범 CCTV','관리번호: P145','126.90108129999998','대한민국 서울특별시 금천구 시흥대로150길 41');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '286','37.4789226','독산로109길 26','방범 CCTV','관리번호: P146','126.90228219999994','대한민국 서울특별시 금천구 독산로109길 26');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '287','37.4770498','문성로1길 7','방범 CCTV','관리번호: P147','126.90427449999993','대한민국 서울특별시 금천구 문성로1길 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '288','37.4606636','독산로50길 183','방범 CCTV','관리번호: P148','126.91229610000005','대한민국 서울특별시 금천구 독산로50길 183');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '289','37.4612232','시흥대로90길 11','방범 CCTV','관리번호: P149','126.89877569999999','대한민국 서울특별시 금천구 시흥대로90길 11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '290','37.4611969','시흥대로84길 54','방범 CCTV','관리번호: P150','126.90205300000002','대한민국 서울특별시 금천구 시흥대로84길 54');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '291','37.450389','탑골로2길 56','방범 CCTV','관리번호: P151','126.91317279999998','대한민국 서울특별시 금천구 탑골로2길 56');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '292','37.445854','호암1터널','방범 CCTV','관리번호: P152','126.91803430000004','대한민국 서울특별시 금천구 시흥동 호암1터널');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '293','37.4486109','시흥대로40다길 28','방범 CCTV','관리번호: P153','126.90591459999996','대한민국 서울특별시 금천구 시흥대로40다길 28');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '294','37.4479648','독산로4길 18','방범 CCTV','관리번호: P154','126.90454739999995','대한민국 서울특별시 금천구 독산로4길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '295','37.4574568','독산로32길 11','방범 CCTV','관리번호: P155','126.90614849999997','대한민국 서울특별시 금천구 독산로32길 11');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '296','37.4594147','시흥대로80길 39','방범 CCTV','관리번호: P156','126.90167099999996','대한민국 서울특별시 금천구 시흥대로80길 39');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '297','37.4597989','독산로41길 7','방범 CCTV','관리번호: P157','126.90433069999994','대한민국 서울특별시 금천구 독산로41길 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '298','37.456205','독산로29길 10','방범 CCTV','관리번호: P158','126.90494949999993','대한민국 서울특별시 금천구 독산로29길 10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '299','37.4552897','금하로9길 13','방범 CCTV','관리번호: P159','126.90215179999995','대한민국 서울특별시 금천구 금하로9길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '300','37.4838349','벚꽃로48길 7','방범 CCTV','관리번호: P192','126.88245710000001','대한민국 서울특별시 금천구 벚꽃로48길 7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '301','37.4721877','시흥대로123길 66','방범 CCTV','관리번호: P194','126.89526290000003','대한민국 서울특별시 금천구 시흥대로123길 66');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '302','37.4744183','시흥대로457','방범 CCTV','관리번호: P196','126.89782919999993','대한민국 서울특별시 금천구 시흥대로 457');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '303','37.4768505','가산디지털2로 98','방범 CCTV','관리번호: P198','126.8811806','대한민국 서울특별시 금천구 가산디지털2로 98');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '304','37.46457050000001','벚꽃로10길 94','방범 CCTV','관리번호: P200','126.89528569999993','대한민국 서울특별시 금천구 벚꽃로10길 94');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '305','37.4663281','범안로1172','방범 CCTV','관리번호: P202','126.8919793','대한민국 서울특별시 금천구 범안로 1172');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '306','37.4528524','금하로1마길 15-8','방범 CCTV','관리번호: P204','126.88961710000001','대한민국 서울특별시 금천구 금하로1마길 15-8');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '307','37.4839525','벚꽃로56길 50','방범 CCTV','관리번호: P206','126.88327279999998','대한민국 서울특별시 금천구 벚꽃로56길 50');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '308','37.4686247','범안로15길 52','방범 CCTV','관리번호: P209','126.89253780000001','대한민국 서울특별시 금천구 범안로15길 52');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '309','37.4518415','탑골로10길 3','방범 CCTV','관리번호: P210','126.9146538','대한민국 서울특별시 금천구 탑골로10길 3');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '310','37.4507428','금하로29길 15','방범 CCTV','관리번호: P212','126.91245559999993','대한민국 서울특별시 금천구 금하로29길 15');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '311','37.4790542','남부순환로130길 32','방범 CCTV','관리번호: P214','126.90811540000004','대한민국 서울특별시 금천구 남부순환로130길 32');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '312','37.446713','시흥대로39길 52 은행마을 주택앞','방범 CCTV','관리번호: P216','126.90053899999998','대한민국 서울특별시 금천구 시흥대로39길 52');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '313','37.4567667','시흥대로73길 70 금나래아트홀','방범 CCTV','관리번호: P217','126.89540050000005','대한민국 서울특별시 금천구 시흥대로73길 70');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '314','37.465671','범안로16길 24-26','방범 CCTV','관리번호: P218','126.89445449999994','대한민국 서울특별시 금천구 범안로16길 24-26');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '315','37.4828444','벚꽃로46길 18','방범 CCTV','관리번호: P219','126.8835765','대한민국 서울특별시 금천구 벚꽃로46길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '316','37.4525091','금하로674','방범 CCTV','관리번호: P220','126.9051627','대한민국 서울특별시 금천구 금하로 674');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '317','37.4726442','두산로3길 49','방범 CCTV','관리번호: P221','126.88869350000004','대한민국 서울특별시 금천구 두산로3길 49');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '318','37.4536035','금하로1라길 33','방범 CCTV','관리번호: P222','126.89044119999994','대한민국 서울특별시 금천구 금하로1라길 33');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '319','37.4621505','시흥대로94길 20','방범 CCTV','관리번호: P223','126.89907189999997','대한민국 서울특별시 금천구 시흥대로94길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '320','37.4765938','독산로103길 14','방범 CCTV','관리번호: P224','126.90271080000002','대한민국 서울특별시 금천구 독산로103길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '321','37.4777107','남부순환로120길 17-21','방범 CCTV','관리번호: P225','126.90179680000005','대한민국 서울특별시 금천구 남부순환로120길 17-21');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '322','37.4674357','독산로72길 10-8','방범 CCTV','관리번호: P226','126.90252699999996','대한민국 서울특별시 금천구 독산로72길 10-8');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '323','37.4677326','독산로70다길 16','방범 CCTV','관리번호: P227','126.90403749999996','대한민국 서울특별시 금천구 독산로70다길 16');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '324','37.4547097','금하로9길 4-15','방범 CCTV','관리번호: P228','126.90256829999998','대한민국 서울특별시 금천구 금하로9길 4-15');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '325','37.4535505','독산로21길 5','방범 CCTV','관리번호: P229','126.90563350000002','대한민국 서울특별시 금천구 독산로21길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '326','37.4608904','독산로36길 94','방범 CCTV','관리번호: P230','126.90942399999994','대한민국 서울특별시 금천구 독산로36길 94');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '327','37.4680446','시흥대로114길 5 아카시아병원뒤','방범 CCTV','관리번호: P231','126.89854000000002','대한민국 서울특별시 금천구 시흥대로114길 5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '328','37.4579248','시흥대로74길 35 해태공원','방범 CCTV','관리번호: P232','126.90200140000002','대한민국 서울특별시 금천구 시흥대로74길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '329','37.460404','독산로43가길 20','방범 CCTV','관리번호: P233','126.90321310000001','대한민국 서울특별시 금천구 독산로43가길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '330','37.4569488','독산로33길 40-2','방범 CCTV','관리번호: P234','126.9031516','대한민국 서울특별시 금천구 독산로33길 40-2');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '331','37.4500828','독산로5길 24','방범 CCTV','관리번호: P235','126.90461040000002','대한민국 서울특별시 금천구 독산로5길 24');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '332','37.4529552','탑골로73 우리교회앞','방범 CCTV','관리번호: P236','126.9167162','대한민국 서울특별시 금천구 탑골로 73');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '333','37.4506854','시흥대로 16길 76','방범 CCTV','관리번호: P237','126.9012586','대한민국 서울특별시 금천구 시흥대로');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '334','37.4393726','시흥대로 14길','방범 CCTV','관리번호: P238','126.90507950000005','대한민국 서울특별시 금천구 시흥대로14길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '335','37.4749398','시흥대로149길 60','방범 CCTV','관리번호: P239','126.89471930000002','대한민국 서울특별시 금천구 시흥대로149길 60');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '336','37.4780539','남부순환로 108길 20','방범 CCTV','관리번호: P240','126.89213749999999','대한민국 서울특별시 금천구 남부순환로108길 20');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '337','37.4672412','범안로17길 17-5','방범 CCTV','관리번호: P241','126.89403909999998','대한민국 서울특별시 금천구 범안로17길 17-5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '338','37.4525941','금하로 1길 17','방범 CCTV','관리번호: P242','126.89214579999998','대한민국 서울특별시 금천구 금하로1길 17');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '339','37.464213','시흥대로 343','방범 CCTV','관리번호: P243','126.89714479999998','대한민국 서울특별시 금천구 시흥대로 343');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '340','37.4594787','시흥대로 82길 14','방범 CCTV','관리번호: P244','126.90060760000006','대한민국 서울특별시 금천구 시흥대로82길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '341','37.4661597','독산로64길','방범 CCTV','관리번호: P245','126.90551600000003','대한민국 서울특별시 금천구 독산로64길');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '342','37.4724749','독산로 85길 39','방범 CCTV','관리번호: P246','126.90084009999998','대한민국 서울특별시 금천구 독산로85길 39');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '343','37.474503','시흥대로 148길 38-18','방범 CCTV','관리번호: P247','126.90072780000002','대한민국 서울특별시 금천구 시흥대로148길 38-18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '344','37.4690656','독산로75길 39','방범 CCTV','관리번호: P248','126.90019359999996','대한민국 서울특별시 금천구 독산로75길 39');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '345','37.4682942','독산로 72길 50','방범 CCTV','관리번호: P249','126.90454249999993','대한민국 서울특별시 금천구 독산로72길 50');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '346','37.4551092','금하로 631-6','방범 CCTV','관리번호: P250','126.90144469999995','대한민국 서울특별시 금천구 금하로 631-6');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '347','37.4614798','독산로45길 12','방범 CCTV','관리번호: P251','126.90379299999995','대한민국 서울특별시 금천구 독산로45길 12');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '348','37.4524024','탑골로65','방범 CCTV','관리번호: P252','126.91616580000004','대한민국 서울특별시 금천구 탑골로 65');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '349','37.4529073','탑골로13길 18','방범 CCTV','관리번호: P253','126.91630550000002','대한민국 서울특별시 금천구 탑골로13길 18');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '350','37.4459322','시흥대로 133-1','방범 CCTV','관리번호: P254','126.9032029','대한민국 서울특별시 금천구 시흥대로 133-1');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '351','37.4506854','시흥대로 122뒤','방범 CCTV','관리번호: P255','126.9012586','대한민국 서울특별시 금천구 시흥대로');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '352','37.4597476','독산로40길 10','방범 CCTV','관리번호: P256','126.90549910000004','대한민국 서울특별시 금천구 독산로40길 10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '353','37.4607268','독산로36길 88-5','방범 CCTV','관리번호: P257','126.90927490000001','대한민국 서울특별시 금천구 독산로36길 88-5');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '354','37.4466351','금하로24길 94-29','방범 CCTV','관리번호: P258','126.9086658','대한민국 서울특별시 금천구 금하로24길 94-29');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '355','37.4513659','탑골로4길 18-10','방범 CCTV','관리번호: P259','126.91231659999994','대한민국 서울특별시 금천구 탑골로4길 18-10');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '356','37.4787797','남부순환로124길 15','방범 CCTV','관리번호: P260','126.90300330000002','대한민국 서울특별시 금천구 남부순환로124길 15');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '357','37.4738393','시흥대로146길 26','방범 CCTV','관리번호: P261','126.90009199999997','대한민국 서울특별시 금천구 시흥대로146길 26');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '358','37.4773797','문성로3길 92','방범 CCTV','관리번호: P262','126.90944220000006','대한민국 서울특별시 금천구 문성로3길 92');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '359','37.475918','독산로102길 17','방범 CCTV','관리번호: P263','126.90464599999995','대한민국 서울특별시 금천구 독산로102길 17');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '360','37.4763386','문성로 22','방범 CCTV','관리번호: P264','126.90608480000003','대한민국 서울특별시 금천구 문성로 22');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '361','37.4622996','독산로47길 30','방범 CCTV','관리번호: P265','126.90261110000006','대한민국 서울특별시 금천구 독산로47길 30');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '362','37.4624534','시흥대로94길 25','방범 CCTV','관리번호: P266','126.89919399999996','대한민국 서울특별시 금천구 시흥대로94길 25');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '363','37.4502555','금하로 728-13','방범 CCTV','관리번호: P267','126.91081370000006','대한민국 서울특별시 금천구 금하로 728-13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '364','37.4649216','독산로58가길 68','방범 CCTV','관리번호: P101','126.90697690000002','대한민국 서울특별시 금천구 독산로58가길 68');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '365','37.4795782','남부순환로128길 14','방범 CCTV','관리번호: P180','126.90699859999995','대한민국 서울특별시 금천구 남부순환로128길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '366','37.4711394','독산로80길 13','방범 CCTV','관리번호: P181','126.9039335','대한민국 서울특별시 금천구 독산로80길 13');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '367','37.4687779','독산로70다길 64','방범 CCTV','관리번호: P182','126.90617799999995','대한민국 서울특별시 금천구 독산로70다길 64');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '368','37.4676325','독산로70라길 14','방범 CCTV','관리번호: P183','126.90489809999997','대한민국 서울특별시 금천구 독산로70라길 14');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '369','37.469427','독산로75길 12-7','방범 CCTV','관리번호: P184','126.9015551','대한민국 서울특별시 금천구 독산로75길 12-7');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '370','37.4700958','시흥대로128길 34','방범 CCTV','관리번호: P185','126.9001333','대한민국 서울특별시 금천구 시흥대로128길 34');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '371','37.4826211','벚꽃로38길 35','방범 CCTV','관리번호: P186','126.884455','대한민국 서울특별시 금천구 벚꽃로38길 35');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '372','37.4734504','가산로5길 43','방범 CCTV','관리번호: P188','126.89212739999993','대한민국 서울특별시 금천구 가산로5길 43');");
        sqlDB.execSQL("INSERT INTO cctvTBL VALUES ( '373','37.477559','남부순환로110길 31','방범 CCTV','관리번호: P190','126.89345290000005','대한민국 서울특별시 금천구 남부순환로110길 31');");
        sqlDB.close();

//        // 테스트 데이터 출력
//        sqlDB = myHelper.getReadableDatabase();
//        Cursor cursor;
//        cursor = sqlDB.rawQuery("SELECT * FROM cctvTBL;", null);
//
//        while (cursor.moveToNext()) {
//            //Toast.makeText(getApplicationContext(), cursor.getString(1), Toast.LENGTH_SHORT).show();
//            //cursor.getString(1);
//            Log.d("db no:", cursor.getString(0));
//            Log.d("db x:", cursor.getString(1));
//        }
//
//        cursor.close();
//        sqlDB.close();

        // CCTV 위치정보 json 파싱 종료

        // 긴급문자 버튼
        ImageButton messageBtn = (ImageButton) findViewById(R.id.messageBtn);
        messageBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                } else {
                    sendSMS();
                }


            }
        });

        // 비상사이렌 버튼
        ImageButton sirenBtn = (ImageButton) findViewById(R.id.sirenBtn);
        sirenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "클릭", Toast.LENGTH_SHORT).show();

                // 비상사이렌 사운드
                final SoundPool sound = new SoundPool(1, AudioManager.STREAM_ALARM, 0);

                final int soundId = sound.load(getApplicationContext(), R.raw.siren, 1);

                sound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        streamId = sound.play(soundId, 1.0F, 1.0F, 1, -1, 1.0F);
                    }
                });

                // 대화상자
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setMessage("비상경고음 작동중");
                dlg.setPositiveButton("끄기", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        sound.stop(streamId);
                    }
                });
                dlg.show();

            }
        });

    }

    // 문자보내기 함수
    private void sendSMS() {

        SharedPreferences prefs =getSharedPreferences("test", MODE_PRIVATE);
        String message = prefs.getString("MSG", ""); //키값, 디폴트값
        String phoneNumber = prefs.getString("PHONE", ""); //키값, 디폴트값

        //Toast.makeText(getBaseContext(), "메시지를 전송하였습니다. phone: " + phoneNumber + ", msg : " + message, Toast.LENGTH_SHORT).show();

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "메시지를 전송하였습니다.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }



    // 데이터베이스 함수
    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(Context context) {
            super(context, "cctvDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE cctvTBL (no INTEGER PRIMARY KEY, gc_mapx varchar(100), gc_maptitle varchar(100), gc_mapname varchar(100), gc_mapdesc varchar(100), gc_mapy varchar(100), gc_mapaddress varchar(100))");


            } catch (SQLException ex) {
                Log.d("SQLException : ", ex.toString());
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS cctvTBL");
            onCreate(db);


        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

//    Settings 버튼 제거
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(getApplicationContext(), ManageActivity.class);
            startActivity(intent);
        }

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
