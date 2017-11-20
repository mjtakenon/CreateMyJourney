package mjtakenon.createmyjourney;

import com.google.android.gms.maps.model.LatLng;
import mjtakenon.createmyjourney.Const.*;

import static mjtakenon.createmyjourney.Const.*;

/**
 * Created by mjtak on 2017/11/15.
 */


//目的地
public class Place {
    private Integer id;                  //訪れる時刻を変更するためのID
    private String  name;               //訪れる場所名
    private String  arrivalTime;       //到着する時刻
    private Integer durationMinute;    //訪れている時間(分)
    private String  departureTime;     //出発する時刻
    private LatLng  latLng;             //緯度経度
    private Integer type;               //場所の種類

    public Place(Place place) {
        this.id = place.getId();
        this.type = place.getType();
        this.name = place.getName();
        this.arrivalTime = place.getArrivalTime();
        this.durationMinute = place.getDurationMinute();
        this.departureTime = place.getDepartureTime();
        this.latLng = place.getLatLng();
    }

    public Place(Integer id, String name) {
        this.id = id;
        this.type = null;
        this.name = name;
        this.arrivalTime = null;
        this.durationMinute = null;
        this.departureTime = null;
        this.latLng = null;
    }

    public Place(Integer id, String name, Integer type) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.arrivalTime = null;
        this.durationMinute = null;
        this.departureTime = null;
        this.latLng = null;
    }

    public Place(Integer id, String name, Integer type, String arrivalTime, Integer durationMinute, String departureTime) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.durationMinute = durationMinute;
        this.departureTime = departureTime;
        this.latLng = null;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getType() { return type; }
    public String getArrivalTime() { return arrivalTime; }
    public Integer getDurationMinute() { return durationMinute; }
    public String getDepartureTime() { return departureTime; }
    public LatLng getLatLng() { return latLng; }

    public Integer getAddButtonId() { return id + ADDBUTTON_ID_BEGIN; }
    public Integer getTextViewId() { return id + TEXTVIEW_ID_BEGIN; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(Integer type) { this.type = type; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setDurationMinute(Integer durationMinute) { this.durationMinute = durationMinute; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setLatLng(LatLng latlng) { this.latLng = latlng; }

    public static Integer TYPE_BEGIN =  0;
    public static Integer TYPE_DIST =   1;
    public static Integer TYPE_END =    2;
    public static Integer TYPE_UNKNOWN = -1;
}
