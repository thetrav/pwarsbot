package the.trav.planetwars.dumbbot;

import the.trav.planetwars.Planet;
import the.trav.planetwars.reusable.Defence;

import java.util.*;
import java.util.logging.Logger;

import static the.trav.planetwars.reusable.Distances.*;
import static the.trav.planetwars.reusable.Attack.*;
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

    

    

}
