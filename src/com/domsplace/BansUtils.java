package com.domsplace;

import static com.domsplace.BansBase.BanMessage;
import static com.domsplace.BansBase.BanMessageTemp;
import static com.domsplace.BansBase.MuteMessage;
import static com.domsplace.BansBase.MuteMessageTemp;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BansUtils extends BansBase {
    public static File dataFolder;
    
    public static String sqlHost = "";
    public static String sqlDB = "";
    public static String sqlUser = "";
    public static String sqlPass = "";
    public static String sqlPort = "";
    public static String sqlTable = "";
    public static Connection dbCon;
    
    public static String getStringLocation (Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
    
    public static Location getLocationString(String string, String world) {
        String[] values = string.split(", ");
        try {
            int x = Integer.parseInt(values[0]);
            int y = Integer.parseInt(values[1]);
            int z = Integer.parseInt(values[2]);
            return new Location(Bukkit.getServer().getWorld(world), x,y,z);
        } catch (NumberFormatException NFE) {
        }
        return null;
    }
    
    public static Location getLocationString(String string, World world) {
        String[] values = string.split(", ");
        try {
            int x = Integer.parseInt(values[0]);
            int y = Integer.parseInt(values[1]);
            int z = Integer.parseInt(values[2]);
            return new Location(world, x,y,z);
        } catch (NumberFormatException NFE) {
        }
        return null;
    }
    
    public static void addBan(OfflinePlayer player, String reason, CommandSender banner, Date unbandate, String type) {
        boolean active = false;
        if(unbandate.getTime() > (new Date()).getTime()) {
            active = true;
        }
        
        addBan(player, reason, banner, unbandate, type, active);
    }
    
    public static void addBan(OfflinePlayer player, String reason, CommandSender banner, Date unbandate, String type, boolean active) {        
        String BannerName = "CONSOLE";
        String location = "0, 0, 0";

        if((banner instanceof Player)) {
            BannerName = ((Player) banner).getName();
            location = getStringLocation(((Player) banner).getLocation());
        }
        
        reason = reason.replaceAll("'", "\"");
        
        String statement = ""
                + "INSERT INTO " + sqlDB + "."+ sqlTable + "Bans ("
                    + "`player`, "
                    + "`reason`, "
                    + "`type`, "
                    + "`playerby`, "
                    + "`pos`, "
                    + "`date`, "
                    + "`unbandate`, "
                    + "`active`"
                + ") VALUES ("
                    + "'" + player.getName() + "', "
                    + "'" + StringEscapeUtils.escapeJava(reason) + "', "
                    + "'" + type + "', "
                    + "'" + BannerName + "', "
                    + "'" + location + "', "
                    + "'" + dateToSQL(new Date()) + "', "
                    + "'" + dateToSQL(unbandate) + "', "
                    + "'" + active + "'"
                + ");";
        sqlQuery(statement);
    }
    
    public static File getDataFolder() {
        return dataFolder;
    }
    
    public static void deleteBan(int id) {
        String statement = ""
                + "UPDATE " + sqlDB + "."+ sqlTable + "Bans "
                + "SET `active` = 'false' "
                + "WHERE `id`=" + id + ";";
        sqlQuery(statement);
    }
    
    public static void BanPlayer(OfflinePlayer player, String reason, CommandSender banner, Date unbandate, boolean useTime) {
        if(!useTime) {
            addBan(player, reason, banner, unbandate, "ban", true);
        } else {
            addBan(player, reason, banner, unbandate, "ban");
        }
        String PlayerName = "CONSOLE";

        if(!isConsole(banner)) {
            PlayerName = ((Player) banner).getName();
        }

        String NotifyMessage = ChatImportant + PlayerName + ChatDefault + " banned " + ChatImportant + player.getName() + ChatDefault + " for " + ChatImportant + reason;

        if(useTime) {
            NotifyMessage += ChatDefault + " for " + ChatImportant + TimeAway(unbandate);
        }

        NotifyMessage += ChatDefault + ".";

        broadcastWithPerm("SQLBans.ban.notify", NotifyMessage);
        
        player.setBanned(true);
        
        /* Send Localized Message */
        if(player.isOnline()) {
            String playerKickMessage = "";
            if(useTime) {
                playerKickMessage = KickMessageFormat(BanMessageTemp, reason, banner).replaceAll("%time%", TimeAway(unbandate));
            } else {
                playerKickMessage = KickMessageFormat(BanMessage, reason, banner);
            }
            
            if(useTime) {
                reason = TimeAway(unbandate) + ChatDefault + " for " + ChatImportant + reason;
            }
            
            ((Player) player).kickPlayer(playerKickMessage);
        }
    }

    public static void KickPlayer(Player target, String reason, CommandSender banner) {
        addBan(target, reason, banner, new Date(), "kick");
        
        String PlayerName = "CONSOLE";
        
        if(!isConsole(banner)) {
            PlayerName = ((Player) banner).getName();
        }
        
        String NotifyMessage = ChatImportant + PlayerName + ChatDefault + " kicked " + ChatImportant + target.getName() + ChatDefault + " for " + ChatImportant + reason + ChatDefault + ".";
        
        broadcastWithPerm("SQLBans.kick.notify", NotifyMessage);
        
        target.kickPlayer(KickMessageFormat(KickMessage, reason, banner));
    }
    
    public static void WarnPlayer(Player target, String reason, CommandSender banner) {
        addBan(target, reason, banner, new Date(), "warn");
        
        String PlayerName = "CONSOLE";
        
        if(!isConsole(banner)) {
            PlayerName = ((Player) banner).getName();
        }
        
        String NotifyMessage = ChatImportant + PlayerName + ChatDefault + " warned " + ChatImportant + target.getName() + ChatDefault + " for " + ChatImportant + reason + ChatDefault + ".";
        
        broadcastWithPerm("SQLBans.warn.notify", NotifyMessage);
        
        target.sendMessage(KickMessageFormat(WarnMessage, reason, banner));
    }
    
    public static void MutePlayer(OfflinePlayer player, String reason, CommandSender banner, Date unbandate, boolean useTime) {
        if(!useTime) {
            addBan(player, reason, banner, unbandate, "mute", true);
        } else {
            addBan(player, reason, banner, unbandate, "mute");
        }
        String PlayerName = "CONSOLE";

        if(!isConsole(banner)) {
            PlayerName = ((Player) banner).getName();
        }
        
        
        String NotifyMessage = ChatImportant + PlayerName + ChatDefault + " muted " + ChatImportant + player.getName() + ChatDefault + " for " + ChatImportant + reason;

        if(useTime) {
            NotifyMessage += ChatDefault + " for " + ChatImportant + TimeAway(unbandate);
        }

        NotifyMessage += ChatDefault + ".";

        broadcastWithPerm("SQLBans.mute.notify", NotifyMessage);
        
        /* Send Localized Message */
        if(player.isOnline()) {
            String playerMuteMessage = "";
            if(useTime) {
                playerMuteMessage = KickMessageFormat(MuteMessageTemp, reason, banner).replaceAll("%time%", TimeAway(unbandate));
            } else {
                playerMuteMessage = KickMessageFormat(MuteMessage, reason, banner);
            }
            
            if(useTime) {
                reason = TimeAway(unbandate) + ChatDefault + " for " + ChatImportant + reason;
            }
            ((Player) player).sendMessage(playerMuteMessage);
        }
    }
    
    public static List<Integer> getActiveBans(OfflinePlayer target, String type) {
        List<Integer> ids = new ArrayList<Integer>();
        String statement = ""
                + "SELECT id FROM " + sqlDB + "."+ sqlTable + "Bans "
                + "WHERE player='" + target.getName() + "' AND active='true' AND type='" + type + "'";
        List<Map<String, String>> results = sqlFetch(statement);
        if(results != null) {
            for(int i = 0; i < results.size(); i++) {
                ids.add(Integer.parseInt(results.get(i).get("id")));
            }
        }
        return ids;
    }
    
    public static List<Integer> getBans(OfflinePlayer target, String type) {
        List<Integer> ids = new ArrayList<Integer>();
        String statement = ""
                + "SELECT id FROM " + sqlDB + "."+ sqlTable + "Bans "
                + "WHERE player='" + target.getName() + "' AND type='" + type + "' ORDER BY date DESC";
        List<Map<String, String>> results = sqlFetch(statement);
        if(results == null) {
            return ids;
        }
        for(int i = 0; i < results.size(); i++) {
            ids.add(Integer.parseInt(results.get(i).get("id")));
        }
        return ids;
    }
    
    public static Map<String, String> getBanData(int id) {
        String statement = ""
                + "SELECT * FROM " + sqlDB + "."+ sqlTable + "Bans "
                + "WHERE id='" + id + "' LIMIT 0,1";
        List<Map<String, String>> results = sqlFetch(statement);
        if(results == null) {
            return null;
        }
        if(results.size() >= 1) {
            return results.get(0);
        }
        return null;
    }
    
    public static void PardonPlayer(OfflinePlayer target, String type) {
        List<Integer> IDs = getActiveBans(target, type);
        for(int i = 0; i < IDs.size(); i++) {
            deleteBan(IDs.get(i));
        }
    }
    
    public static boolean hasActiveBans(OfflinePlayer player, String type) {
        if(getActiveBans(player, type).size() > 0) {
            return true;
        }
        return false;
    }
    
    public static boolean isPlayerBanned(OfflinePlayer player) {
        if(!player.isBanned()) {
            if(hasActiveBans(player, "bans")) {
                return true;
            }
            return false;
        }
        return true;
    }
    
    public static int getActiveBannedTimes(OfflinePlayer player, String type) {
        return getActiveBans(player, type).size();
    }

    public static String getBanReason(OfflinePlayer target, String type) {
        String statement = ""
                + "SELECT reason FROM " + sqlDB + "."+ sqlTable + "Bans "
                + "WHERE player='" + target.getName() + "' AND active='true' AND type='" + type + "' LIMIT 1;";
        List<Map<String, String>> results = sqlFetch(statement);
        if(results == null) {
            return "Unknown reason";
        }
        return results.get(0).get("reason");
    }

    public static String getBanner(OfflinePlayer target, String type) {
        String statement = ""
                + "SELECT playerby FROM " + sqlDB + "."+ sqlTable + "Bans "
                + "WHERE player='" + target.getName() + "' AND active='true' AND type='" + type + "'  LIMIT 1;";
        List<Map<String, String>> results = sqlFetch(statement);
        if(results == null) {
            return "Uknown Player";
        }
        return results.get(0).get("playerby");
    }

    public static String getPastTenseStringName(String type) {
        type = type.toLowerCase();
        if(type.equalsIgnoreCase("ban")) {
            return "banned";
        }
        if(type.equalsIgnoreCase("warn")) {
            return "warned";
        }
        if(type.equalsIgnoreCase("kick")) {
            return "kicked";
        }
        if(type.equalsIgnoreCase("mute")) {
            return "muted";
        }
        if(type.equalsIgnoreCase("strike")) {
            return "striked";
        }
        return type + "ed";
    }
    
    public static void checkBans() {
        String statement = ""
                + "SELECT * FROM " + sqlDB + "."+ sqlTable + "Bans "
                + "WHERE active='true' ORDER BY date ASC";
        List<Map<String, String>> results = sqlFetch(statement);
        if(results == null) {
            return;
        }
        for(int i = 0; i < results.size(); i++) {
            Map<String, String> data = results.get(i);
            if(data.get("date").equalsIgnoreCase(data.get("unbandate"))) {
                continue;
            }
            
            long now = (new Date()).getTime();
            long unb = getSQLDate(data.get("unbandate")).getTime();
            long diff = unb - now;
            if(diff > 0) {
                continue;
            }
            deleteBan(Integer.parseInt(data.get("id")));
            if(data.get("type").equalsIgnoreCase("ban")) {
                OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(data.get("player"));
                op.setBanned(false);
                if(isPlayerBanned(op)) {
                    op.setBanned(true);
                }
            }
        }
    }
    
    public static String FormatString(String msg) {
        String[] andCodes = {"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f", "&l", "&o", "&n", "&m", "&k", "&r"};
        String[] altCodes = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f", "§l", "§o", "§n", "§m", "§k", "§r"};
        
        for(int x = 0; x < andCodes.length; x++) {
            msg = msg.replaceAll(andCodes[x], altCodes[x]);
        }
        
        return msg;
    }
    
    public static long getNow() {
        Date someTime = new Date();
        long currentMS = someTime.getTime();
        return currentMS;
    }
    
    public static boolean isValidTime(String input) {
        String[] names = new String[]{
            ("year"),
            ("years"),
            ("month"),
            ("months"),
            ("day"),
            ("days"),
            ("hour"),
            ("hours"),
            ("minute"),
            ("minutes"),
            ("second"),
            ("seconds")
        };
        
        Pattern timePattern = Pattern.compile(
        "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
        
        Matcher m = timePattern.matcher(input);
        
        while(m.find()) {
            	if (m.group() == null || m.group().isEmpty()) {
                    continue;
                }
                for (int i = 0; i < m.groupCount(); i++) {
                    if (m.group(i) != null && !m.group(i).isEmpty()) {
                        return true;
                    }
                }
        }
        
        return false;
    }
    
    public static Date nowAndString(String input) {
        boolean found = false;
        
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        Date now = new Date();
        String[] names = new String[]{
            ("year"),
            ("years"),
            ("month"),
            ("months"),
            ("day"),
            ("days"),
            ("hour"),
            ("hours"),
            ("minute"),
            ("minutes"),
            ("second"),
            ("seconds")
        };
        
        Pattern timePattern = Pattern.compile(
        "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
        
        Matcher m = timePattern.matcher(input);
        
        while(m.find()) {
            	if (m.group() == null || m.group().isEmpty()) {
                    continue;
                }
                for (int i = 0; i < m.groupCount(); i++) {
                    if (m.group(i) != null && !m.group(i).isEmpty()) {
                        found = true;
                    }
                    if(found) {
                        if (m.group(1) != null && !m.group(1).isEmpty()) {
                            years = Integer.parseInt(m.group(1));
                        }
                        if (m.group(2) != null && !m.group(2).isEmpty()) {
                            months = Integer.parseInt(m.group(2));
                        }
                        if (m.group(3) != null && !m.group(3).isEmpty()) {
                            weeks = Integer.parseInt(m.group(3));
                        }
                        if (m.group(4) != null && !m.group(4).isEmpty()) {
                            days = Integer.parseInt(m.group(4));
                        }
                        if (m.group(5) != null && !m.group(5).isEmpty()) {
                            hours = Integer.parseInt(m.group(5));
                        }
                        if (m.group(6) != null && !m.group(6).isEmpty()) {
                            minutes = Integer.parseInt(m.group(6));
                        }
                        if (m.group(7) != null && !m.group(7).isEmpty()) {
                            seconds = Integer.parseInt(m.group(7));
                        }
                        break;
                    }
                }
        }
        
        Calendar c = Calendar.getInstance();
        if (years > 0) {
            c.add(Calendar.YEAR, years);
        }
        if (months > 0)  {
            c.add(Calendar.MONTH, months);
        }
        if (weeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, weeks);
        }
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days);
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours);
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes);
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds);
        }
        now = c.getTime();
        return now;
    }
    
    public static boolean sqlConnect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://"+sqlHost+":"+sqlPort+"/" + sqlDB;
            msgConsole("Opening SQL connection to " + url);
            dbCon = DriverManager.getConnection(url,sqlUser,sqlPass);
            return true;
        } catch (Exception ex) {
            msgConsole(ChatError + "Failed to Connect to SQL. Error: " + ex.getLocalizedMessage());
            return false;
        }
    }
    
    public static boolean sqlQuery(String query) {
        try {
            PreparedStatement sqlStmt = dbCon.prepareStatement(query);
            boolean result = sqlStmt.execute(query);
            return result;
        } catch (SQLException ex) {
            msgConsole(ChatError + "Failed to execute SQL query. Error: " + ex.getLocalizedMessage());
        }
        return false;
    }
    
    public static int sqlQueryID(String query) {
        try {
            PreparedStatement sqlStmt = dbCon.prepareStatement(query);
            int result = sqlStmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return result;
        } catch (SQLException ex) {
            msgConsole(ChatError + "Failed to execute SQL (Return ID) query. Error: " + ex.getLocalizedMessage());
        }
        return -1;
    }
    
    public static List<Map<String, String>> sqlFetch(String query) {
        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        try {
            Statement myStmt = dbCon.createStatement();
            ResultSet result = myStmt.executeQuery(query);
            while (result.next()){
                Map<String, String> data = new HashMap<String, String>();
                for(int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                    data.put(result.getMetaData().getColumnName(i), result.getString(result.getMetaData().getColumnName(i)));
                }
                results.add(data);
            }
        }
        catch (Exception sqlEx) {
            msgConsole(ChatError + "Failed to result SQL query. Error: " + sqlEx.getLocalizedMessage());
        }
        
        if(results.size() < 1) {
            return null;
        }
        
        return results;
    }
    
    public static void sqlClose() {
        try {
            dbCon.close();
            msgConsole("Closing SQL connection...");
        } catch (Exception ex) {
            msgConsole(ChatError + "Failed to Close SQL connection. Error: " + ex.getLocalizedMessage());
        }
    }

    public static void broadcastWithPerm(String permission, String message) {
        Player[] OnlinePlayers = Bukkit.getOnlinePlayers();
        for(int i = 0; i < OnlinePlayers.length; i++) {
            Player p = OnlinePlayers[i];
            if(p.hasPermission(permission)) {
                p.sendMessage(message);
            }
        }
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public static boolean isConsole(CommandSender sender) {
        if(sender instanceof Player) {
            return false;
        }
        return true;
    }

    public static String TimeAway(Date unbanDate) {
        Long NowInMilli = (new Date()).getTime();
        Long TargetInMilli = unbanDate.getTime();
        Long diffInSeconds = (TargetInMilli - NowInMilli) / 1000+1;

        long diff[] = new long[] {0,0,0,0,0};
        /* sec */diff[4] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        /* min */diff[3] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        /* hours */diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        /* days */diff[1] = (diffInSeconds = (diffInSeconds / 24)) >= 31 ? diffInSeconds % 31: diffInSeconds;
        /* months */diff[0] = (diffInSeconds = (diffInSeconds / 31));
        
        String message = "";
        
        if(diff[0] > 0) {
            message += diff[0] + " month";
            if(diff[0] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[1] > 0) {
            message += diff[1] + " day";
            if(diff[1] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[2] > 0) {
            message += diff[2] + " hour";
            if(diff[2] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[3] > 0) {
            message += diff[3] + " minute";
            if(diff[3] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[4] > 0) {
            message += diff[4] + " second";
            if(diff[4] > 1) {
                message += "s";
            }
            return message;
        }
        
        return "Invalid Time Diff!";
    }
    
    public static String TimeAgo(Date date) {
        Long NowInMilli = (new Date()).getTime();
        Long TargetInMilli = date.getTime();
        Long diffInSeconds = (NowInMilli - TargetInMilli) / 1000;

        long diff[] = new long[] {0,0,0,0,0};
        /* sec */diff[4] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        /* min */diff[3] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        /* hours */diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        /* days */diff[1] = (diffInSeconds = (diffInSeconds / 24)) >= 31 ? diffInSeconds % 31: diffInSeconds;
        /* months */diff[0] = (diffInSeconds = (diffInSeconds / 31));
        
        String message = "";
        
        if(diff[0] > 0) {
            message += diff[0] + " month";
            if(diff[0] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[1] > 0) {
            message += diff[1] + " day";
            if(diff[1] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[2] > 0) {
            message += diff[2] + " hour";
            if(diff[2] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[3] > 0) {
            message += diff[3] + " minute";
            if(diff[3] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[4] > 0) {
            message += diff[4] + " second";
            if(diff[4] > 1) {
                message += "s";
            }
            return message;
        }
        
        return "Invalid Time Diff!";
    }
    
    public static String TimeDiff(Date early, Date late) {
        Long NowInMilli = late.getTime();
        Long TargetInMilli = early.getTime();
        Long diffInSeconds = (NowInMilli - TargetInMilli) / 1000;

        long diff[] = new long[] {0,0,0,0,0};
        /* sec */diff[4] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        /* min */diff[3] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        /* hours */diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        /* days */diff[1] = (diffInSeconds = (diffInSeconds / 24)) >= 31 ? diffInSeconds % 31: diffInSeconds;
        /* months */diff[0] = (diffInSeconds = (diffInSeconds / 31));
        
        String message = "";
        
        if(diff[0] > 0) {
            message += diff[0] + " month";
            if(diff[0] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[1] > 0) {
            message += diff[1] + " day";
            if(diff[1] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[2] > 0) {
            message += diff[2] + " hour";
            if(diff[2] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[3] > 0) {
            message += diff[3] + " minute";
            if(diff[3] > 1) {
                message += "s";
            }
            return message;
        }
        if(diff[4] > 0) {
            message += diff[4] + " second";
            if(diff[4] > 1) {
                message += "s";
            }
            return message;
        }
        
        return "Invalid Time Diff!";
    }
    
    public static String dateToSQL(Date date) {
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        String now = ft.format(date);
        return now;
    }
    
    public static Date getSQLDate(String sqlDate) {
        SimpleDateFormat fat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date returnDate = new Date();
        try {
            returnDate = fat.parse(sqlDate);
        } catch (ParseException ex) {
            return new Date();
        }
        return returnDate;
    }
    
    public static String KickMessageFormat(String BanMessage, String Reason, CommandSender banner) {
        return KickMessageFormat(BanMessage, Reason, banner.getName());
    }
    
    public static String KickMessageFormat(String BanMessage, String Reason, String banner) {
        return FormatString(BanMessage.replaceAll("%reason%", Reason).replaceAll("%player%", banner));
    }
    
    public static void msgConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatDefault + message);
    }
    
    public static boolean CanPlayerTalk(OfflinePlayer player) {
        if(BansUtils.hasActiveBans(player, "mute")) {
            return false;
        }
        return true;
    }
}
