package the.trav.planetwars.reusable;

import the.trav.planetwars.Planet;

import java.util.*;

import static the.trav.planetwars.MyBotMain.PW;

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

    public static double distanceToNearest(Planet source, List<Planet> potentials) {
        double distance = Double.MAX_VALUE;
        for(Planet enemy : potentials) {
            double d = distance(source, enemy);
            if(d < distance) {
                distance = d;
            }
        }
        return distance;
    }

    public static double distanceToNearestEnemy(Planet source) {
        return distanceToNearest(source, PW.EnemyPlanets());
    }

    public static double distanceToNearestAlly(Planet source) {
        return distanceToNearest(source, PW.MyPlanets());
    }

    public static double distanceToNearestNeutral(Planet source) {
        return distanceToNearest(source, PW.NeutralPlanets());
    }

    public static double averageDistance(Planet source, List<Planet> others) {
        double sum = 0;
        for(Planet a : others) {
            sum += distance(source, a);
        }
        return sum / others.size();
    }

    public static double averageDistanceToAllys(Planet p) {
        return averageDistance(p, PW.MyPlanets());
    }

    public static double averageDistanceToEnemies(Planet p) {
        return averageDistance(p, PW.EnemyPlanets());
    }

    public static double averageDistanceToNeutrals(Planet p) {
        return averageDistance(p, PW.NeutralPlanets());
    }

    public static void sortByDistanceToNearestAlly(List<Planet> potentials) {
        Collections.sort(potentials, new Comparator<Planet>() {
            public int compare(Planet a, Planet b) {
                return (int)(distanceToNearestAlly(a) - distanceToNearestAlly(b));
            }
        });
    }
}
