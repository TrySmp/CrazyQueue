package net.vertrauterdavid.queue.velocity.command;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;
import net.vertrauterdavid.queue.velocity.queue.ServerQueue;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;
import net.vertrauterdavid.queue.velocity.util.CommandUtil;

import java.util.*;

public class CrazyQueueCommand implements RawCommand {

    private final String name;
    private final ProxyServer proxyServer;
    private final QueueManager queueManager;

    public CrazyQueueCommand(String name) {
        this.name = name;
        this.proxyServer = CrazyQueueVelocity.getInstance().getProxyServer();
        this.queueManager = CrazyQueueVelocity.getInstance().getQueueManager();

        proxyServer.getCommandManager().register(name, this);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;
        String[] args = CommandUtil.getArgs(invocation);

        // remove first if empty
        if (args.length > 0 && args[0].replaceAll(" ", "").equalsIgnoreCase("")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length == 1 || args.length == 2) {
            if (args[0].equalsIgnoreCase("info")) {
                player.sendMessage(ColorUtil.translate("§8▏"));
                player.sendMessage(ColorUtil.translate("§8▏"));
                if (args.length == 2 && args[1].equalsIgnoreCase("--players")) {
                    queueManager.getServerQueues().forEach((server, serverQueue) -> {
                        Queue<Player> players = serverQueue.getPlayerQueue();
                        player.sendMessage(ColorUtil.translate("§8▏ " + ColorUtil.GREEN + server + " §8(§7" + players.size() + "§8): §7" + players.stream().map(Player::getUsername).reduce((a, b) -> a + ", " + b).orElse("")));
                    });
                } else {
                    queueManager.getServerQueues().forEach((server, serverQueue) -> {
                        Queue<Player> players = serverQueue.getPlayerQueue();
                        player.sendMessage(ColorUtil.translate("§8▏ " + ColorUtil.GREEN + server + "§8: §7" + players.size() + " players"));
                    });
                }
                player.sendMessage(ColorUtil.translate("§8▏"));
                player.sendMessage(ColorUtil.translate("§8▏"));
                player.sendMessage(ColorUtil.translate("§8▏ §7Queue Time: " + ColorUtil.GREEN + (CrazyQueueVelocity.PROCESS_TIMER * 1000) + "ms"));
                player.sendMessage(ColorUtil.translate("§8▏"));
                player.sendMessage(ColorUtil.translate("§8▏"));
                return;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("clear")) {
                String server = args[1];
                ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "The server " + ColorUtil.RED + server + " §7does not exist."));
                    return;
                }
                serverQueue.clear();
                player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "The queue of the server " + ColorUtil.RED + server + " §7has been cleared."));
                return;
            }
            if (args[0].equalsIgnoreCase("queue")) {
                proxyServer.getCommandManager().executeAsync(player, "queue " + args[1]);
                return;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("connect")) {
                String server = args[1];
                ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "The server " + ColorUtil.RED + server + " §7does not exist."));
                    return;
                }
                try {
                    int amount = Math.max(0, Math.min(Integer.parseInt(args[2]), serverQueue.getPlayerQueue().size()));
                    serverQueue.connect(amount);
                    player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + amount + " player" + (amount != 1 ? "s" : "") + " will be connected."));
                    return;
                } catch (NumberFormatException ignored) { }
            }
            if (args[0].equalsIgnoreCase("send")) {
                String targetS = args[1];

                String server = args[2];
                ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "The server " + ColorUtil.RED + server + " §7does not exist."));
                    return;
                }

                List<Player> targets = new ArrayList<>();

                switch (targetS) {
                    case "all" -> {
                        if (!player.hasPermission("crazyqueue.command.sendall")) {
                            player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "You do not have permission to send all players"));
                            return;
                        }
                        targets.addAll(proxyServer.getAllPlayers().stream().filter(target -> !target.getUsername().equalsIgnoreCase(player.getUsername())).toList());
                    }
                    case "current" -> player.getCurrentServer().ifPresent(serverConnection -> targets.addAll(serverConnection.getServer().getPlayersConnected().stream().filter(target -> !target.getUsername().equalsIgnoreCase(player.getUsername())).toList()));
                    case "hub" -> targets.addAll(proxyServer.getAllPlayers().stream().filter(target -> target.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().toLowerCase().contains("hub")).orElse(false)).toList());
                    default -> {
                        Player target = proxyServer.getPlayer(targetS).orElse(null);
                        if (target == null) {
                            player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "The player " + ColorUtil.RED + targetS + " §7is not online."));
                            return;
                        }
                        targets.add(target);
                    }
                }

                targets.removeIf(target -> target.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(server)).orElse(false));
                targets.forEach(serverQueue::add);

                player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + ColorUtil.GREEN + targets.size() + " §7player" + (targets.size() != 1 ? "s" : "") + " have been added to the queue for " + ColorUtil.GREEN + server + "§7."));
                return;
            }
        }

        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " info"));
        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " info --players"));
        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " clear <server>"));
        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " queue <server>"));
        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " connect <queue> <amount>"));
        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " send <playerName / all / current / hub> <server>"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> list = new ArrayList<>();
        String[] args = CommandUtil.getArgs(invocation);

        if ((args.length == 2 && (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("queue") || args[0].equalsIgnoreCase("connect"))) || (args.length == 3 && (args[0].equalsIgnoreCase("send")))) {
            list.addAll(queueManager.getAllServerNames());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            if (invocation.source().hasPermission("crazyqueue.command.sendall")) {
                list.add("all");
            }
            list.add("current");
            list.add("hub");
            proxyServer.getAllPlayers().forEach(player -> list.add(player.getUsername()));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            list.add("--players");
        }

        if (args.length == 1) {
            list.addAll(Arrays.asList("info", "clear", "queue", "connect", "send"));
        }

        return CommandUtil.finishComplete(list, args);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("crazyqueue.command");
    }

}
