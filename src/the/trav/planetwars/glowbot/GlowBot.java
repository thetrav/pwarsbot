package the.trav.planetwars.glowbot;

import the.trav.planetwars.Planet;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static the.trav.planetwars.MyBotMain.PW;
import static the.trav.planetwars.reusable.Defence.*;
import static the.trav.planetwars.reusable.Attack.*;
import static the.trav.planetwars.reusable.Distances.*;
import static the.trav.planetwars.reusable.Constants.*;

public class GlowBot {
    public static final double GROWTH_IMPORTANCE = 1;
    public static final double DEFENCE_IMPORTANCE = 1;
    public static final double CLOSE_TO_ME_IMPORTANCE = 2;
    public static final double CLOSE_TO_ENEMY_IMPORTANCE = 1.5;
    public static final double CLOSE_TO_NEUTRAL_IMPORTANCE = 1;

    public void turn() {

        //don't lose anything
        defendPlanets();

        List<Planet> potentials = PW.NotMyPlanets();

        scorePlanets(potentials);

        attackPlanets(potentials);
    }

    public void scorePlanets(List<Planet> planets) {
        Collections.sort(planets, new Comparator<Planet>(){
            public int compare(Planet a, Planet b) {
                return (int)(scorePlanet(a) - scorePlanet(b));
            }
        });
    }

    //TODO: cache this calculation
    //TODO: introduce growth as an importance variable for distances
    public double scorePlanet(Planet p) {
        return scoreGrowth(p)
                    * scoreDefence(p)
                    * scoreNearToMe(p)
                    * scoreNearToEnemy(p)
                    * scoreNearToNeutral(p);
    }

    public double scoreGrowth(Planet p) {
        return p.GrowthRate() * GROWTH_IMPORTANCE;
    }

    public double scoreDefence(Planet p) {
        return (p.NumShips() + p.Owner() > MY_PLAYER_ID ? p.GrowthRate() : 0) * DEFENCE_IMPORTANCE;
    }

    public double scoreNearToMe(Planet p) {
        final double CLOSEST_EDGE_IMPORTANCE = 1;
        final double AVERAGE_DISTANCE_IMPORTANCE = 0.5;
        double closestEdge = distanceToNearestAlly(p) * CLOSEST_EDGE_IMPORTANCE;
        double averageDistance = averageDistanceToAllys(p) * AVERAGE_DISTANCE_IMPORTANCE;
        return closestEdge * averageDistance * CLOSE_TO_ME_IMPORTANCE;
    }

    public double scoreNearToEnemy(Planet p) {
        final double CLOSEST_EDGE_IMPORTANCE = 1;
        final double AVERAGE_DISTANCE_IMPORTANCE = 0.5;
        double closestEdge = distanceToNearestEnemy(p) * CLOSEST_EDGE_IMPORTANCE;
        double averageDistance = averageDistanceToEnemies(p) * AVERAGE_DISTANCE_IMPORTANCE;
        return closestEdge * averageDistance * CLOSE_TO_ENEMY_IMPORTANCE;
    }

    public double scoreNearToNeutral(Planet p) {
        final double CLOSEST_EDGE_IMPORTANCE = 1;
        final double AVERAGE_DISTANCE_IMPORTANCE = 0.5;
        double closestEdge = distanceToNearestNeutral(p) * CLOSEST_EDGE_IMPORTANCE;
        double averageDistance = averageDistanceToNeutrals(p) * AVERAGE_DISTANCE_IMPORTANCE;
        return closestEdge * averageDistance * CLOSE_TO_NEUTRAL_IMPORTANCE;
    }

    public void attackPlanets(List<Planet> targets) {
        for(Planet target : targets) {
            if(!capturePlanet(target)) {
                return;
            }
        }
    }
}
