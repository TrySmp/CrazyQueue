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

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                queueManager.getServerQueues().forEach((server, serverQueue) -> {
                    Queue<Player> players = serverQueue.getPlayers();
                    player.sendMessage(ColorUtil.translate("§a" + server + " §8- §a" + players.size() + " §8: §7" + players.stream().map(Player::getUsername).reduce((a, b) -> a + ", " + b).orElse(""))); // todo
                });
                return;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("clear")) {
                String server = args[1];
                ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate("§7The server §c" + server + " §7does not exist."));
                    return;
                }
                serverQueue.clear();
                player.sendMessage(ColorUtil.translate("§7The queue of the server §c" + server + " §7has been cleared."));
                return;
            }
            if (args[0].equalsIgnoreCase("queue")) {
                proxyServer.getCommandManager().executeAsync(player, "queue " + args[1]);
                return;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("send")) {
                String targetS = args[1];

                String server = args[2];
                ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate("§7The server §c" + server + " §7does not exist."));
                    return;
                }

                List<Player> targets = new ArrayList<>();

                switch (targetS) {
                    case "all" -> targets.addAll(proxyServer.getAllPlayers());
                    case "current" -> player.getCurrentServer().ifPresent(serverConnection -> targets.addAll(serverConnection.getServer().getPlayersConnected()));
                    default -> {
                        Player target = proxyServer.getPlayer(targetS).orElse(null);
                        if (target == null) {
                            player.sendMessage(ColorUtil.translate("§7The player §c" + targetS + " §7is not online."));
                            return;
                        }
                        targets.add(target);
                    }
                }

                targets.removeIf(target -> target.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(server)).orElse(false));
                targets.forEach(serverQueue::add);

                player.sendMessage(ColorUtil.translate("§a" + targets.size() + " §7player" + (targets.size() != 1 ? "s" : "") + " have been added to the queue for §a" + server + "§7."));
                return;
            }
        }

        player.sendMessage(ColorUtil.translate("§7Please use: §c/" + name + " list"));
        player.sendMessage(ColorUtil.translate("§7Please use: §c/" + name + " clear <server>"));
        player.sendMessage(ColorUtil.translate("§7Please use: §c/" + name + " queue <server>"));
        player.sendMessage(ColorUtil.translate("§7Please use: §c/" + name + " send <playerName / all / current> <server>"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> list = new ArrayList<>();
        String[] args = CommandUtil.getArgs(invocation);

        if ((args.length == 2 && (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("queue"))) || (args.length == 3 && args[0].equalsIgnoreCase("send"))) {
            list.addAll(queueManager.getAllServerNames());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            list.addAll(Arrays.asList("all", "current"));
            proxyServer.getAllPlayers().forEach(player -> list.add(player.getUsername()));
        }

        if (args.length == 1) {
            list.addAll(Arrays.asList("list", "clear", "queue", "send"));
        }

        return CommandUtil.finishComplete(list, args);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("crazyqueue.command");
    }

}
