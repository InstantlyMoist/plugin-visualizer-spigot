package me.kyllian.PluginVisualizer.executors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kyllian.PluginVisualizer.PluginVisualizerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static me.kyllian.PluginVisualizer.utils.StringUtils.color;

public class PluginVisualizerExecutor implements CommandExecutor {

    private final PluginVisualizerPlugin plugin;
    private final String apiUrl = "https://pluginvisualizer.com";

    public PluginVisualizerExecutor(PluginVisualizerPlugin plugin) {
        this.plugin = plugin;

        plugin.getCommand("pluginvisualizer").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if (!(commandSender.hasPermission("pluginvisualizer.generate"))) {
            commandSender.sendMessage(color("&cYou don't have permission to execute this command!"));
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(color("&cUsage /pluginvisualizer generate"));
            return true;
        }
        if (args[0].equalsIgnoreCase("generate")) {
            plugin.generatePluginInformation();
            JsonObject export = plugin.generatePluginInformation();
            upload(commandSender, export);
            return true;
        }
        return true;
    }

    private void upload(CommandSender sender, JsonObject export) {
        if (Bukkit.isPrimaryThread()) {
            // Ensure async
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> upload(sender, export));
            return;
        }

        String apiUrl = plugin.getConfig().getString("API_URL");
        if (apiUrl == null || apiUrl.isEmpty()) {
            // Default to the public API
            apiUrl = this.apiUrl;
        }

        // Send post request to the API
        try {
            URL url = new URL(apiUrl + "/api/v1/plugins/upload");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = export.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    String pageId = response.toString();
                    String visualizeUrl = apiUrl + "/visualize/" + pageId;
                    sender.sendMessage(color("&aData successfully uploaded, you can visualize it at: " + visualizeUrl));
                }
            } else {
                sender.sendMessage(color("&cFailed to upload data to the API. Response code: " + responseCode));
            }
        } catch (Exception exception) {
            sender.sendMessage(color("&cAn error occurred while uploading the data to the API, check the console for more information."));
            exception.printStackTrace();
        }
    }
}
