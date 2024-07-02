package net.vertrauterdavid.queue.velocity.queue;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

import java.util.HashMap;
import java.util.List;

@Getter
public class QueueManager {

    private final HashMap<String, ServerQueue> serverQueues = new HashMap<>();
    private final HashMap<String, ServerListener> serverListeners = new HashMap<>();

    public QueueManager() {
        CrazyQueueVelocity.getInstance().getProxyServer().getAllServers().forEach((registeredServer) -> {
            ServerQueue serverQueue = new ServerQueue(registeredServer);
            serverQueue.startScheduler();
            serverQueues.put(registeredServer.getServerInfo().getName().toLowerCase(), serverQueue);

            ServerListener serverListener = new ServerListener(registeredServer, serverQueue);
            serverListener.startPinging();
            serverListeners.put(registeredServer.getServerInfo().getName().toLowerCase(), serverListener);
        });
    }

    public void leaveAllQueues(Player player) {
        serverQueues.values().forEach(serverQueue -> serverQueue.remove(player));
    }

    public List<String> getAllServerNames() {
        return CrazyQueueVelocity.getInstance().getProxyServer().getAllServers().stream().map(server -> server.getServerInfo().getName()).toList();
    }

    public ServerQueue getQueue(String server) {
        return serverQueues.get(server.toLowerCase());
    }

}
