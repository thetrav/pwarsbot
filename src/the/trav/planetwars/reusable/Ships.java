package the.trav.planetwars.reusable;

import the.trav.planetwars.Fleet;
import the.trav.planetwars.Planet;

import java.util.*;

import static the.trav.planetwars.MyBotMain.PW;
import static the.trav.planetwars.reusable.Distances.*;

public class Ships {
    private static Map<Integer, PlanetUsage> usage = new HashMap<Integer, PlanetUsage>();

    public static void clearUsage() {
        usage = new HashMap<Integer, PlanetUsage>();
    }

    public static void claim(Planet p, int ships) {
        usage(p).claimedFleets += ships;
    }


    public static int myShipsEnRoute(Planet dest, int maxEta) {
        int counter = 0;
        for(Fleet fleet : PW.MyFleets()) {
            if(fleet.DestinationPlanet() == dest.PlanetID() && fleet.TurnsRemaining() <= maxEta) {
                counter += fleet.NumShips();
            }
        }
        return counter;
    }

    static class PlanetUsage {
        int claimedFleets;
    }

    public static PlanetUsage usage(Planet p) {
        PlanetUsage u = usage.get(p.PlanetID());
        if (u != null) return u;
        PlanetUsage nu = new PlanetUsage();
        usage.put(p.PlanetID(), nu);
        return nu;
    }

    public static int availableShips(Planet p) {
        return p.NumShips() - usage(p).claimedFleets;
    }

    public static BattleGroup findReinforcements(int required, Planet dest, int time) {
        int maxDistance = time * time; //fleets move at 1 per turn, time is therefore Math.sqrt(distance^2) squaring is faster than rooting
        BattleGroup battleGroup = new BattleGroup();
        List<Planet> potentials = planetsWithShipsInRange(dest, maxDistance);
        sortByDistanceTo(dest, potentials);
        sortByDistanceToNearestEnemy(potentials);
        assignShips(required, potentials, battleGroup);
        return battleGroup;
    }

    private static List<Planet> planetsWithShipsInRange(Planet source, double maxDistance) {
        List<Planet> potentials = new ArrayList<Planet>();
        for(Planet p : PW.MyPlanets()) {
            //we want a list of planets furtherest from the enemy and furtherest from this planet but still in range
            if(availableShips(p) == 0){
//                LOG.info("no ships available");
                continue;
            }
            int distance = (int)distance(p, source);
//            LOG.info("distance="+distance+" max="+maxDistance);
            if(distance > maxDistance){
//                LOG.info("not in range");
                continue;
            }
            potentials.add(p);
        }
        return potentials;
    }

    public static void assignShips(int required, List<Planet> sources, BattleGroup group) {
        for(Planet p : sources) {
            int ships = availableShips(p);
            if(ships <= 0) continue;
            if(ships < required) {
//                LOG.info("adding all ships");
                group.addShips(p, ships);
                required -= ships;
            } else {
//                LOG.info("adding required ships");
                group.addShips(p, required);
                required = 0;
            }
            if(required <= 0) {
                return;
            }
        }
    }

    /**
     * closest last
     * @param dest
     * @param potentials
     */
    private static void sortByDistanceTo(final Planet dest, List<Planet> potentials) {
        Collections.sort(potentials, new Comparator<Planet>() {
            public int compare(Planet a, Planet b) {
                return (int)(distance(a, dest) - distance(b, dest));
            }
        });
    }

    /**
     * closest last
     * @param potentials
     */
    private static void sortByDistanceToNearestEnemy(List<Planet> potentials) {
        Collections.sort(potentials, new Comparator<Planet>() {
            public int compare(Planet a, Planet b) {
                return (int)(distanceToNearestEnemy(a) - distanceToNearestEnemy(b));
            }
        });
    }

    public static double distanceToNearestEnemy(Planet source) {
        double distance = Double.MAX_VALUE;
        for(Planet enemy : PW.EnemyPlanets()) {
            double d = distance(source, enemy);
            if(d < distance) {
                distance = d;
            }
        }
        return distance;
    }
}
