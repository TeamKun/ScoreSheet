package net.teamfruit.scoresheet;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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

    public void updateScoreboard(String[][] data) {
        Log.log.log(Level.INFO, ArrayUtils.toString(data));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            return false;
        if ("set".equalsIgnoreCase(args[0])) {
            if (args.length == 1)
                return false;
            getConfig().set("spreadsheet.id", args[1]);
            sender.sendMessage("Spreadsheet ID has been set.");
            return true;
        } else if ("fetch".equalsIgnoreCase(args[0])) {
            String[][] data = GoogleSheets.fetchSpreadsheet();
            updateScoreboard(data);
            sender.sendMessage("Spreadsheet data has been fetched.");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0)
            return Arrays.asList("set", "fetch");
        if (args.length == 1 && "set".equalsIgnoreCase(args[0]))
            return Arrays.asList("<sheetid>");
        return Arrays.asList();
    }
}
