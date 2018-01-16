package com.sanvenir.positiongetter;

import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sanvenir on 1/16/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class UserData {
    static Map<String, UserData> allUserData = new HashMap<>();
    private static String trackingUser = null;
    private String userAlias;
    private Double latitude;
    private Double longitude;
    private Double elevation;

    public static void setAllUserData(String[] userInfo) {
        allUserData = new HashMap<>();
        if(userInfo.length > 0) {
            for(String info : userInfo) {
                UserData userData = new UserData(info);
                UserData.allUserData.put(userData.getUserAlias(), userData);
            }
        }
    }

    public UserData(Location location) {
        userAlias = "";
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        elevation = location.getAltitude();
    }

    public UserData(String info) {
        String[] infoList = info.split(",");
        userAlias = infoList[0];
        latitude = Double.valueOf(infoList[1]);
        longitude = Double.valueOf(infoList[2]);
        elevation = Double.valueOf(infoList[3]);
    }

    public double getDistance(UserData userData) {
        return Math.sqrt(
                Math.pow(this.getLatitude() - userData.getLatitude(), 2) +
                        Math.pow(this.getLongitude() - userData.getLongitude(), 2)
        );
    }

    public static String getTrackingUser() {
        return trackingUser;
    }

    public static void setTrackingUser(String trackingUser) {
        UserData.trackingUser = trackingUser;
    }

    public String toString(){
        return userAlias + "\tLa: " + latitude + " Lo: " + longitude + " El: " + elevation;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getUserAlias() {
        return userAlias;
    }

    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }
}
