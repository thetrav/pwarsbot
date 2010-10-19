package the.trav.planetwars.reusable;

import the.trav.planetwars.Fleet;
import the.trav.planetwars.Planet;

import java.util.logging.Logger;

import static the.trav.planetwars.reusable.Constants.*;
import static the.trav.planetwars.MyBotMain.PW;

public class Defence {

    private static final Logger LOG = Logger.getLogger(Defence.class.getName());

    public static void defendPlanets() {
        for (Fleet f : PW.EnemyFleets()) {
            Planet dest = PW.GetPlanet(f.DestinationPlanet());
            if(dest.Owner() == MY_PLAYER_ID) {
//                LOG.info("planet:"+dest.PlanetID() + " under attack");
                int shipsOnPlanet = Ships.availableShips(dest);
                int shipsGrownByArrival = f.TurnsRemaining() * dest.GrowthRate();
                int shipsEnRoute = Ships.myShipsEnRoute(dest, f.TurnsRemaining());
                int shipsOnArrival = shipsOnPlanet + shipsGrownByArrival + shipsEnRoute;
                if(shipsOnArrival < f.NumShips())
                {
                    int deficit = f.NumShips() - shipsOnArrival;
                    if(deficit <= 0) {
                        LOG.info(f.NumShips() + " incoming, when they arrive we will only have "+shipsOnArrival + " defenders, need "+deficit+" reinforcements");
                    }
                    BattleGroup battleGroup = Ships.findReinforcements(deficit, dest, f.TurnsRemaining());
                    if(battleGroup.count < deficit) {
                        //planet is probably fucked, use that to your advantage
                    } else {
                        battleGroup.dispatch(dest);
                        Ships.claim(dest, dest.NumShips());
                    }
                }
            }
        }
    }
}
