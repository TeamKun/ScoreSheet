package net.teamfruit.scoresheet;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.bukkit.configuration.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class GoogleSheets {
    public static String[][] fetchSpreadsheet() {
        try {
            Configuration config = ScoreSheet.INSTANCE.getConfig();

            //GETでスプレッドシートから値を取得
            URL url = new URL(
                    "https://sheets.googleapis.com/v4/spreadsheets/"
                            + config.getString("spreadsheet.id")
                            + "/values/"
                            + config.getString("spreadsheet.sheet")
                            + "!"
                            + config.getString("spreadsheet.range")
                            + "?key="
                            + config.getString("credential.spreadsheet.token")
            );

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.connect();

            Gson gson = new Gson();
            Sheet sheet = gson.fromJson(
                    new JsonReader(new InputStreamReader(
                            http.getInputStream(), StandardCharsets.UTF_8)),
                    Sheet.class);

            return sheet.values;

        } catch (IOException e) {
            Log.log.log(Level.SEVERE, "Could not get spreadsheet data", e);
        }

        return new String[0][0];
    }

    public class Sheet {
        public String range;
        public String majorDimension;
        public String values[][];
    }
}