package com.cam2.ryandevlin.worldview;

import java.io.Serializable;

/**
 * Created by RyanDevlin on 12/10/17.
 * Updated by Rohith Ravindanath 10//05/18
 */

public class Camera implements Serializable{

    //GLOBAL VARIABLES
    double latitude;
    double longitude;
    String cameraID;
    String sourceURL;
    String address;

    /**
     * Constructor to create a Camera Object
     * @param id
     */
    public Camera(String id){
        this.cameraID = id;
    }

    /**
     * Constructor to create a Camera Object
     * @param id
     * @param lat
     * @param lng
     * @param address
     * @param url
     */
    public Camera(String id, double lat, double lng, String address, String url){
        this.cameraID = id;
        this.latitude = lat;
        this.longitude = lng;
        this.address = address;
        this.sourceURL = url;
    }

    /**
     * Setting method for latitude coordinates
     * @param lat
     */
    public void setLatitude(double lat){
        latitude = lat;
    }

    /**
     * Setting method for longitude coordinates
     * @param lng
     */
    public void setLongitude(double lng){
        longitude = lng;
    }

    /**
     * Setting method for camera's address
     * @param cam_address
     */
    public void setCameraAddress(String cam_address){
        address = cam_address;
    }

    /**
     * Setting method for camera's url
     * @param cam_url
     */
    public void setCameraUrl(String cam_url){
        sourceURL = cam_url;
    }

    /**
     * Returns a string representation of the object
     * @return  string representation of the object
     */
    @Override
    public String toString() {
        return this.address;
    }
}
