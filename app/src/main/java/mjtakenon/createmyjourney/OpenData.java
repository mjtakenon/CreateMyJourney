package mjtakenon.createmyjourney;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

// オープンデータ1つ
public class OpenData implements Serializable{

    private static final long serialVersionUID = 114514L;


    public OpenData(Integer id, String attribute, String name, String address, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.attribute = attribute;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recommend = null;
    }

    public OpenData(Integer id, String attribute, String name, String address) {
        this.id = id;
        this.name = name;
        this.attribute = attribute;
        this.address = address;
        this.latitude = null;
        this.longitude = null;
        this.recommend = null;
    }

    public OpenData(Integer id, String attribute, String name) {
        this.id = id;
        this.name = name;
        this.attribute = attribute;
        this.address = null;
        this.latitude = null;
        this.longitude = null;
        this.recommend = null;
    }

    public Integer getId() { return id; }
    public String getAttribute() { return attribute; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getRecommend() { return recommend; }

    public void setId(Integer id) { this.id = id; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setRecommend(Double recommend) { this.recommend = recommend; }

    private Integer id;
    private String attribute;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double recommend;
}
