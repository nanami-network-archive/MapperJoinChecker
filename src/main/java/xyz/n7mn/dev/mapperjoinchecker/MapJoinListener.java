package xyz.n7mn.dev.mapperjoinchecker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.sql.*;

class MapJoinListener implements Listener {

    private final Plugin plugin;
    private Connection con;

    public MapJoinListener(Plugin plugin, Connection con){
        this.plugin = plugin;
        this.con = con;
    }

    @EventHandler
    public void AsyncPlayerPreLoginEvent (AsyncPlayerPreLoginEvent e) {
        try {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList");
            statement.execute();
            statement.close();
        } catch (SQLException ex1) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                con.setAutoCommit(true);
            } catch (SQLException ex2) {
                ex2.printStackTrace();
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }

        try {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM MinecraftUserList a, RoleRankList b WHERE a.RoleUUID = b.UUID AND MinecraftUUID = ?");
            statement.setString(1, e.getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                // System.out.println(set.getInt("Rank"));
                if (set.getInt("Rank") >= 3){
                    e.allow();
                    return;
                }
            }

            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "\n--- Nanami Network ---\n権限がないかDiscord連携がされていません。\nDiscord : https://discord.gg/C6zeZZn5ys");
        } catch (SQLException ex) {
            ex.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e){
        e.getPlayer().setOp(true);
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e){
        e.getPlayer().setOp(false);
    }
}
