package com.berdnamu.peaceofhome_v2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ManageActivity extends Activity {

    EditText msgEdit;
    EditText phoneEdit;

    // 데이터베이스
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    String msg;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage);

        ImageButton btnBack_manage = (ImageButton) findViewById(R.id.btnBack_manage);

        btnBack_manage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 데이터베이스에서 보낼메시지 있나 확인 후 있으면 표시
        // 데이터베이스 헬퍼 호출
//        myHelper = new myDBHelper(this);
//        sqlDB = myHelper.getWritableDatabase();
//        myHelper.onUpgrade(sqlDB, 1 , 2);
//        sqlDB.close();
//
//        sqlDB = myHelper.getReadableDatabase();
//        Cursor cursor;
//        cursor = sqlDB.rawQuery("SELECT * FROM msgTBL;", null);
//
//        while (cursor.moveToNext()) {
//            Log.d("msg db no:", cursor.getString(0));
//            Log.d("msg db msg:", cursor.getString(1));
//
//            msg = cursor.getString(1);
//        }
//
//        cursor.close();
//        sqlDB.close();




        // 가져온 메시지를 에디트텍스트에 지정
        //값읽기
        SharedPreferences prefs =getSharedPreferences("test", MODE_PRIVATE);
        String result = prefs.getString("MSG", ""); //키값, 디폴트값
        String result2 = prefs.getString("PHONE", ""); //키값, 디폴트값

        msgEdit = (EditText) findViewById(R.id.messageEdit);
        msgEdit.setText(result);
        phoneEdit = (EditText) findViewById(R.id.phoneEdit);
        phoneEdit.setText(result2);

        // 연락처등록하기 & 보낼메시지등록
        ImageButton setBtn = (ImageButton) findViewById(R.id.setBtn);
        setBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String value1 = msgEdit.getText().toString();
                String value2 = phoneEdit.getText().toString();

//                sqlDB = myHelper.getWritableDatabase();
//                sqlDB.execSQL("INSERT INTO msgTBL(msg) VALUES ('" + value1 + "');");
//                sqlDB.close();

                SharedPreferences pref =getSharedPreferences("test", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("MSG",value1 ); //키값, 저장값
                editor.putString("PHONE",value2 ); //키값, 저장값
                editor.commit();

                Toast.makeText(getApplicationContext(), "등록하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });




    }

    // 데이터베이스 함수
    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(Context context) {
            super(context, "cctvDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS msgTBL (no INTEGER PRIMARY KEY autoincrement, msg varchar(2000))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS msgTBL");
            onCreate(db);
        }
    }

}
