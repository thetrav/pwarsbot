package the.trav.planetwars.reusable;

import the.trav.planetwars.Fleet;
import the.trav.planetwars.Planet;

import static the.trav.planetwars.MyBotMain.PW;
import static the.trav.planetwars.reusable.Constants.*;
import static the.trav.planetwars.reusable.Distances.*;
import static the.trav.planetwars.reusable.Ships.*;

public class Attack {
    public static boolean capturePlanet(Planet target) {
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

    public static int maxOpposition(Planet target, double etaForBackup) {
        int ships = target.NumShips();
        ships += target.GrowthRate() + etaForBackup;
        ships += enemiesEnRoute(target, etaForBackup);
        ships -= myShipsEnRoute(target, (int)etaForBackup);
        return ships;
    }


    public static int enemiesEnRoute(Planet target, double etaForBackup) {
        int counter = 0;
        for(Fleet f : PW.EnemyFleets()) {
            if (f.DestinationPlanet() == target.PlanetID() && f.TurnsRemaining() <= etaForBackup) {
                counter += f.NumShips();
            }
        }
        return counter;
    }

    
}
