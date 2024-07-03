package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

import java.util.Arrays;

public class ServerConnectedListener {

    @Subscribe
    public void handle(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer targetServer = event.getServer();

        if (Arrays.stream(CrazyQueueVelocity.DISABLED_QUEUES).toList().contains(targetServer.getServerInfo().getName())) {
            CrazyQueueVelocity.getInstance().getOldServers().remove(player);
            return;
        }

        CrazyQueueVelocity.getInstance().getOldServers().put(player, targetServer);
    }

}
