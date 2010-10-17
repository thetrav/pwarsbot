package the.trav.planetwars;

import java.util.*;
import java.util.logging.Logger;


/**
 * finds nearest planet not owned and launches all out offensive against it, probably not the best bot
 */
public class Bot {

    static final Logger LOG = Logger.getLogger(Bot.class.getName());

    //By convention the current player is always player 1
    static final int MY_PLAYER_ID = 1;
    static final int NO_PLAYER_ID = 0;

    private PlanetWars input;

    private Map<String, Double> planetDistances = new HashMap<String, Double>();

    private Map<Integer, PlanetUsage> usage = new HashMap<Integer, PlanetUsage>();

    public void turn(PlanetWars input) {
        this.input = input;
        usage = new HashMap<Integer, PlanetUsage>();

        defendPlanets();

        capturePlanets(3);

        LOG.info("completed turn");
    }

    private void defendPlanets() {
        for (Fleet f : input.EnemyFleets()) {
            Planet dest = input.GetPlanet(f.DestinationPlanet());
            if(dest.Owner() == MY_PLAYER_ID) {
//                LOG.info("planet:"+dest.PlanetID() + " under attack");
                int shipsGrownByArrival = f.TurnsRemaining() * dest.GrowthRate() + myShipsEnRoute(dest, f.TurnsRemaining());
                if(shipsGrownByArrival <= f.NumShips())
                {
                    int deficit = f.NumShips() - shipsGrownByArrival - dest.NumShips();
                    BattleGroup battleGroup = findReinforcements(deficit, dest, f.TurnsRemaining());
                    if(battleGroup.count < deficit) {
                        //planet is probably fucked, use that to your advantage
                    } else {
                        battleGroup.dispatch(dest);
                        claim(dest, dest.NumShips());
                    }
                }
            }
        }
    }

    private void capturePlanets(int targets) {
        List<Planet> potentials = input.NotMyPlanets();
        sortByDistanceToNearestAlly(potentials);
        for(int i=0; i< targets; i++) {
            LOG.info("target:"+i);
            Planet target = potentials.get(i);
            if(!capturePlanet(target)) {
                return; 
            }
        }
    }

    private boolean capturePlanet(Planet target) {
//        LOG.info("target found id"+target.PlanetID());
        if(target.Owner() == NO_PLAYER_ID) {
            BattleGroup invasion = findReinforcements(target.NumShips(), target, 10000);
            if(invasion.count >= target.NumShips()) {
                LOG.info("destroying pathetic neutrals");
                invasion.dispatch(target);
                LOG.info("done");
                return true;
            } else {
//                LOG.info("target too well defended can only muster" + invasion.count+" ships but need "+target.NumShips());
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
//                    LOG.info("crush the opposition!");
                    invasion.dispatch(target);
                    return true;
                } else {
//                    LOG.info("curse their black hearts, another day perhaps");
                    return false;
                }
            } else {
//                LOG.info("their fate is already sealed");
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

    private int myShipsEnRoute(Planet dest, int maxEta) {
        int counter = 0;
        for(Fleet fleet : input.MyFleets()) {
            if(fleet.DestinationPlanet() == dest.PlanetID() && fleet.TurnsRemaining() <= maxEta) {
                counter += fleet.NumShips();
            }
        }
        return counter;
    }

    private int enemiesEnRoute(Planet target, double etaForBackup) {
        int counter = 0;
        for(Fleet f : input.EnemyFleets()) {
            if (f.DestinationPlanet() == target.PlanetID() && f.TurnsRemaining() <= etaForBackup) {
                counter += f.NumShips();
            }
        }
        return counter;
    }

    private BattleGroup findReinforcements(int required, Planet dest, int time) {
        int maxDistance = time * time; //fleets move at 1 per turn, time is therefore Math.sqrt(distance^2) squaring is faster than rooting
        BattleGroup battleGroup = new BattleGroup();
        List<Planet> potentials = planetsWithShipsInRange(dest, maxDistance);
        sortByDistanceTo(dest, potentials);
        sortByDistanceToNearestEnemy(potentials);
        assignShips(required, potentials, battleGroup);
        return battleGroup;
    }

    private void assignShips(int required, List<Planet> sources, BattleGroup group) {
        for(Planet p : sources) {
            int ships = p.NumShips();
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

    private List<Planet> planetsWithShipsInRange(Planet source, double maxDistance) {
        List<Planet> potentials = new ArrayList<Planet>();
        for(Planet p : input.MyPlanets()) {
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

    /**
     * closest last
     * @param potentials
     */
    private void sortByDistanceToNearestEnemy(List<Planet> potentials) {
        Collections.sort(potentials, new Comparator<Planet>() {
            public int compare(Planet a, Planet b) {
                return (int)(distanceToNearestEnemy(a) - distanceToNearestEnemy(b));
            }
        });
    }

    private double distanceToNearestEnemy(Planet source) {
        double distance = Double.MAX_VALUE;
        for(Planet enemy : input.EnemyPlanets()) {
            double d = distance(source, enemy);
            if(d < distance) {
                distance = d;
            }
        }
        return distance;
    }

    /**
     * closest last
     * @param dest
     * @param potentials
     */
    private void sortByDistanceTo(final Planet dest, List<Planet> potentials) {
        Collections.sort(potentials, new Comparator<Planet>() {
            public int compare(Planet a, Planet b) {
                return (int)(distance(a, dest) - distance(b, dest));
            }
        });
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
        for(Planet a: input.MyPlanets()) {
            double d = distance(p, a);
            if(d < distance) {
                distance = d;
            }
        }
        return distance;
    }

    class BattleGroup {
        int count = 0;
        List<Pair> ships = new LinkedList<Pair>();
        public void addShips(Planet p, int s) {
            ships.add(new Pair(p,s));
            count+= s;
        }

        public void dispatch(Planet dest) {
            for(Pair p : ships) {
                p.launch(dest);
            }
        }

        class Pair {
            Pair(Planet p, int s) {this.p = p; this.s = s;}
            int s; Planet p;
            public void launch(Planet dest) {
                input.IssueOrder(p, dest, s);
                claim(p, s);
            }
        }
    }

    private void claim(Planet p, int ships) {
        usage(p).claimedFleets += ships;
    }

    private int availableShips(Planet p) {
        return p.NumShips() - usage(p).claimedFleets;
    }

    private PlanetUsage usage(Planet p) {
        PlanetUsage u = usage.get(p.PlanetID());
        if (u != null) return u;
        PlanetUsage nu = new PlanetUsage();
        usage.put(p.PlanetID(), nu);
        return nu;
    }

    class PlanetUsage {
        int claimedFleets;
    }

    class PotentialTarget {
        public PotentialTarget(Planet target, Planet source) {
            this.target = target;
            this.source = source;
        }

        public Planet target;
        public Planet source;
    }

    private PotentialTarget findNearest() {
        double nearestDistance = Double.MAX_VALUE;
        Planet nearestTarget = null;
        Planet nearestSource = null;
        for (Planet p : input.NotMyPlanets()) {
//            if(p.GrowthRate() > 0) {
                for (Planet mp : input.MyPlanets()) {
                    double distance = distance(p, mp);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestSource = mp;
                        nearestTarget = p;
                    }
//                }
            }
        }
        return new PotentialTarget(nearestTarget, nearestSource);
    }

    private String key(Planet a, Planet b) {
        return a.PlanetID()+"_"+b.PlanetID();
    }


    private double distance(Planet a, Planet b) {
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

    private int calcDefence(Fleet f, Planet p) {
        if (p.Owner() == 0) {
            return p.NumShips();
        }
        return p.GrowthRate() * f.TurnsRemaining();
    }

    private int calcReturn() {
        //base growth
        //time to reach
        //conquer cost
        //if opponents planet
        //opponents relative strength & reinforcement ability
        return 0;
    }
}
