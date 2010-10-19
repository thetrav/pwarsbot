package the.trav.planetwars.reusable;

import the.trav.planetwars.Planet;

import java.util.HashMap;
import java.util.Map;

public class Distances {
    private static Map<String, Double> planetDistances = new HashMap<String, Double>();

    public static double distance(Planet a, Planet b) {
        String key = key(a,b);
        Double d = planetDistances.get(key);
        if(d != null) {
            return d;
        }
        double x = b.X() - a.X();
        double y = b.Y() - a.Y();
        Double distance = new Double(x * x + y * y);
        planetDistances.put(key, distance);
        planetDistances.put(key(b,a), distance);
        return distance;
    }

    public static String key(Planet a, Planet b) {
        return a.PlanetID()+"_"+b.PlanetID();
    }
}
