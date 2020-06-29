package com.example.finalproject;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Direction {

    public ArrayList<Route> routes;

    public static class Route {
        public ArrayList<Leg> legs;
    }

    public static class Leg {
        public Distance distance;
        public Duration duration;
        public String end_address;
        public LatLng end_location;
        public String start_address;
        public LatLng start_location;
        public List<LatLng> steps;
    }

    public static class Distance {
        public String text;
        public int value;
        public Distance(String text, int value) {
            this.text = text;
            this.value = value;
        }
    }

    public static class Duration {
        public String text;
        public int value;
        public Duration(String text, int value) {
            this.text = text;
            this.value = value;
        }
    }

    public static class Location {
        public double lat;
        public double lng;

        public Location(double lat, double lng) {
            this.lat=lat;
            this.lng=lng;
        }
    }

    public static class Step {
        public Distance distance;
        public Duration duration;
        public Location end_location;
        public Location start_location;
        public String html_instructions;
    }
}
