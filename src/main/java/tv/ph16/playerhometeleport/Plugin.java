package tv.ph16.playerhometeleport;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @NotNull
    private String getHomePath(OfflinePlayer player) {
        return Paths.get("homeLocation", player.getUniqueId().toString()).toString();
    }
    private void setHome(OfflinePlayer player, @NotNull Location location) {
        getConfig().set(getHomePath(player), location);
        saveConfig();
    }

    @Nullable
    private Location getHome(@NotNull OfflinePlayer player) {
        return getConfig().getLocation(getHomePath(player));
    }

    private void removeHome(@NotNull OfflinePlayer player) {
        getConfig().set(getHomePath(player), null);
        saveConfig();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 1) {
                ArrayList<String> completes = new ArrayList<>();
                if ("set".startsWith(args[0].toLowerCase())) {
                    completes.add("set");
                } else if ("remove".startsWith(args[0].toLowerCase())) {
                    completes.add("remove");
                } else if ("list".startsWith(args[0].toLowerCase())) {
                    completes.add("list");
                }
                for (OfflinePlayer player : getServer().getWhitelistedPlayers()) {
                    String name = player.getName();
                    if (getHome(player) != null && name.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completes.add(name);
                    }
                }
                return completes;
            } else if (sender.isOp() && args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove"))) {
                ArrayList<String> completes = new ArrayList<>();
                for (OfflinePlayer player : getServer().getWhitelistedPlayers()) {
                    String name = player.getName();
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completes.add(name);
                    }
                }
                return completes;
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }

    @Nullable
    private OfflinePlayer getPlayer(@NotNull String name) {
        for (OfflinePlayer player : getServer().getWhitelistedPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 0 && sender instanceof Player) {
                Player sourceTarget = (Player)sender;
                Location homeLocation = getHome(sourceTarget);
                if (homeLocation != null) {
                    sourceTarget.teleport(homeLocation);
                } else {
                    sender.sendMessage("You do not have a home set.");
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("set") && sender instanceof Player) {
                    Player source = (Player) sender;
                    setHome(source, source.getLocation());
                    sender.sendMessage("Home Set");
                } else if (args[0].equalsIgnoreCase("remove") && sender instanceof Player) {
                    Player source = (Player) sender;
                    removeHome(source);
                    sender.sendMessage("Home Removed");
                } else if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("The following players have a home set:");
                    for (OfflinePlayer player : getServer().getWhitelistedPlayers()) {
                        if (getHome(player) != null) {
                            sender.sendMessage("\t" + player.getName());
                        }
                    }
                } else {
                    OfflinePlayer target = getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage("Cannot find a player with that name");
                    } else {
                        Location homeLocation = getHome(target);
                        if (homeLocation != null) {
                            if (sender instanceof Player) {
                                Player source = (Player)sender;
                                source.teleport(homeLocation);
                            } else {
                                sender.sendMessage("Players home: " + homeLocation.toString());
                            }
                        } else {
                            sender.sendMessage("That player doesn't have a home yet");
                        }
                    }
                }
            } else if (args.length == 2 && sender.isOp() && sender instanceof Player) {
                Player source = (Player)sender;
                if (args[0].equalsIgnoreCase("set")) {
                    OfflinePlayer target = getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("Cannot find a player with that name");
                    } else {
                        setHome(target, source.getLocation());
                        sender.sendMessage("Home Set");
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    OfflinePlayer target = getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("Cannot find a player with that name");
                    } else {
                        removeHome(target);
                        sender.sendMessage("Home Removed");
                    }
                } else {
                    sender.sendMessage("Unknown sub command");
                }
            } else {
                sender.sendMessage("Unknown sub command");
            }
            return true;
        } else {
            return false;
        }
    }
}
