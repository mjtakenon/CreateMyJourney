package mjtakenon.createmyjourney;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import mjtakenon.createmyjourney.Const.*;

import static mjtakenon.createmyjourney.Const.*;

//目的地
public class Place implements Serializable{

    private static final long serialVersionUID = 1919810L;

    private Integer id;                  //訪れる時刻を変更するためのID
    private String  name;               //訪れる場所名
    private String  arriveTime;       //到着する時刻
    private Integer durationMinute;    //訪れている時間(分)
    private String  departTime;     //出発する時刻
    private transient LatLng  latLng;             //緯度経度
    private Integer type;               //場所の種類

    public Place(Place place) {
        this.id = place.getId();
        this.type = place.getType();
        this.name = place.getName();
        this.arriveTime = place.getarriveTime();
        this.durationMinute = place.getDurationMinute();
        this.departTime = place.getDepartTime();
        this.latLng = place.getLatLng();
    }

    public Place(Integer id, String name) {
        this.id = id;
        this.type = null;
        this.name = name;
        this.arriveTime = null;
        this.durationMinute = null;
        this.departTime = null;
        this.latLng = null;
    }

    public Place(Integer id, String name, Integer type) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.arriveTime = null;
        this.durationMinute = null;
        this.departTime = null;
        this.latLng = null;
    }

    public Place(Integer id, String name, Integer type, String arriveTime, Integer durationMinute, String departTime) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.arriveTime = arriveTime;
        this.durationMinute = durationMinute;
        this.departTime = departTime;
        this.latLng = null;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getType() { return type; }
    public String getarriveTime() { return arriveTime; }
    public Integer getDurationMinute() { return durationMinute; }
    public String getDepartTime() { return departTime; }
    public LatLng getLatLng() { return latLng; }

    public Integer getAddButtonId() { return id + ADDBUTTON_ID_BEGIN; }
    public Integer getTextMovingId() { return id + TEXTMOVING_ID_BEGIN; }
    public Integer getTextViewId() { return id + TEXTPLACE_ID_BEGIN; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(Integer type) { this.type = type; }
    public void setarriveTime(String arriveTime) { this.arriveTime = arriveTime; }
    public void setDurationMinute(Integer durationMinute) { this.durationMinute = durationMinute; }
    public void setDepartTime(String departTime) { this.departTime = departTime; }
    public void setLatLng(LatLng latlng) { this.latLng = latlng; }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("type", type);
        jsonObject.put("arriveTime", arriveTime);
        jsonObject.put("durationMinute", durationMinute);
        jsonObject.put("departTime", departTime);
        return jsonObject;
    }

    public static Integer TYPE_BEGIN =  0;
    public static Integer TYPE_DIST =   1;
    public static Integer TYPE_END =    2;
    public static Integer TYPE_UNKNOWN = -1;
}
