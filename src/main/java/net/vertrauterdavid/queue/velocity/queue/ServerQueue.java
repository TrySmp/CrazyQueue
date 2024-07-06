package net.vertrauterdavid.queue.velocity.queue;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class ServerQueue {

    private final RegisteredServer registeredServer;
    private final Queue<Player> playerQueue = new ConcurrentLinkedQueue<>();

    @Setter
    private boolean processing = false;

    public void startScheduler() {
        CrazyQueueVelocity.getInstance().getProxyServer().getScheduler().buildTask(CrazyQueueVelocity.getInstance(), this::process).repeat((long) (CrazyQueueVelocity.PROCESS_TIMER * 1000), TimeUnit.MILLISECONDS).schedule();
    }

    private void process() {
        synchronized (playerQueue) {
            if (playerQueue.isEmpty()) return;
            playerQueue.removeIf(player -> player.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(registeredServer.getServerInfo().getName())).orElse(false));
            playerQueue.removeIf(player -> !player.isActive());

            sendActionbar();

            if (playerQueue.isEmpty()) return;
            Player player = playerQueue.peek();
            player.createConnectionRequest(registeredServer).connect().thenAccept(result -> {
                ConnectionRequestBuilder.Status status = result.getStatus();

                if (status == ConnectionRequestBuilder.Status.SUCCESS) {
                    playerQueue.poll();
                    player.sendActionBar(ColorUtil.translate(ColorUtil.GREEN + "Successfully connected to " + registeredServer.getServerInfo().getName()));
                }

                /*
                // removes the player from the queue if the server disconnects the player
                if (status == ConnectionRequestBuilder.Status.SERVER_DISCONNECTED) {
                    players.poll();
                    player.sendActionBar(ColorUtil.translate(ColorUtil.RED + "Failed to connect to " + registeredServer.getServerInfo().getName()));
                }
                 */
            });
        }
    }

    private void sendActionbar() {
        int position = 1;
        for (Player player : playerQueue) {
            player.sendActionBar(ColorUtil.translate(ColorUtil.GREEN + "#" + position + "§7 in the queue to " + ColorUtil.GREEN + "§n" + registeredServer.getServerInfo().getName() + "§r §8(§7Waiting: " + playerQueue.size() + "§8)"));
            position++;
        }
    }

    public void add(Player player) {
        synchronized (playerQueue) {
            if (playerQueue.contains(player)) return;

            if (player.hasPermission("crazyqueue.queue.priority")) {
                Queue<Player> tempQueue = new ConcurrentLinkedQueue<>();

                boolean added = false;
                for (Player queuedPlayer : playerQueue) {
                    if (!added && !queuedPlayer.hasPermission("crazyqueue.queue.priority")) {
                        tempQueue.add(player);
                        added = true;
                    }
                    tempQueue.add(queuedPlayer);
                }

                if (!added) {
                    tempQueue.add(player);
                }

                playerQueue.clear();
                playerQueue.addAll(tempQueue);
            } else {
                playerQueue.add(player);
            }

            player.sendMessage(ColorUtil.translate(ColorUtil.PREFIX + "You have been added to the queue for " + ColorUtil.GREEN + registeredServer.getServerInfo().getName()));
        }
    }

    public void remove(Player player) {
        synchronized (playerQueue) {
            if (!playerQueue.contains(player)) return;
            playerQueue.remove(player);
            player.sendActionBar(ColorUtil.translate(" "));
        }
    }

    public void clear() {
        synchronized (playerQueue) {
            while (!playerQueue.isEmpty()) {
                playerQueue.poll().sendActionBar(ColorUtil.translate(" "));
            }
        }
    }

}
