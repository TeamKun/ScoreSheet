package net.teamfruit.scoresheet;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleSheets {
    public static void fetchSpreadsheetAndApply() {
        try {
            Configuration config = ScoreSheet.INSTANCE.getConfig();

            String token = config.getString("credential.spreadsheet.token");
            String id = config.getString("spreadsheet.id");
            String sheet = config.getString("spreadsheet.sheet");
            // String mc_uuid = config.getString("spreadsheet.minecraft.uuid");
            String mc_name = config.getString("spreadsheet.data.minecraft.name");
            // int row_label = config.getInt("spreadsheet.row.label");
            int row_data = config.getInt("spreadsheet.row.data");
            List<DataType> types = config.getList("spreadsheet.data.types", Collections.emptyList())
                    .stream()
                    .map(Map.class::cast)
                    .map(DataType::new)
                    .collect(Collectors.toList());

            //GETでスプレッドシートから値を取得
            URL url = new URL(
                    MessageFormat.format(
                            "https://sheets.googleapis.com/v4/spreadsheets/{0}/values:batchGet?{1}",
                            id,
                            Stream.concat(
                                    Stream.concat(
                                            types.stream(),
                                            Stream.of(
                                                    // new DataType(mc_uuid, null),
                                                    new DataType(mc_name, null)
                                            )
                                    ).map(e -> MessageFormat.format(
                                            "ranges={0}!{1}:{1}",
                                            sheet,
                                            e.column
                                    )),
                                    Stream.of(
                                            "majorDimension=COLUMNS",
                                            "valueRenderOption=UNFORMATTED_VALUE",
                                            "key=" + token
                                    )
                            ).collect(Collectors.joining("&"))
                    )
            );

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");

            Sheets data;
            try (
                    Closeable c = http::disconnect;
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(
                            http.getInputStream(), StandardCharsets.UTF_8));
            ) {
                http.connect();

                data = new Gson().fromJson(jsonReader, Sheets.class);
            }

            int types_length = types.size();
            String[] mc_name_data = data.valueRanges.get(types_length).values[0];
            for (int ix = 0; ix < types_length; ix++) {
                Objective objective = types.get(ix).scoreboard.initAndGetObjective();
                String[] valueRange = data.valueRanges.get(ix).values[0];
                for (int iy = 0; iy < mc_name_data.length; iy++) {
                    int score = iy < valueRange.length ? NumberUtils.toInt(valueRange[iy]) : 0;
                    if (mc_name_data[iy].length() <= 40)
                        objective.getScore(mc_name_data[iy]).setScore(score);
                }
            }
        } catch (IOException e) {
            Log.log.log(Level.SEVERE, "Could not get spreadsheet data", e);
        }
    }

    public static class Sheets {
        public List<Sheet> valueRanges;

        public static class Sheet {
            public String range;
            public String majorDimension;
            public String values[][];
        }
    }

    public static class DataType {
        public String column;
        public DataTypeScoreboard scoreboard;

        @SuppressWarnings("unchecked")
        public DataType(Map<?, ?> data) {
            column = (String) data.get("column");
            scoreboard = new DataTypeScoreboard((Map<String, Object>) data.get("scoreboard"));
        }

        public DataType(String column, DataTypeScoreboard scoreboard) {
            this.column = column;
            this.scoreboard = scoreboard;
        }

        public static class DataTypeScoreboard {
            public String name;
            public String title;
            public Objective objective;

            public DataTypeScoreboard(Map<String, Object> data) {
                name = (String) data.get("name");
                title = (String) data.get("title");
            }

            public Objective initAndGetObjective() {
                if (objective == null) {
                    Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
                    objective = sb.getObjective(name);
                    if (objective == null)
                        objective = sb.registerNewObjective(name, "dummy", title);
                }
                return objective;
            }
        }
    }
}