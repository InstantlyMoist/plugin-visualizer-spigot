package me.kyllian.PluginVisualizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.kyllian.PluginVisualizer.executors.PluginVisualizerExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PluginVisualizerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        reloadConfig();

        new PluginVisualizerExecutor(this);
    }

    public JsonObject generatePluginInformation() {
        JsonObject plugins = new JsonObject();
        JsonObject jsonObject = new JsonObject();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            PluginDescriptionFile descriptionFile = plugin.getDescription();
            JsonObject listObject = new JsonObject();

            JsonArray dependsArray = new JsonArray();
            for (String depend : descriptionFile.getDepend()) {
                // This helps avoid typos in the plugin.yml file
                Plugin dependPlugin = Bukkit.getPluginManager().getPlugin(depend);
                if (dependPlugin == null) continue;
                dependsArray.add(dependPlugin.getName());
            }

            JsonArray softDependsArray = new JsonArray();
            for (String softDepend : descriptionFile.getSoftDepend()) {
                // This helps avoid typos in the plugin.yml file
                Plugin softDependPlugin = Bukkit.getPluginManager().getPlugin(softDepend);
                if (softDependPlugin == null) continue;
                softDependsArray.add(softDependPlugin.getName());
            }

            listObject.add("depend", dependsArray);
            listObject.add("softdepend", softDependsArray);

            jsonObject.add(descriptionFile.getName(), listObject);
        }

        plugins.add("plugins", jsonObject);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File path = new File(this.getDataFolder(), "plugins.json");
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(plugins, writer);
        } catch (IOException e) {
            Bukkit.getLogger().severe("An error occurred while writing the plugins.json file!");
            e.printStackTrace();
        }
        return plugins;
    }
}
