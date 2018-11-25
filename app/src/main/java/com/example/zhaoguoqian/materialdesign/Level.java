package com.example.zhaoguoqian.materialdesign;

public class Level {
    // difficulty: 1-easy 2-normal 3-hard
    private int difficulty;

    private String name;

    // state: 0-lock 1-playing 2-done
    private int state;
    private int number;

    public Level(int difficulty, int number, int state){
        this.difficulty = difficulty;
        this.number = number;
        this.name = "Level "+ number;
        this.state = state;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static int getImage(Level level){
        switch(level.getState()){
            case 0:
                return R.drawable.ic_lock_black_24dp;
            case 1:
                return R.drawable.ic_star_border_black_24dp;
            case 2:
                return R.drawable.ic_star_black_24dp;
            default:
                return R.drawable.ic_star_border_black_24dp;
        }
    }
}
