package net.vertrauterdavid.queue.velocity.command;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;
import net.vertrauterdavid.queue.velocity.queue.ServerQueue;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;
import net.vertrauterdavid.queue.velocity.util.CommandUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueueCommand implements RawCommand {

    private final String name;
    private final QueueManager queueManager;

    public QueueCommand(String name) {
        this.name = name;
        this.queueManager = CrazyQueueVelocity.getInstance().getQueueManager();

        CrazyQueueVelocity.getInstance().getProxyServer().getCommandManager().register(name, this);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;
        String[] args = CommandUtil.getArgs(invocation);

        // remove first if empty
        if (args.length > 0 && args[0].replaceAll(" ", "").equalsIgnoreCase("")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length == 1) {
            String server = args[0];
            ServerQueue serverQueue = queueManager.getQueue(server);
            if (serverQueue == null) {
                player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "The server " + ColorUtil.RED + server + " §7does not exist."));
                return;
            }

            if (serverQueue.getPlayerQueue().contains(player)) {
                player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "You are already in the queue for " + ColorUtil.RED + server + "§7."));
                return;
            }

            if (player.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(server)).orElse(false)) {
                player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "You are already on the server " + ColorUtil.RED + server + "§7."));
                return;
            }

            queueManager.leaveAllQueues(player);
            serverQueue.add(player);
            return;
        }

        player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "Please use: " + ColorUtil.RED + "/" + name + " <server>"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> list = new ArrayList<>();
        String[] args = CommandUtil.getArgs(invocation);

        if (args.length == 1) {
            list.addAll(queueManager.getAllServerNames());
        }

        return CommandUtil.finishComplete(list, args);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("crazyqueue.queue");
    }

}
