package com.example.zhaoguoqian.materialdesign;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

public class SettingActivity extends PreferenceActivity {
    private String TAG = "Setting Activity";
    private DataBaseHelper myDbHelper;
    private ListPreference difficultyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // open the database for modification
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

        // set the default value for the difficulty list
        difficultyList = (ListPreference) findPreference (getString(R.string.key_difficulty));
        if(difficultyList.getValue()==null) {
            difficultyList.setValueIndex(0);
        }
        // put the difficulty preference in the shared preference
        SharedPreferences mPrefs = getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Integer.toString(R.string.key_difficulty), difficultyList.getValue());
        editor.apply();

        // create a dialog for the reset button
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Confirmation for Reset");
        //set content
        alertDialogBuilder
                .setMessage("Are you sure to reset all the data?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        SQLiteDatabase db = myDbHelper.getWritableDatabase();

                        // set all the edges to unconnected
                        String sqlResetEdge = "UPDATE edges SET connected=0;";
                        db.execSQL(sqlResetEdge);

                        // set all the level (excluding the level 1) to locked
                        String sqlResetLevel1 = "UPDATE levelState SET state=0;";
                        String sqlResetLevel2 = "UPDATE levelState SET state=1 WHERE level=1;";
                        db.execSQL(sqlResetLevel1);
                        db.execSQL(sqlResetLevel2);

                        // show the users that it has finished
                        Toast.makeText(MyApplication.getContext(), "Data has been reset.", Toast.LENGTH_SHORT).show();
                        db.close();
                    }
                })
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //do nothing
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // reset function
        Preference button = findPreference(getString(R.string.key_reset));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                alertDialog.show();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("difficulty", difficultyList.getValue());
        setResult(RESULT_OK, intent);
        finish();
    }

    // set some edges to connected to test whether the reset button works
    private void resetTestInitiate(){
        SQLiteDatabase db = myDbHelper.getWritableDatabase();
        String sqlUpdateTest = "UPDATE edges SET connected=1 WHERE level = 3";
        db.execSQL(sqlUpdateTest);
        Log.v(TAG, "ID updated");
        db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDbHelper.close();
    }
}