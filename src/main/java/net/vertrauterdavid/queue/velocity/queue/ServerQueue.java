package net.vertrauterdavid.queue.velocity.queue;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class ServerQueue {

    private final RegisteredServer registeredServer;
    private final Queue<Player> players = new LinkedList<>();

    @Setter
    private boolean processing = false;

    public void startScheduler() {
        CrazyQueueVelocity.getInstance().getProxyServer().getScheduler().buildTask(CrazyQueueVelocity.getInstance(), this::process).repeat((long) (CrazyQueueVelocity.PROCESS_TIMER * 1000), TimeUnit.MILLISECONDS).schedule();
    }

    private void process() {
        synchronized (players) {
            if (players.isEmpty()) return;
            players.removeIf(player -> player.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(registeredServer.getServerInfo().getName())).orElse(false));
            players.removeIf(player -> !player.isActive());

            sendActionbar();

            if (players.isEmpty()) return;
            Player player = players.peek();
            player.createConnectionRequest(registeredServer).connect().thenAccept(result -> {
                ConnectionRequestBuilder.Status status = result.getStatus();

                if (status == ConnectionRequestBuilder.Status.SUCCESS) {
                    players.poll();
                    player.sendActionBar(ColorUtil.translate("§aSuccessfully connected to " + registeredServer.getServerInfo().getName()));
                }

                /*
                // removes the player from the queue if the server disconnects the player
                if (status == ConnectionRequestBuilder.Status.SERVER_DISCONNECTED) {
                    players.poll();
                    player.sendActionBar(ColorUtil.translate("§cFailed to connect to " + registeredServer.getServerInfo().getName()));
                }
                 */
            });
        }
    }

    private void sendActionbar() {
        int position = 1;
        for (Player player : players) {
            player.sendActionBar(ColorUtil.translate("§a#" + position + " §7in the queue to §a" + registeredServer.getServerInfo().getName()));
            position++;
        }
    }

    public void add(Player player) {
        synchronized (players) {
            players.add(player);
        }
    }

    public void remove(Player player) {
        synchronized (players) {
            if (!players.contains(player)) return;
            players.remove(player);
            player.sendActionBar(ColorUtil.translate(" "));
        }
    }

    public void clear() {
        synchronized (players) {
            while (!players.isEmpty()) {
                players.poll().sendActionBar(ColorUtil.translate(" "));
            }
        }
    }

}
