// Name:            Liam Howes
// Student Number:  5880331
// Email:           lh15fh@brocku.ca
// Project:         WeatherApp

package ca.brocku.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class weatherAdapter extends RecyclerView.Adapter<weatherAdapter.weatherViewHolder> {
    private ArrayList<weatherItem> weatherList;

    public static class weatherViewHolder extends RecyclerView.ViewHolder{

        public TextView wLocation;
        public TextView wTemperature;
        public TextView wDescription;
        public TextView wDate_and_Time;
        public ImageView wImage;

        public weatherViewHolder(@NonNull View itemView) {
            super(itemView);
            wLocation = itemView.findViewById(R.id.locationName);
            wTemperature = itemView.findViewById(R.id.temperature);
            wDescription = itemView.findViewById(R.id.description);
            wImage = itemView.findViewById(R.id.weather_image);
            wDate_and_Time = itemView.findViewById(R.id.dateAndTime);
        }
    }

    public weatherAdapter(ArrayList<weatherItem> wList){
        weatherList = wList;
    }

    @NonNull
    @Override
    public weatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false);
        weatherViewHolder wvh = new weatherViewHolder(view);
        return wvh;
    }

    @Override
    public void onBindViewHolder(@NonNull weatherViewHolder holder, int position) {
        weatherItem currentItem = weatherList.get(position); // get position
        holder.wLocation.setText(currentItem.getLocation()); // get all the values
        holder.wTemperature.setText(currentItem.getTemperature());
        holder.wDescription.setText(currentItem.getDescription());
        holder.wImage.setImageResource(currentItem.getImage());
        holder.wDate_and_Time.setText(currentItem.getDate_and_time());

    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }
}
