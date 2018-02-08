package com.intel.test.locationdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.intel.test.locationdemo.data.DBManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static String LOG_TAG = "MainActivity";
    private Button mCollect;
    private Button mCollect4;
    private Button mTest;
    private Button mDelete;
    private Button mSearch;
    private EditText mTableName;

    private EditText mTB1;
    private EditText mTB2;

    private DBManager mDBM;
    public String tableName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponent();

        mDBM = new DBManager(this);

        //DBContext.getMyDatabasePath("test");

    }
    private void initComponent() {
        mTableName = (EditText)findViewById(R.id.et_tablename);
        mTB1 = (EditText)findViewById(R.id.et_tbname1);
        mTB2 = (EditText)findViewById(R.id.et_tbname2);

        mCollect = (Button)findViewById(R.id.btn_collect);
        mCollect.setOnClickListener(mClickListener);

        mTest = (Button)findViewById(R.id.btn_test);
        mTest.setOnClickListener(mClickListener);

        mCollect4 = (Button)findViewById(R.id.btn_collect4);
        mCollect4.setOnClickListener(mClickListener);

        mDelete = (Button)findViewById(R.id.btn_delete);
        mDelete.setOnClickListener(mClickListener);

        mSearch = (Button)findViewById(R.id.btn_search);
        mSearch.setOnClickListener(mClickListener);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            Bundle bundle = new Bundle();
            Intent intent = new Intent();
            switch (view.getId()){
                case R.id.btn_search:
                    tableName = mTableName.getText().toString();
                    Log.d(LOG_TAG,"tableName is " + tableName);
                    if (tableName.length()>0){
                        searchTable(tableName);
                    }else {
                        Toast toast=Toast.makeText(getApplicationContext(), "Please Input tableName", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(LOG_TAG,"Please Input tableName");
                    }
                    break;
                case R.id.btn_delete:
                    tableName = mTableName.getText().toString();
                    if (tableName.length()>0){
                        deleteTable(tableName);
                    }else {
                        Toast toast=Toast.makeText(getApplicationContext(), "Please Input tableName", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(LOG_TAG,"Please Input tableName");
                    }
                    break;
                case R.id.btn_collect:
                    tableName = mTableName.getText().toString();
                    if (tableName.length()>0){
                        intent = new Intent(MainActivity.this,CollectOneActivity.class);
                        bundle.putString("tableName",tableName);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else {
                        Toast toast=Toast.makeText(getApplicationContext(), "Please Input tableName", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(LOG_TAG,"Please Input tableName");
                    }
                    break;
                case R.id.btn_collect4:
                    tableName = mTableName.getText().toString();
                    if (tableName.length()>0){
                        intent = new Intent(MainActivity.this,CollectFourActivity.class);
                        bundle.putString("tableName",tableName);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else {
                        Toast toast=Toast.makeText(getApplicationContext(), "Please Input tableName", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(LOG_TAG,"Please Input tableName");
                    }

                    break;
                case R.id.btn_test:
                    String tableName1 = mTB1.getText().toString();
                    String tableName2 = mTB2.getText().toString();
                    if (tableName1.length()>0 && tableName2.length()>0){
                        boolean flag1 = false;
                        boolean flag2 = false;

                        flag1 = searchTable(tableName1);
                        flag2 = searchTable(tableName2);
                        if (flag1 && flag2){
                            intent = new Intent(MainActivity.this,TestActivity.class);
                            bundle.putString("tableName1",tableName1);
                            bundle.putString("tableName2",tableName2);

                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }else {
                        Toast toast=Toast.makeText(getApplicationContext(), "Please Input tableName", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.d(LOG_TAG,"Please Input tableName");
                    }
                    break;
            }
        }
    };
    private boolean searchTable(String tableName){
        Log.d(LOG_TAG,"searchTable " + tableName);
        boolean flag = false;
        List<String> tableNames = new ArrayList<String>();
        tableNames = mDBM.queryTableNameList();
        for (String name : tableNames) {
            if(name.equals(tableName)){
                flag = true;
            }
        }
        if (!flag){
            Toast toast=Toast.makeText(getApplicationContext(), "Table " + tableName + " not exists", Toast.LENGTH_SHORT);
            toast.show();
            Log.d(LOG_TAG,"Table " + tableName +" not exists");
        }else {
            Toast toast=Toast.makeText(getApplicationContext(), "Table " + tableName + " exists", Toast.LENGTH_SHORT);
            toast.show();
            Log.d(LOG_TAG,"Table " + tableName +" exists");
        }
        return flag;
    }

    private void deleteTable(String tableName){
        Log.d(LOG_TAG,"deleteTable " + tableName);
        boolean flagSearch = false;
        flagSearch = searchTable(tableName);
        if (!flagSearch){
            Log.d(LOG_TAG,"Table " + tableName + "not exists!");
        }else {
            mDBM.deleteTable(tableName);
            boolean flagDelete = false;
            flagDelete = searchTable(tableName);
            if (!flagDelete){
                Toast toast=Toast.makeText(getApplicationContext(), "Table " + tableName + " delete successful!", Toast.LENGTH_SHORT);
                toast.show();
                Log.d(LOG_TAG,"Table " + tableName +" delete successful!");
            }else {
                Toast toast=Toast.makeText(getApplicationContext(), "Table " + tableName + " delete failed!", Toast.LENGTH_SHORT);
                toast.show();
                Log.d(LOG_TAG,"Table " + tableName +" delete failed!");
            }
        }
    }
}
