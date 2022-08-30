package me.theseems.velope.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.theseems.velope.Velope;
import me.theseems.velope.config.user.VelopeConfig;
import me.theseems.velope.server.VelopedServer;
import me.theseems.velope.server.VelopedServerRepository;
import me.theseems.velope.utils.ConnectionUtils;
import net.kyori.adventure.text.Component;

import java.util.Optional;

import static me.theseems.velope.utils.ConnectionUtils.findNearestAvailable;

public class VelopeServerInitialListener {
    @Inject
    private Velope velope;
    @Inject
    @Named("initial")
    private VelopedServer velopedServer;

    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        RegisteredServer server = ConnectionUtils.findNearestAvailable(velope.getProxyServer(), velopedServer);
        if (server == null) {
            velope.getLogger().info("Cannot find initial server: unavailable");
            return;
        }

        event.setInitialServer(server);
    }
}