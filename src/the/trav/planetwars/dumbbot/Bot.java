package the.trav.planetwars.dumbbot;

import the.trav.planetwars.Fleet;
import the.trav.planetwars.Planet;
import the.trav.planetwars.PlanetWars;
import the.trav.planetwars.reusable.BattleGroup;
import the.trav.planetwars.reusable.Defence;

import java.util.*;
import java.util.logging.Logger;

import static the.trav.planetwars.reusable.Constants.*;
import static the.trav.planetwars.reusable.Distances.*;
import static the.trav.planetwars.reusable.Ships.*;
import static the.trav.planetwars.MyBotMain.*;

/**
 * finds nearest planet not owned and launches all out offensive against it, probably not the best bot
 */
public class Bot {

    static final Logger LOG = Logger.getLogger(Bot.class.getName());


    public void turn() {

        Defence.defendPlanets();

        capturePlanets(3);
    }


    private void capturePlanets(int targets) {
        List<Planet> potentials = PW.NotMyPlanets();
        sortByDistanceToNearestAlly(potentials);
        for(int i=0; i< targets; i++) {
            Planet target = potentials.get(i);
            if(!capturePlanet(target)) {
                return; 
            }
        }
    }

    private boolean capturePlanet(Planet target) {
        if(target.Owner() == NO_PLAYER_ID) {
            int neededShips = target.NumShips() + 1;
            BattleGroup invasion = findReinforcements(neededShips, target, 10000);
            if(invasion.count >= neededShips) {
                invasion.dispatch(target);
                return true;
            } else {
                return false;
            }
        } else {
            //how close is it to its reinforcements
            double nearestBackup = distanceToNearestEnemy(target);
            int etaForBackup = (int)Math.sqrt(nearestBackup);
            int ships = maxOpposition(target, nearestBackup);
            if(ships > 0) {
                BattleGroup invasion = findReinforcements(ships, target, etaForBackup);
                if(invasion.count >= ships) {
                    invasion.dispatch(target);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private int maxOpposition(Planet target, double etaForBackup) {
        int ships = target.NumShips();
        ships += target.GrowthRate() + etaForBackup;
        ships += enemiesEnRoute(target, etaForBackup);
        ships -= myShipsEnRoute(target, (int)etaForBackup);
        return ships;
    }


    private int enemiesEnRoute(Planet target, double etaForBackup) {
        int counter = 0;
        for(Fleet f : PW.EnemyFleets()) {
            if (f.DestinationPlanet() == target.PlanetID() && f.TurnsRemaining() <= etaForBackup) {
                counter += f.NumShips();
            }
        }
        return counter;
    }

    private void sortByDistanceToNearestAlly(List<Planet> potentials) {
        Collections.sort(potentials, new Comparator<Planet>() {
            public int compare(Planet a, Planet b) {
                return (int)(distanceToNearestAlly(a) - distanceToNearestAlly(b));
            }
        });
    }

    private double distanceToNearestAlly(Planet p) {
        double distance = Double.MAX_VALUE;
        for(Planet a: PW.MyPlanets()) {
            double d = distance(p, a);
            if(d < distance) {
                distance = d;
            }
        }
        return distance;
    }

}
