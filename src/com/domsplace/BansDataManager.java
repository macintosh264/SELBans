package com.domsplace;

import static com.domsplace.BansBase.BanMessage;
import static com.domsplace.BansBase.BanMessageTemp;
import static com.domsplace.BansBase.KickMessage;
import static com.domsplace.BansBase.MuteMessage;
import static com.domsplace.BansBase.MuteMessageTemp;
import static com.domsplace.BansBase.WarnMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.configuration.file.YamlConfiguration;

public class BansDataManager extends BansBase {
    public static YamlConfiguration config;
    public static File configFile;
    
    public static boolean checkConfig(SELBans plugin) {
        //Create YML if not exists.//
        if(!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        try {
            configFile = new File(plugin.getDataFolder(), "/config.yml");
            if(!configFile.exists()) {
                configFile.createNewFile();
            }
            
            File webInterface = new File(plugin.getDataFolder(), "SelBans.php");
            if(!webInterface.exists()) {
                InputStream is = plugin.getResource("SelBans.php");
                OutputStream os = new FileOutputStream(webInterface);
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		}
                
                os.close();
                is.close();
            }
            
            //Load YML into memory.
            config = YamlConfiguration.loadConfiguration(configFile);
            YamlConfiguration oldConfiguration = YamlConfiguration.loadConfiguration(configFile);
            
            if(BansUtils.dbCon != null) {
                BansUtils.sqlClose();
            }
            
            if(!config.contains("sql")) {                
                config.set("sql.username", "root");
                config.set("sql.password", "password");
                config.set("sql.host", "localhost");
                config.set("sql.database", "minecraft");
                config.set("sql.port", "3306");
                config.set("sql.tableprefix", "SELBans");
            }
            BansUtils.sqlHost = config.getString("sql.host");
            BansUtils.sqlDB = config.getString("sql.database");
            BansUtils.sqlUser = config.getString("sql.username");
            BansUtils.sqlPass = config.getString("sql.password");
            BansUtils.sqlPort = config.getString("sql.port");
            BansUtils.sqlTable = config.getString("sql.tableprefix");
            
            if(!config.contains("colors")) {
                if(!config.contains("colors.error")) {
                    config.set("colors.error", "&c");
                }
                if(!config.contains("colors.default")) {
                    config.set("colors.default", "&7");
                }
                if(!config.contains("colors.important")) {
                    config.set("colors.important", "&9");
                }
            }
            BansBase.ChatError = BansUtils.FormatString(config.getString("colors.error"));
            BansBase.ChatDefault = BansUtils.FormatString(config.getString("colors.default"));
            BansBase.ChatImportant = BansUtils.FormatString(config.getString("colors.important"));
            
            String p = "messages.";
            if(!config.contains(p + "warnmessage")) {
                config.set(p + "warnmessage", WarnMessage);
            }
            if(!config.contains(p + "kickmessage")) {
                config.set(p + "kickmessage", KickMessage);
            }
            if(!config.contains(p + "banmessage")) {
                config.set(p + "banmessage", BanMessage);
            }
            if(!config.contains(p + "banmessagetemp")) {
                config.set(p + "banmessagetemp", BanMessageTemp);
            }
            if(!config.contains(p + "mutemessage")) {
                config.set(p + "mutemessage", MuteMessage);
            }
            if(!config.contains(p + "mutemessagetemp")) {
                config.set(p + "mutemessagetemp", MuteMessageTemp);
            }
            if(!config.contains(p + "mutemessagechat")) {
                config.set(p + "mutemessagechat", MuteMessageChat);
            }
            if(!config.contains(p + "mutemessagecommand")) {
                config.set(p + "mutemessagecommand", MuteMessageCommand);
            }
            
            if(!config.contains("maxbantime")) {
                config.set("maxbantime", "NONE");
            }
            
            BansBase.WarnMessage = config.getString(p + "warnmessage");
            BansBase.KickMessage = config.getString(p + "kickmessage");
            BansBase.BanMessage = config.getString(p + "banmessage");
            BansBase.BanMessageTemp = config.getString(p + "banmessagetemp");
            BansBase.MuteMessage = config.getString(p + "mutemessage");
            BansBase.MuteMessageTemp = config.getString(p + "mutemessagetemp");
            BansBase.MuteMessageChat = config.getString(p + "mutemessagechat");
            BansBase.MuteMessageCommand = config.getString(p + "mutemessagecommand");
            
            if(!config.getString("maxbantime").equalsIgnoreCase("NONE")) {
                BansBase.MaxBanTime = config.getLong("maxbantime");
            }
            
            //Add Muted Commands
            if(!config.contains("mutedcommands")) {
                config.set("mutedcommands", BansBase.mutedCommands);
            }
            BansBase.mutedCommands = config.getStringList("mutedcommands");
            
            if(!config.contains("hidedeath")) {
                config.set("hidedeath", true);
            }
            BansBase.hideDeathMessage = config.getBoolean("hidedeath");
            
            if(!config.equals(oldConfiguration)) {
                config.save(configFile);
            }
            
            if(!BansUtils.sqlConnect()) {
                return false;
            }
            
            /*** Create the Bans Table ***/
            String statement = ""
                    + "CREATE TABLE IF NOT EXISTS " + BansUtils.sqlDB + "."+ BansUtils.sqlTable + "Bans ("
                        + "id INT NOT NULL AUTO_INCREMENT, "
                        + "player VARCHAR(45) NOT NULL, "
                        + "reason VARCHAR(200) NOT NULL, "
                        + "type VARCHAR(15) NOT NULL, "
                        + "playerby VARCHAR(45) NOT NULL, "
                        + "pos VARCHAR(150) NOT NULL, "
                        + "date DATETIME NOT NULL, "
                        + "unbandate DATETIME NOT NULL, "
                        + "active VARCHAR(15) NOT NULL, "
                        + "PRIMARY KEY (id) "
                    + ");";
            BansUtils.sqlQuery(statement);
            
            
            /*** Alter Table If needed ***/
            statement = "ALTER IGNORE TABLE " + BansUtils.sqlDB + "." + BansUtils.sqlTable + "Bans ADD `world` VARCHAR(2048) NULL DEFAULT NULL;";
            BansUtils.sqlQuery(statement, true);
            
            return true;
        } catch(Exception ex) {
            BansUtils.msgConsole(ChatError + "Failed to load Config! Error: " + ex.getLocalizedMessage());
            return false;
        }
    }
}
