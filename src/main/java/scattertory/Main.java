package scattertory;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

public final class Main extends JavaPlugin {
    HashMap<String, SubPlugin> plugins;

    static {
        ConfigurationSerialization.registerClass(TimeUnitList.class);
    }

    public Main() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Setting Defaults for the plugin
        if(getConfig().contains("version")) saveDefaultConfig();
        plugins = new HashMap<String, SubPlugin>();
        // reloadPlugins(false);
    }

    void reloadPlugins() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        plugins.clear();
        Iterator<String> i = this.getConfig().getKeys(false).iterator();
        while(i.hasNext()) {
            String k = i.next();
            if(k!="version") {
                String cn = this.getConfig().getString(k + ".class");
                if (cn != null) {
                    Class c = Class.forName(cn);
                    SubPlugin s = (SubPlugin) c.getDeclaredConstructor(String.class, Main.class).newInstance(k, this);
                    plugins.put(k, s);
                }
            }
        }
    }

    public void onEnable() {
        this.reloadConfig();
        try {
            reloadPlugins();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        getLogger().info("Scattertory has been activated");
    }

    public void disableAll() {
        Iterator<String> i = plugins.keySet().iterator();
        while(i.hasNext()) {
            plugins.get(i.next()).disable();
        }
    }

    public void onDisable() {
        disableAll();
        getLogger().info("Scattertory has been deactivated");
    }

    public void sendCommand(String c) {
        getServer().dispatchCommand(getServer().getConsoleSender(),c);
    }
}