package net.vertrauterdavid.queue.velocity.command;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;

import java.util.ArrayList;
import java.util.List;

public class LeaveQueueCommand implements RawCommand {

    private final QueueManager queueManager;

    public LeaveQueueCommand(String name) {
        this.queueManager = CrazyQueueVelocity.getInstance().getQueueManager();

        CrazyQueueVelocity.getInstance().getProxyServer().getCommandManager().register(name, this);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;
        queueManager.leaveAllQueues(player);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return new ArrayList<>();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("crazyqueue.queue");
    }

}
