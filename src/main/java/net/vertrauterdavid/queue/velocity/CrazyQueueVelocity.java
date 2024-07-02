package net.vertrauterdavid.queue.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.vertrauterdavid.queue.velocity.command.CrazyQueueCommand;
import net.vertrauterdavid.queue.velocity.command.LeaveQueueCommand;
import net.vertrauterdavid.queue.velocity.command.QueueCommand;
import net.vertrauterdavid.queue.velocity.listener.DisconnectListener;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;

import javax.inject.Inject;

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

    @Getter
    private static CrazyQueueVelocity instance;
    private final ProxyServer proxyServer;
    private QueueManager queueManager;

    @Inject
    public CrazyQueueVelocity(ProxyServer server) {
        instance = this;
        proxyServer = server;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        queueManager = new QueueManager();

        new CrazyQueueCommand("crazyqueue");
        new LeaveQueueCommand("leavequeue");
        new QueueCommand("queue");

        proxyServer.getEventManager().register(this, new DisconnectListener());
    }

}
