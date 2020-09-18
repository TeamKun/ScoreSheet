package net.teamfruit.scoresheet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class ScoreSheet extends JavaPlugin {
    public static ScoreSheet INSTANCE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        Log.log = getLogger();

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reloadConfig();
        GoogleSheets.fetchSpreadsheetAndApply();
        sender.sendMessage("Spreadsheet data has been fetched.");
        return true;
    }
}
