package net.vertrauterdavid.queue.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import net.vertrauterdavid.queue.velocity.command.CrazyQueueCommand;
import net.vertrauterdavid.queue.velocity.command.LeaveQueueCommand;
import net.vertrauterdavid.queue.velocity.command.QueueCommand;
import net.vertrauterdavid.queue.velocity.listener.DisconnectListener;
import net.vertrauterdavid.queue.velocity.listener.KickedFromServerListener;
import net.vertrauterdavid.queue.velocity.listener.PluginMessageListener;
import net.vertrauterdavid.queue.velocity.listener.ServerConnectedListener;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;

import javax.inject.Inject;
import java.util.WeakHashMap;

@Plugin(
        id = "crazyqueue",
        name = "CrazyQueue",
        version = "1.0",
        authors = {"VertrauterDavid"}
)
@Getter
public class CrazyQueueVelocity {

    public static final double PROCESS_TIMER = 0.5; // time in seconds between each queue process
    public static final double PING_TIMER = 5; // time in seconds between each ping to the queue servers
    public static final String[] DISABLED_QUEUES = new String[] { // servers that should not be enabled as server queues
            "main", // main server on development network
            "Hub-1",
            "Hub-2",
            "Hub-3",
            "Hub-4",
            "Hub-5"
    };

    @Getter
    private static CrazyQueueVelocity instance;
    private final ProxyServer proxyServer;
    private QueueManager queueManager;

    private final WeakHashMap<Player, RegisteredServer> oldServers = new WeakHashMap<>();

    @Inject
    public CrazyQueueVelocity(ProxyServer server) {
        instance = this;
        proxyServer = server;
        proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.from("crazyqueue:tobukkit"));
        proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.from("crazyqueue:toproxy"));
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        queueManager = new QueueManager();

        new CrazyQueueCommand("crazyqueue");
        new LeaveQueueCommand("leavequeue");
        new QueueCommand("queue");

        proxyServer.getEventManager().register(this, new DisconnectListener());
        proxyServer.getEventManager().register(this, new KickedFromServerListener());
        proxyServer.getEventManager().register(this, new PluginMessageListener());
        proxyServer.getEventManager().register(this, new ServerConnectedListener());
    }

}
