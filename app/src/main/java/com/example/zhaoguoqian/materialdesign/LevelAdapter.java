package com.example.zhaoguoqian.materialdesign;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class LevelAdapter extends  RecyclerView.Adapter<LevelAdapter.ViewHolder>{
    private List<Level> mLevelList;
    private Activity activity;
    private ViewHolder holder;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View levelView;
        ImageView levelImage;
        TextView levelName;
        Activity activity;

        public ViewHolder (View view, Activity activity){
            super(view);
            levelView = view;
            levelImage = view.findViewById(R.id.level_image);
            levelName = view.findViewById(R.id.level_name);
            this.activity = activity;
        }

    }

    public LevelAdapter(List<Level> levelList, Activity activity){
        this.mLevelList = levelList;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.level_item, parent, false);

        // add onClick action to the holder
        final ViewHolder holder = new ViewHolder(view, activity);
        holder.levelView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position = holder.getAdapterPosition();
                if(mLevelList.get(position).getState()!=0){
                    Intent intent = new Intent();
                    intent.putExtra("level", position);
                    activity.setResult(RESULT_OK, intent);
                    activity.finish();
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){

        Level level = mLevelList.get(position);
        holder.levelImage.setImageResource(Level.getImage(level));
        holder.levelName.setText(level.getName());
    }

    @Override
    public int getItemCount(){
        return mLevelList.size();
    }

    public ViewHolder getHolder() {
        return holder;
    }

    public List<Level> getmLevelList() {
        return mLevelList;
    }

    public Activity getActivity() {
        return activity;
    }
}