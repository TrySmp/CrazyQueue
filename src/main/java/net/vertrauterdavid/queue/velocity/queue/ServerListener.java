package net.vertrauterdavid.queue.velocity.queue;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
public class ServerListener {

    private final RegisteredServer registeredServer;
    private final ServerQueue serverQueue;

    private int status = 0; // 0 = unknown, 1 = online, 2 = offline

    public void startPinging() {
        CrazyQueueVelocity.getInstance().getProxyServer().getScheduler().buildTask(CrazyQueueVelocity.getInstance(), this::ping).repeat((long) (CrazyQueueVelocity.PING_TIMER * 1000), TimeUnit.SECONDS).schedule();
    }

    private void ping() {
        try {
            registeredServer.ping().get();
            markOnline();
        } catch (InterruptedException | ExecutionException e) {
            markOffline();
        }
    }

    private void markOnline() {
        if (status == 1) return;
        serverQueue.setProcessing(true);
        status = 1;
    }

    private void markOffline() {
        if (status == 2) return;
        serverQueue.setProcessing(false);
        status = 2;
    }

}
