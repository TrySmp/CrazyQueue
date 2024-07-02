package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

public class DisconnectListener {

    @Subscribe
    public void handle(DisconnectEvent event) {
        CrazyQueueVelocity.getInstance().getQueueManager().leaveAllQueues(event.getPlayer());
    }

}
