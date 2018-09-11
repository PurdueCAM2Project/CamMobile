package com.cam2.ryandevlin.worldview;

import java.io.Serializable;

/**
 * Created by RyanDevlin on 12/10/17.
 */

public class Camera implements Serializable{
    //String description;
    //String camera_type;
    String camera_id;
    double latitude;
    double longitude;
    String source_url;
    //String country;
    //String city;
    String address;

    //constructor for Camera class
    public Camera(String id){
        this.camera_id = id;
    }
    //public void des(String des){
      //  description = des;
    //}
    //public void cam_type(String cam_type){
      //  camera_type = cam_type;
    //}
    public void lat(double lat){
        latitude = lat;
    }
    public void lng(double lng){
        longitude = lng;
    }
    // cam_address holds formatted address of the camera location.
    public void cam_address(String cam_address){
        address=cam_address;
    }
    public void cam_url(String cam_url){
        source_url = cam_url;
    }
    //public void cam_country(String cam_country){
      //  country = cam_country;
    //}
    //public void cam_city(String cam_city){
      //  city = cam_city;
    //}

    @Override
    public String toString() {
        return this.address;
    }
}
