package scattertory;

import jdk.internal.jline.internal.Nullable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.*;

import static scattertory.UniversalFunctions.*;

public class SwitchPlayers extends SubPlugin {
    static TimeUnitList units;
    NotificationHandler n;
    boolean skipped;

    TimeSpan warning;
    TimeSpan cooldown;

    public SwitchPlayers(String name, Main p) {
        super(name,p,false);
        units = new TimeUnitList(settings.getConfigurationSection("units"));
        warning = new TimeSpan(settings.getConfigurationSection("warning"));
        cooldown = new TimeSpan(settings.getConfigurationSection("cooldown"));
        n = new NotificationHandler(settings.getConfigurationSection("notification"));
        skipped = false;
        enable();
    }

    public void Countdown(@Nullable Integer countdown) {
        if(countdown == null) countdown = (int) Math.floor(warning.displayAmount);
        final int cd = countdown;
        ArrayList<Player> players = getActivePlayers();
        // If there are not enough players
        if(players.size() < 2 || skipped) {
            if(cd < Math.floor(warning.displayAmount)) {
                Iterator<Player> i = (Iterator<Player>) Bukkit.getOnlinePlayers().iterator();
                if (Bukkit.getOnlinePlayers().size() > 1) {
                    while (i.hasNext()) {
                        if(skipped) {
                            String status;
                            if(cooldown.u.name.equals(warning.u.name)) {
                                double e = cooldown.displayAmount + countdown;
                                status = new TimeSpan(cooldown.u,cooldown.displayAmount+cd).status;
                            } else {
                                status = cooldown.status+" and "+countdown+" "+warning.u.getAppropriateTense(cd);
                            }
                            n.sendTitle(i.next(), "Switch Skipped", "Next switch in "+status);
                        } else {
                            n.sendTitle(i.next(), "Switch Cancelled", "Not enough people were ready");
                        }
                    }
                }
            }
            skipped = false;
            addLaterRunnable(new BukkitRunnable() {
                public void run() {
                    Countdown(null);
                }
            }, cooldown.trueAmount - warning.u.toTicks(countdown), false);
        } else {
            if(cd > 0) {
                for (Player player : players) {
                    n.sendTitle(player,cd + "", warning.u.getAppropriateTense(cd) + " until you switch!");
                }
                addLaterRunnable(new BukkitRunnable() {
                    public void run() {
                        Countdown(cd-1);
                    }
                }, warning.u.toTicks(1), false);
            } else {
                switchAllPlayers();
            }
        }
    }

    public void switchAllPlayers() {
        //Bukkit.broadcastMessage("Haha, april fools!");
        ArrayList<Player> players = getActivePlayers();
        Player a, b;
        while(players.size() > 0) {
            if(players.size() < 4) {
                switchPlayers((Player[]) players.toArray());
                players.clear();
            } else {
                a = getRandomFromList(players);
                players.remove(a);
                b = getRandomFromList(players);
                players.remove(b);
                switchPlayers(new Player[]{a,b});
            }
        }
        QueueNextCountdown();
    }

    public void switchPlayers(Player[] players) {
        int num = players.length;
        Location[] locations = new Location[num];
        ItemStack[][] items = new ItemStack[num][];
        for(int i = 0; i<num; i++) {
            locations[i] = players[i].getLocation();
            items[i] = players[i].getInventory().getContents();
        }
        for(int i = 0; i<num; i++) {
            Player a = players[i];
            int n = (i+1) % num;
            a.teleport(locations[n]);
            a.getInventory().setContents(items[n]);
            switchPlayerTitles(a,players[n]);
        }
    }

    void switchPlayerTitles(Player a, Player b) {
        a.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("You switched with "+b.getDisplayName()+"!"));
        n.sendTitle(a,"Switch!","Next switch is in "+cooldown.status);
    }

    /**
     * Runs after every switdchAllPlayers
     */
    public void QueueNextCountdown() {
        addLaterRunnable(new BukkitRunnable() {
            public void run() {
                Countdown(null);
            }
        },cooldown.trueAmount-warning.trueAmount, false);
    }

    @Override
    public void enable() {
        super.enable();
        QueueNextCountdown();
    }
}



class TimeUnit implements ConfigurationSerializable {
    public int tickAmount;
    public boolean isDefault;
    public String name;
    public String plural;

    TimeUnit(Map<String,Object> o) {
        if(o.containsKey("default")) {
            isDefault = (Boolean) o.get("default");
        } else {
            isDefault = false;
        }
        if(o.containsKey("ticks")) {
            tickAmount = (Integer) o.get("ticks");
        } else if(o.containsKey("seconds")) {
            tickAmount = (Integer) o.get("seconds") * 20;
        } else {
            tickAmount = 20;
        }
        name = (String) o.get("name");
        if(o.containsKey("plural")) {
            plural = (String) o.get("plural");
        } else {
            plural = name+"s";
        }
    }

    TimeUnit(ConfigurationSection c) {
        this(c.getValues(false));
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> r = new HashMap<String, Object>();
        r.put("name",name);
        r.put("default",isDefault);
        r.put("ticks",tickAmount);
        return r;
    }

    @Override
    public String toString() {
        return ("name: "+name+", "+plural+", ticks: "+tickAmount+", default: "+isDefault);
    }

    public String getAppropriateTense(double i) {
        return (i == 1) ? name : plural;
    }

    public String getAppropriateTense(int i) {
        return (i == 1) ? name : plural;
    }

    public int toTicks(double i) {
        return (int) Math.floor(tickAmount * i);
    }
}

class TimeUnitList implements ConfigurationSerializable {
    HashMap<String, TimeUnit> units;
    String defaultKey = null;

    TimeUnitList(ConfigurationSection c) {
        units = new HashMap<String, TimeUnit>();
        for (String k : c.getKeys(false)) {
            TimeUnit u = new TimeUnit(c.getConfigurationSection(k));
            if (u.isDefault) defaultKey = k;
            units.put(k, u);
        }
    }

    public Map<String, Object> serialize() {
        Map<String,Object> r = new HashMap<String,Object>();
        for (String k : units.keySet()) {
            r.put(k, units.get(k).serialize());
        }
        return r;
    }

    @Override
    public String toString() {
        Iterator<String> i = units.keySet().iterator();
        String r = "";
        while(i.hasNext()) {
            String k = i.next();
            r += (k+" : { "+units.get(k) + " }");
            if(i.hasNext()) r+="\n";
        }
        return r;
    }

    public TimeUnit get(@Nullable String u) {
        if(units.isEmpty()) return null;
        if(u==null || !units.containsKey(u)) return (defaultKey==null) ? null : units.get(defaultKey);
        return units.get(u);
    }
}

class TimeSpan {
    public TimeUnit u;
    public String status;
    public double displayAmount;
    public int trueAmount;

    TimeSpan(ConfigurationSection c) {
        String un = c.getString("unit");
        u = SwitchPlayers.units.get(un);
        if(u==null) throw new NullPointerException();
        displayAmount = c.getDouble("value");
        status = String.format("%s %s",niceNumberFormat(),u.getAppropriateTense(displayAmount));
        trueAmount = u.toTicks(displayAmount);
    }

    TimeSpan(TimeUnit unit, double displayAmount) {
        this.displayAmount = displayAmount;
        this.u = unit;
        status = String.format("%s %s",niceNumberFormat(),u.getAppropriateTense(displayAmount));
        trueAmount = u.toTicks(displayAmount);
    }

    String niceNumberFormat() {
        String r = "";
        if(displayAmount >= 1000) r = "more than ";
        double n = Math.min(displayAmount,999);
        if(n % 1.0 == 0) return r + (long) n;
        return String.format("%.1f",n);
    }

    @Override
    public String toString() {
        return status+", or "+trueAmount+" ticks";
    }
}

class NotificationHandler {
    int in;
    int hold;
    int out;

    NotificationHandler(ConfigurationSection c) {
        in = c.getInt("in");
        out = c.getInt("out");
        hold = c.getInt("hold");
    }

    NotificationHandler(int i, int h, int o) {
        in = i;
        hold = h;
        out = o;
    }

    void sendTitle(Player p, @Nullable String title, @Nullable String subtitle, NotificationHandler n) {
        p.sendTitle(title,subtitle,n.in,n.hold,n.out);
    }

    void sendTitle(Player p, @Nullable String title, @Nullable String subtitle) {
        sendTitle(p,title,subtitle,this);
    }
}