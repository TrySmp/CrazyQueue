package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageListener {

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getIdentifier().getId().equals("crazyqueue:toproxy"))) return;
        if (!(event.getTarget() instanceof Player target)) return;

        handle(target, event.getData());
    }

    public void handle(Player player, byte[] data) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data));
            String message = dataInputStream.readUTF();

            if (message.startsWith("command ")) {
                CrazyQueueVelocity.getInstance().getProxyServer().getCommandManager().executeAsync(player, message.replaceAll("command ", ""));
            }
        } catch (IOException ignored) { }
    }

}
