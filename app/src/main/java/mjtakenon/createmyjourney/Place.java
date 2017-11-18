package mjtakenon.createmyjourney;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mjtak on 2017/11/15.
 */


//目的地
public class Place {
    private Integer id;                 //訪れる時刻を変更するためのID
    private String  name;               //訪れる場所名
    private String  arrivalTime;       //到着する時刻
    private Integer durationMinute;   //訪れている時間(分)
    private String  departureTime;     //出発する時刻
    private LatLng  latLng;           //緯度経度

    public Place(Integer id) {
        this.id = id;
    }

    public Place(Integer id, String name, String arrivalTime, Integer durationMinute, String departureTime) {
        this.id = id;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.durationMinute = durationMinute;
        this.departureTime = departureTime;
        this.latLng = null;
    }

    public Integer getId() {return id;}
    public String getName() {return name;}
    public String getArrivalTime() {return arrivalTime;}
    public Integer getDurationMinute() {return durationMinute;}
    public String getDepartureTime() {return departureTime;}
    public LatLng getLatLng() {return latLng;}

    public void setId(Integer id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setArrivalTime(String arrivalTime) {this.arrivalTime = arrivalTime;}
    public void setDurationMinute(Integer durationMinute) {this.durationMinute = durationMinute;}
    public void setDepartureTime(String departureTime) {this.departureTime = departureTime;}
    public void setPosition(LatLng latlng) {this.latLng = latlng;}
}
