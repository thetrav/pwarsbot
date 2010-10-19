package the.trav.planetwars.reusable;

import the.trav.planetwars.Planet;

import java.util.LinkedList;
import java.util.List;

import static the.trav.planetwars.MyBotMain.PW;

public class BattleGroup {
    public int count = 0;
    List<Pair> ships = new LinkedList<Pair>();
    public void addShips(Planet p, int s) {
        if(s <= 0) throw new IllegalArgumentException("must add a positive number of ships: " + s);
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
            PW.IssueOrder(p, dest, s);
            Ships.claim(p, s);
        }
    }
}
