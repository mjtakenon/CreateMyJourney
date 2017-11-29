package mjtakenon.createmyjourney;

import java.io.Serializable;
import java.util.ArrayList;

//目的地の集合体
public class Journey implements Serializable {

    private static final long serialVersionUID = 364364L;

    public Journey() {
        places = new ArrayList<Place>();
        name = null;
        beginDate = null;
    }

    public Journey(Journey journey) {
        this.places = journey.places;
        this.name = journey.name;
        this.beginDate = journey.beginDate;
    }

    public ArrayList<Place> getPlaces() {
        return places;
    }

    public String getName() {
        return name;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void addPlace(Place place){
        places.add(place);
    }

    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }


    private ArrayList<Place> places;
    private String name;
    private String beginDate;
}
