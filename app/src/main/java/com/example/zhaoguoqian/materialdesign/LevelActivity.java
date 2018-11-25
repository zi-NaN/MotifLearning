package com.example.zhaoguoqian.materialdesign;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LevelActivity extends AppCompatActivity {
    private List<Level> levelList = new ArrayList<>();
    private DataBaseHelper myDBHelper;
    private final String TAG = "Level Activity";
    private int difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);

        // set the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set the difficulty
        Intent intent = getIntent();
        difficulty = intent.getIntExtra("difficulty", 0);
        if (difficulty==0){
            Log.w(TAG, "Not pass the difficulty to level activity");
            difficulty = 1;
        }

        // get/update the level data
        queryData();

        // set the recycle view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        final LevelAdapter adapter = new LevelAdapter(levelList, this);

        recyclerView.setAdapter(adapter);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }
        return true;
    }

    // when exit the activity without choosing a level
    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        myDBHelper.close();
        finish();
    }

    private void queryData(){
        myDBHelper = new DataBaseHelper(this);
        levelList.clear();

        // try to open the database
        try {
            myDBHelper.createDataBase();
            Log.v(TAG, "database created");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {
            myDBHelper.openDataBase();
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }

        // query the level state data
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String queryLevel = "SELECT * FROM levelState WHERE difficulty=?";
        Cursor cursor = db.rawQuery(queryLevel, new String[]{Integer.toString(difficulty)});
        if(cursor.moveToFirst()){
            do{
                int number = cursor.getInt(cursor.getColumnIndex("level"));
                int diff = cursor.getInt(cursor.getColumnIndex("difficulty"));
                int state = cursor.getInt(cursor.getColumnIndex("state"));
                levelList.add(new Level(this.difficulty, number, state));
            }while(cursor.moveToNext());
        }
    }

}