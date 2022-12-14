package scattertory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class UniversalFunctions {
    public static ArrayList<Player> getAllButMe(Player p) {
        Player[] players = new Player[Bukkit.getOnlinePlayers().size()];
        players = (Player[])Bukkit.getOnlinePlayers().toArray((Object[])players);
        ArrayList<Player> r = new ArrayList<Player>();
        int i;
        int j = players.length;
        //Player[] arrayOfPlayer1;
        for (i = 0; i < j; i++ ) {
            Player pl = players[i];
            if (eligible(p, pl))
                r.add(pl);
        }
        return r;
    }

    public static Player getRandomOther(Player p) {
        ArrayList<Player> all = getAllButMe(p);
        if (all.size() == 0)
            return p;
        return getRandomFromList(all);
    }

    public static Player getRandomFromList(ArrayList<Player> all) {
        return all.get((new Random()).nextInt(all.size()));
    }

    /**
     * This function checks the eligibility of player A interacting with player b. If interactibility is all you need,
     * set a to null
     * @param a - The player who will be interacting with b
     * @param b - The player who will be interacted with by a. This is the one that should be tested
     * @return True if b is available, false otherwise
     */
    public static boolean eligible(Player a, Player b) {
        return (
            (
                a==null || (
                    a.getWorld().equals(b.getWorld())
                    && !a.equals(b)
                )
            )
            && !b.isDead()
        );
    }

    public static ArrayList<Player> getActivePlayers() {
        return getAllButMe(null);
    }
}
