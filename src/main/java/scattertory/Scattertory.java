package scattertory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import static scattertory.UniversalFunctions.*;

public class Scattertory extends SubPlugin implements Listener {
    public Scattertory(String s, Main p) {
        super(s, p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (event.getEntityType() == EntityType.PLAYER)
            if (takeItemFromPlayer(event.getItem(), (Player)event.getEntity()))
                event.setCancelled(true);
    }

    public boolean takeItemFromPlayer(Item initialItem, Player player) {
        Random r = new Random();
        ArrayList<Player> players = getAllButMe(player);
        if (players.size() > 0) {
            initialItem.remove();
            ItemStack remaining = initialItem.getItemStack();
            while (remaining != null && remaining.getAmount() > 0 && players.size() > 0) {
                Player p = getRandomFromList(players);
                players.remove(p);
                HashMap<Integer, ItemStack> a = p.getInventory().addItem(new ItemStack[] { remaining });
                if (a.size() > 0) {
                    Iterator<Integer> i = a.keySet().iterator();
                    while (i.hasNext())
                        remaining = a.get(i.next());
                    continue;
                }
                remaining = null;
            }
            if (remaining != null && remaining.getAmount() > 0) {
                Player p = getRandomOther(player);
                p.getWorld().dropItemNaturally(p.getLocation(), remaining);
            }
            return true;
        }
        return false;
    }
}
