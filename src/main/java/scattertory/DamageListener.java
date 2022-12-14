package scattertory;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import static scattertory.UniversalFunctions.*;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class DamageListener extends SubPlugin implements Listener {
    HashMap<UUID,HashMap<Class,Boolean>> toBeDamaged;

    public DamageListener(String s, Main p) {
        super(s, p);
        toBeDamaged = new HashMap<UUID,HashMap<Class,Boolean>>();
        Iterator<Player> i = (Iterator<Player>) Bukkit.getOnlinePlayers().iterator();
        while(i.hasNext()) {
            createPlayerEntry(i.next());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if(e.getEntityType() == EntityType.PLAYER) {
            Player a = (Player) e.getEntity();
            if(toBeDamaged.get(a.getUniqueId()).get(e.getClass())) {
                toBeDamaged.get(a.getUniqueId()).put(e.getClass(),false);
            } else {
                Player p = getRandomFromList(getActivePlayers());
                toBeDamaged.get(p.getUniqueId()).put(e.getClass(),true);
                e.setCancelled(true);
                EntityDamageEvent newEvent;
                if(e.getClass().equals(EntityDamageByBlockEvent.class)) {
                    EntityDamageByBlockEvent f = (EntityDamageByBlockEvent) e;
                    newEvent = new EntityDamageByBlockEvent(f.getDamager(), p, f.getCause(), f.getDamage());
                    p.damage(e.getDamage());
                } else if(e.getClass().equals(EntityDamageByEntityEvent.class)) {
                    EntityDamageByEntityEvent g = (EntityDamageByEntityEvent) e;
                    newEvent = new EntityDamageByEntityEvent(g.getDamager(), p, g.getCause(), g.getDamage());
                    p.damage(e.getDamage(), g.getDamager());
                } else {
                    newEvent = new EntityDamageEvent(p,e.getCause(),e.getDamage());
                    p.damage(e.getDamage());
                }
                p.setLastDamageCause(newEvent);
            }
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        createPlayerEntry(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        toBeDamaged.remove(e.getPlayer().getUniqueId());
    }

    void createPlayerEntry(Player p) {
        HashMap<Class, Boolean> m = new HashMap<Class, Boolean>();
        m.put(EntityDamageByEntityEvent.class,false);
        m.put(EntityDamageByBlockEvent.class,false);
        m.put(EntityDamageEvent.class,false);
        toBeDamaged.put(p.getUniqueId(),m);
    }
}
