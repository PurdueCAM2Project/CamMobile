package com.cam2.ryandevlin.worldview;

/**
 * Created by RyanDevlin on 12/10/17.
 */

public class Camera{
    String description;
    String camera_type;
    int camera_id;
    double latitude;
    double longitude;
    String source_url;
    String country;
    String city;

    //constructor for Camera class
    public Camera(int id){
        this.camera_id = id;
    }

    public void des(String des){
        description = des;
    }
    public void cam_type(String cam_type){
        camera_type = cam_type;
    }
    public void lat(double lat){
        latitude = lat;
    }
    public void lng(double lng){
        longitude = lng;
    }
    public void cam_url(String cam_url){
        source_url = cam_url;
    }
    public void cam_country(String cam_country){
        country = cam_country;
    }
    public void cam_city(String cam_city){
        city = cam_city;
    }

}
