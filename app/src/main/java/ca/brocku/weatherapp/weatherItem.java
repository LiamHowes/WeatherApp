// Name:            Liam Howes
// Student Number:  5880331
// Email:           lh15fh@brocku.ca
// Project:         WeatherApp

package ca.brocku.weatherapp;

public class weatherItem {
    private String location;
    private String temperature;
    private String description;
    private String date_and_time;
    private int wImage;

    public weatherItem(String loc, String temp, String desc, int image, String time){
        location = loc;
        temperature = temp;
        description = desc;
        wImage = image;
        date_and_time = time;
    }

    public String getLocation(){
        return location;
    }
    public String getTemperature(){
        return temperature;
    }
    public String getDescription(){ return description; }
    public String getDate_and_time(){return date_and_time;}
    public int getImage(){
        return wImage;
    }
}
