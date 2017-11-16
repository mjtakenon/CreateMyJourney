package mjtakenon.createmyjourney;

/**
 * Created by mjtak on 2017/11/15.
 */


//目的地
public class Place {
    private int     id;           //訪れる時刻を変更するためのID
    private String  time;        //到着する時刻
    private int     duration;   //訪れている時間(分)
    private String  name;        //訪れる場所名

    public Place(int id) {
        this.id = id;
    }

    public Place(int id, String name, String time, int duration) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.duration = duration;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getTime() {return time;}
    public int getDuration() {return duration;}

    public void setId(int id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setTime(String time) {this.time = time;}
    public void setDuration(int duration) {this.duration = duration;}
}
