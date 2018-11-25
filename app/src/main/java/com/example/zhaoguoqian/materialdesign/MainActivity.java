package com.example.zhaoguoqian.materialdesign;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.google.gson.Gson;

import butterknife.InjectView;
import butterknife.ButterKnife;

import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import giwi.org.networkgraph.GraphSurfaceView;
import giwi.org.networkgraph.beans.NetworkGraph;
import giwi.org.networkgraph.beans.Vertex;


public class MainActivity extends AppCompatActivity {
    public final static int SELECTLEVEL = 1;
    public final static int DIFFICULTY = 2;
    private DrawerLayout mDrawerLayout;
    private String TAG = "Main Activity"; //use in Log
    private int level;
    private int difficulty=1;
    private DataBaseHelper myDbHelper;
    private Edge[] edgeArr;
    private int connectedNum = 0;
    private boolean win=false;

    @InjectView(R.id.network) WebView webview;

    public class WebAppInterface {
        private Context context;

        public WebAppInterface(Context context) {
            this.context = context;
        }
        @JavascriptInterface
        public void logD(String msg){
            Log.d(TAG, msg);
        }

        @JavascriptInterface
        public String getNodeData() {
            SQLiteDatabase db = myDbHelper.getWritableDatabase();
            String queryNode = "select * from nodes " +
                    "where level = ? " +
                    "and ((innerID in (select node1 from edges where edges.level = ? and edges.difficulty = ?)) " +
                         "or (innerID in (select node2 from edges where edges.level = ? and edges.difficulty=?)))";

            List<Node> nodeList = new ArrayList<Node>();

            Cursor cursor = db.rawQuery(queryNode, new String[]{Integer.toString(level), Integer.toString(level), Integer.toString(difficulty),Integer.toString(level), Integer.toString(difficulty)});
            if(cursor.moveToFirst()){
                do{
                    String innerID = cursor.getString(cursor.getColumnIndex("innerID"));
                    String label = cursor.getString(cursor.getColumnIndex("label"));
                    int level = cursor.getInt(cursor.getColumnIndex("level"));

                    nodeList.add(new Node(innerID, label, level));
                }while(cursor.moveToNext());
            }

            Node[] nodeArr = nodeList.toArray(new Node[]{});
            Log.d(TAG, "select level " + level +" nodes");
            return new Gson().toJson(nodeArr, Node[].class );
        }

        @JavascriptInterface
        public String getAllNodeData() {
            SQLiteDatabase db = myDbHelper.getWritableDatabase();
            String queryNode = "SELECT * FROM nodes WHERE level = ?;";

            List<Node> nodeList = new ArrayList<Node>();

            Cursor cursor = db.rawQuery(queryNode, new String[]{Integer.toString(level)});
            if(cursor.moveToFirst()){
                do{
                    String innerID = cursor.getString(cursor.getColumnIndex("innerID"));
                    String label = cursor.getString(cursor.getColumnIndex("label"));
                    int level = cursor.getInt(cursor.getColumnIndex("level"));

                    nodeList.add(new Node(innerID, label, level));
                }while(cursor.moveToNext());
            }

            Node[] nodeArr = nodeList.toArray(new Node[]{});
            Log.d(TAG, "select all level " + level +" nodes");
            return new Gson().toJson(nodeArr, Node[].class );
        }

        @JavascriptInterface
        public String getUnConnectedEdge(){
            SQLiteDatabase db = myDbHelper.getWritableDatabase();
            String queryEdge = "SELECT * FROM edges WHERE level = ? AND difficulty = ? AND connected=0;";
            List<Edge> edgeList = new ArrayList<Edge>();
            Cursor cursor = db.rawQuery(queryEdge, new String[]{Integer.toString(level), Integer.toString(difficulty)});
            if(cursor.moveToFirst()){
                do{
                    String node1 = cursor.getString(cursor.getColumnIndex("node1"));
                    String node2 = cursor.getString(cursor.getColumnIndex("node2"));
                    int level = cursor.getInt(cursor.getColumnIndex("level"));
                    int difficulty = cursor.getInt(cursor.getColumnIndex("difficulty"));
                    int connected = cursor.getInt(cursor.getColumnIndex("connected"));

                    edgeList.add(new Edge(node1, node2, level, difficulty, connected));
                }while(cursor.moveToNext());
                cursor.close();
            }

            edgeArr = edgeList.toArray(new Edge[]{});
            Log.d(TAG, "select level "+ level+" difficulty "+ difficulty + " unconnected edges");
            return new Gson().toJson(edgeArr, Edge[].class);
        }

        @JavascriptInterface
        public String getEdgeData(){
            SQLiteDatabase db = myDbHelper.getWritableDatabase();
            String queryEdge = "SELECT * FROM edges WHERE level = ? AND difficulty = ? AND node1 != node2;";
            List<Edge> edgeList = new ArrayList<Edge>();
            Cursor cursor = db.rawQuery(queryEdge, new String[]{Integer.toString(level), Integer.toString(difficulty)});
            if(cursor.moveToFirst()){
                do{
                    String node1 = cursor.getString(cursor.getColumnIndex("node1"));
                    String node2 = cursor.getString(cursor.getColumnIndex("node2"));
                    int level = cursor.getInt(cursor.getColumnIndex("level"));
                    int difficulty = cursor.getInt(cursor.getColumnIndex("difficulty"));
                    int connected = cursor.getInt(cursor.getColumnIndex("connected"));

                    edgeList.add(new Edge(node1, node2, level, difficulty, connected));
                }while(cursor.moveToNext());
            }

            edgeArr = edgeList.toArray(new Edge[]{});
            Log.d(TAG, "select level "+ level+" difficulty "+ difficulty + "edges");
            return new Gson().toJson(edgeArr, Edge[].class);
        }

        @JavascriptInterface
        public int getDifficulty(){return difficulty;}

        @JavascriptInterface
        public void changeAndCheck(int i, boolean check){
            if(edgeArr==null || i>=edgeArr.length){
                win = false;
            }else{
                int connect;
                connect = check?1:0;
                Edge changedEdge = edgeArr[i];
                String setSQL = "UPDATE edges SET connected=" + connect +
                        " WHERE level=" + level +
                        " AND difficulty=" + difficulty +
                        " AND node1=\"" + changedEdge.getSource() +
                        "\" AND node2=\"" + changedEdge.getTarget() + "\"";
                SQLiteDatabase db = myDbHelper.getWritableDatabase();
                db.execSQL(setSQL);

                // check whether the player wins
                String getSum = "SELECT SUM(connected) FROM edges WHERE level=? AND difficulty=?;";
                Cursor cursor = db.rawQuery(getSum, new String[]{Integer.toString(level),Integer.toString(difficulty)});
                if(cursor.moveToFirst()){
                    int sumConnected = cursor.getInt(cursor.getColumnIndex("SUM(connected)"));
                    cursor.close();
                    if(sumConnected >= edgeArr.length){
                        String completeLevelSQL = "UPDATE levelState SET state = 2 " +
                                "WHERE level=" + level +
                                " AND difficulty=" + difficulty;
                        String newLevelSQL = "UPDATE levelState SET state=1" +
                                " WHERE level=" + (level+1)+
                                " AND difficulty="+difficulty;
                        db.execSQL(completeLevelSQL);
                        db.execSQL(newLevelSQL);
                        win = true;
                    }else{
                        win = false;
                    }
                }else{
                    Log.e(TAG, "Fail to check whether the player wins.");
                    win = false;
                }
                db.close();
            }

            if(win==true){
                winDialog();
            }
        }

        @JavascriptInterface
        public int checkConnected(int i){
            int result=0;
            if(i>=edgeArr.length){
                return -1;
            }

            SQLiteDatabase db = myDbHelper.getWritableDatabase();
            Edge myEdge = edgeArr[i];
            String checkSQL = "SELECT connected FROM edges WHERE level=? AND difficulty=? AND node1=? AND node2=?";
            Cursor cursor = db.rawQuery(checkSQL, new String[]{Integer.toString(level), Integer.toString(difficulty), myEdge.getSource(), myEdge.getTarget()});
            if(cursor.moveToFirst()){
                result = cursor.getInt(cursor.getColumnIndex("connected"));
                Log.v(TAG, "check state of edge, the state is " + result);
                cursor.close();
            }
            return result;
        }

        @JavascriptInterface
        public void init(){
            String initSQL = "UPDATE edges SET connected=0 WHERE level="+level+" AND difficulty="+difficulty;
            SQLiteDatabase db = myDbHelper.getWritableDatabase();
            db.execSQL(initSQL);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadToolBar();
        addFuncitonToDrawer();
        webview = findViewById(R.id.network);

        // add function to the hint and reset button
        findViewById(R.id.button_hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWin();
                if(!win){
                    webview.post(new Runnable() {
                        @Override
                        public void run() {
                            webview.loadUrl("javascript:addRandomEdge()");
                        }
                    });
                }else{
                    winDialog();
                }
            }
        });
        findViewById(R.id.button_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.post(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl("javascript:reset()");
                    }
                });

            }
        });


    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    // when the icons on action bar are selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            // receive the selected level and load the network
            case SELECTLEVEL:
                if (resultCode == RESULT_OK){
                    level = data.getIntExtra("level", 1)+1;
                    findViewById(R.id.initial).setVisibility(View.GONE);
                    findViewById(R.id.network).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_reset).setVisibility(View.VISIBLE);
                    findViewById(R.id.button_hint).setVisibility(View.VISIBLE);

                    webview = findViewById(R.id.network);

                    Log.v(TAG, "select level "+ level);

                    openDBHelper();

                    // if the level is completed, reset it
                    SQLiteDatabase db = myDbHelper.getWritableDatabase();
                    String levelStateSQL = "SELECT state FROM levelState WHERE level=? AND difficulty=?;";
                    Cursor cursor = db.rawQuery(levelStateSQL, new String[]{Integer.toString(level), Integer.toString(difficulty)});
                    if(cursor.moveToFirst()){
                        int state = cursor.getInt(cursor.getColumnIndex("state"));
                        cursor.close();
                        if(state==2){
                            resetCurrentLevel();
                        }
                    }else{
                        Log.e(TAG, "Fail to get the level state");
                    }
                    loadNetwork();
                }
                break;
            case DIFFICULTY:
                if(resultCode==RESULT_OK){
                    this.difficulty = Integer.parseInt(data.getStringExtra("difficulty"));
                    Log.v(TAG, "change difficulty to " + Integer.toString(difficulty));
                    loadNetwork();
                }
            default:
        }
    }

    private void loadToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);

        // change the action bar
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_parent);

        // change the home button to menu button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void loadNetwork(){
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);

        final WebSettings ws = webview.getSettings();
        ws.setPluginState(WebSettings.PluginState.ON);
        ws.setAllowFileAccess(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface( new WebAppInterface( this ), "Android");
        webview.loadUrl("file:///android_asset/main.html");
    }

    private void addFuncitonToDrawer(){
        NavigationView navView = findViewById(R.id.nav_view);
        // set action to the selected navigation item in drawer
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener (){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                mDrawerLayout.closeDrawers();
                Intent intent = null;
                switch(item.getItemId()){
                    case R.id.nav_level:
                        intent = new Intent(MyApplication.getContext(), LevelActivity.class);
                        intent.putExtra("difficulty", difficulty);
                        startActivityForResult(intent, SELECTLEVEL);
                        break;
                    case R.id.nav_setting:
                        intent = new Intent(MyApplication.getContext(), SettingActivity.class);
                        startActivityForResult(intent, DIFFICULTY);
                        break;
                    case R.id.nav_tutorial:
                        intent = new Intent(MyApplication.getContext(), TutorialActivity.class);
                        startActivity(intent);
                        break;
                    default:
                }
                return true;
            }
        });
    }

    public int getDifficulty(){
        return this.difficulty;
    }

    private void openDBHelper(){
        myDbHelper = new DataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
            Log.v(TAG, "database created");
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            myDbHelper.openDataBase();
        }catch(SQLException sqle){
            throw sqle;
        }
    }

    private void resetLevel(int l, int d){
        String resetLevelData = "UPDATE edges SET connected=0 " +
                "WHERE level="+level+
                " AND difficulty="+difficulty;
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        db.execSQL(resetLevelData);
    }

    private void resetCurrentLevel(){
        resetLevel(level, difficulty);
    }

    private void winDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Congratulations! You win!")
                .setMessage("What do you want to do next?")
                .setCancelable(false)
                .setPositiveButton("Play again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetCurrentLevel();
                    }
                })
                .setNegativeButton("Choose level", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MyApplication.getContext(), LevelActivity.class);
                        intent.putExtra("difficulty", difficulty);
                        startActivityForResult(intent, SELECTLEVEL);
                    }
                });

        final AlertDialog alertDialog = builder.create();
    }

    private void checkWin(){
        String sumQuerySQL = "select sum(connected) from edges where difficulty = "+difficulty+" and level = "+level;
        int sum=-1;
        String countQuerySQL = "select count(*) from edges where difficulty = "+difficulty+" and level = "+level+" and node1 != node2;";
        int count=-2;
        SQLiteDatabase db = myDbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(sumQuerySQL, null);
        if (cursor.moveToNext()) {
            sum = cursor.getInt(0);
        }

        cursor.close();
        cursor = db.rawQuery(countQuerySQL, null);
        if(cursor.moveToNext()){
            count = cursor.getInt(0);
        }

        Log.d(TAG, "check win: count = "+Integer.toString(count));
        Log.d(TAG, "check win: sum = "+Integer.toString(sum));

        if(sum>=count-1){
            win=true;
        }else{
            win = false;
        }

    }
}