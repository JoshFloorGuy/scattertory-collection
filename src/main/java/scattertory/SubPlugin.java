package scattertory;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class SubPlugin  {
    String pluginName;
    Main main;
    ConfigurationSection settings;
    HashMap<UUID,BukkitTask> tasks;
    boolean enabled;

    public SubPlugin(String name, Main p, boolean enableOnFinish) {
        tasks = new HashMap<UUID,BukkitTask>();
        main = p;
        pluginName = name;
        settings = p.getConfig().getConfigurationSection(pluginName);
        assert settings != null;
        enabled = settings.getBoolean("enabled");
        if(enableOnFinish && enabled) {
            enable();
        }
    }

    public SubPlugin(String s, Main p) {
        this(s,p,true);
    }

    public void enable() {
        settings.set("enabled",true);
        if(this instanceof Listener) {
            getServer().getPluginManager().registerEvents((Listener) this, main);
        }
    }

    public void disable() {
        settings.set("enabled",false);
        for (BukkitTask bukkitTask : tasks.values()) {
            bukkitTask.cancel();
        }
        tasks.clear();
    }

    public void addLaterRunnable(final BukkitRunnable r, int ticks, boolean async) {
        BukkitTask t;
        final UUID index = UUID.randomUUID();

        if(async) {
            BukkitRunnable newRun = new BukkitRunnable() {
                public void run() {
                    r.runTaskAsynchronously(main);
                    tasks.remove(index);
                }
            };
            t = newRun.runTaskLaterAsynchronously(main, ticks);
        } else {
            BukkitRunnable newRun = new BukkitRunnable() {
                public void run() {
                    r.runTask(main);
                    tasks.remove(index);
                }
            };
            t = newRun.runTaskLater(main, ticks);
        }
        tasks.put(index,t);
    }
}
