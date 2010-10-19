package the.trav.planetwars;

import the.trav.planetwars.glowbot.GlowBot;

import java.util.logging.Logger;

public class MyBotMain {

    private static final Logger LOG = Logger.getLogger("MAIN");
    public static PlanetWars PW = null;

    public static void main(String[] args)  throws Exception{
        final GlowBot bot = new GlowBot();
        StringBuffer line = new StringBuffer();
        StringBuffer message = new StringBuffer();
        int c;
        try {
            while ((c = System.in.read()) >= 0) {
                switch (c) {
                    case '\n':
                        if (line.toString().equals("go")) {
                            PW = new PlanetWars(message.toString());
                            bot.turn();
                            PW.FinishTurn();
                            message = new StringBuffer();
                        } else {
                            message.append(line).append("\n");
                        }
                        line = new StringBuffer();
                        break;
                    default:
                        line.append((char) c);
                        break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void debug(String s) {
        System.err.println(s);
    }
}
