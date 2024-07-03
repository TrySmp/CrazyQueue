package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.ServerQueue;

public class KickedFromServerListener {

    @Subscribe
    public void handle(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        if (player.getCurrentServer().isEmpty()) return;

        RegisteredServer oldServer = CrazyQueueVelocity.getInstance().getOldServers().getOrDefault(player, null);
        ServerQueue serverQueue = CrazyQueueVelocity.getInstance().getQueueManager().getQueue(oldServer == null ? null : oldServer.getServerInfo().getName());
        if (serverQueue == null) return;

        serverQueue.add(player);
    }

}
