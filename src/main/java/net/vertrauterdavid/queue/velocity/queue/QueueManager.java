package net.vertrauterdavid.queue.velocity.queue;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Getter
public class QueueManager {

    private final HashMap<String, ServerQueue> serverQueues = new HashMap<>();
    private final HashMap<String, ServerListener> serverListeners = new HashMap<>();

    public QueueManager() {
        CrazyQueueVelocity.getInstance().getProxyServer().getAllServers().forEach((registeredServer) -> {
            if (Arrays.stream(CrazyQueueVelocity.DISABLED_QUEUES).toList().contains(registeredServer.getServerInfo().getName())) return;

            ServerQueue serverQueue = new ServerQueue(registeredServer);
            serverQueue.startScheduler();
            serverQueues.put(registeredServer.getServerInfo().getName().toLowerCase(), serverQueue);

            ServerListener serverListener = new ServerListener(registeredServer) {
                @Override
                public void markOnline() {
                    super.markOnline();
                    serverQueue.setProcessing(true);
                }
                @Override
                public void markOffline() {
                    super.markOffline();
                    serverQueue.setProcessing(false);
                }
            };
            serverListener.startPinging();
            serverListeners.put(registeredServer.getServerInfo().getName().toLowerCase(), serverListener);
        });
    }

    public void leaveAllQueues(Player player) {
        serverQueues.values().forEach(serverQueue -> serverQueue.remove(player));
    }

    public List<String> getAllServerNames() {
        return serverQueues.values().stream().map(serverQueue -> serverQueue.getRegisteredServer().getServerInfo().getName()).toList();
    }

    public ServerQueue getQueue(String server) {
        if (server == null) return null;
        return serverQueues.get(server.toLowerCase());
    }

}
