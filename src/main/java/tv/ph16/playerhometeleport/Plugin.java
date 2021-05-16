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
    private static final String CANNOT_FIND_A_PLAYER_WITH_THAT_NAME = "Cannot find a player with that name";
    private static final String SET_COMMAND = "set";
    private static final String LIST_COMMAND = "list";
    private static final String REMOVE_COMMAND = "remove";
    private static final String HOME_COMMAND = "home";

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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase(HOME_COMMAND)) {
            if (args.length == 1) {
                return onFirstComplete(args);
            } else if (sender.isOp() && args.length == 2 && (args[0].equalsIgnoreCase(SET_COMMAND) || args[0].equalsIgnoreCase(REMOVE_COMMAND))) {
                return onSecondComplete(args);
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }

    @NotNull
    private ArrayList<String> onSecondComplete(@NotNull String @NotNull [] args) {
        ArrayList<String> completes = new ArrayList<>();
        for (OfflinePlayer player : getServer().getOfflinePlayers()) {
            String playerName = player.getName();
            if (playerName != null && playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
                completes.add(playerName);
            }
        }
        return completes;
    }

    @NotNull
    private ArrayList<String> onFirstComplete(@NotNull String @NotNull [] args) {
        ArrayList<String> completes = new ArrayList<>();
        if (SET_COMMAND.startsWith(args[0].toLowerCase())) {
            completes.add(SET_COMMAND);
        } else if (REMOVE_COMMAND.startsWith(args[0].toLowerCase())) {
            completes.add(REMOVE_COMMAND);
        } else if (LIST_COMMAND.startsWith(args[0].toLowerCase())) {
            completes.add(LIST_COMMAND);
        }
        for (OfflinePlayer player : getServer().getOfflinePlayers()) {
            String playerName = player.getName();
            if (playerName != null && getHome(player) != null && playerName.toLowerCase().startsWith(args[0].toLowerCase())) {
                completes.add(playerName);
            }
        }
        return completes;
    }

    @Nullable
    private OfflinePlayer getPlayer(@NotNull String name) {
        for (OfflinePlayer player : getServer().getOfflinePlayers()) {
            String playerName = player.getName();
            if (playerName != null && playerName.equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("home")) {
            if (args.length == 0 && sender instanceof Player) {
                onOwnHomeCommand(sender);
            } else if (args.length == 1) {
                onCommandOne(sender, args);
            } else if (args.length == 2 && sender.isOp() && sender instanceof Player) {
                onOpCommand(sender, args);
            } else {
                sender.sendMessage("Unknown sub command");
            }
            return true;
        }
        return false;
    }

    private void onCommandOne(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args[0].equalsIgnoreCase(SET_COMMAND) && sender instanceof Player) {
            Player source = (Player) sender;
            setHome(source, source.getLocation());
            sender.sendMessage("Home Set");
        } else if (args[0].equalsIgnoreCase(REMOVE_COMMAND) && sender instanceof Player) {
            Player source = (Player) sender;
            removeHome(source);
            sender.sendMessage("Home Removed");
        } else if (args[0].equalsIgnoreCase(LIST_COMMAND)) {
            onListCommand(sender);
        } else {
            onOtherHomeCommand(sender, args[0]);
        }
    }

    private void onOpCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        Player source = (Player) sender;
        if (args[0].equalsIgnoreCase(SET_COMMAND)) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(CANNOT_FIND_A_PLAYER_WITH_THAT_NAME);
            } else {
                setHome(target, source.getLocation());
                sender.sendMessage("Home Set");
            }
        } else if (args[0].equalsIgnoreCase(REMOVE_COMMAND)) {
            OfflinePlayer target = getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(CANNOT_FIND_A_PLAYER_WITH_THAT_NAME);
            } else {
                removeHome(target);
                sender.sendMessage("Home Removed");
            }
        } else {
            sender.sendMessage("Unknown sub command");
        }
    }

    private void onOtherHomeCommand(@NotNull CommandSender sender, @NotNull String arg) {
        OfflinePlayer target = getPlayer(arg);
        if (target == null) {
            sender.sendMessage(CANNOT_FIND_A_PLAYER_WITH_THAT_NAME);
        } else {
            Location homeLocation = getHome(target);
            if (homeLocation != null) {
                if (sender instanceof Player) {
                    Player source = (Player)sender;
                    source.teleport(homeLocation);
                } else {
                    sender.sendMessage("Players home: " + homeLocation);
                }
            } else {
                sender.sendMessage("That player doesn't have a home yet");
            }
        }
    }

    private void onListCommand(@NotNull CommandSender sender) {
        sender.sendMessage("The following players have a home set:");
        for (OfflinePlayer player : getServer().getOfflinePlayers()) {
            if (getHome(player) != null) {
                sender.sendMessage("    " + player.getName());
            }
        }
    }

    private void onOwnHomeCommand(@NotNull CommandSender sender) {
        Player sourceTarget = (Player) sender;
        Location homeLocation = getHome(sourceTarget);
        if (homeLocation != null) {
            sourceTarget.teleport(homeLocation);
        } else {
            sender.sendMessage("You do not have a home set.");
        }
    }
}
