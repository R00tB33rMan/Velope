package me.theseems.velope.commands;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.theseems.velope.Velope;
import me.theseems.velope.config.user.VelopeConfig;
import me.theseems.velope.server.VelopedServer;
import me.theseems.velope.server.VelopedServerRepository;
import me.theseems.velope.utils.ConnectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;

import static me.theseems.velope.utils.ConnectionUtils.*;

public class LobbyCommand implements SimpleCommand {
    private static final String LOBBY_COMMAND_USE_PERMISSION = "velope.use.lobby";

    @Inject
    private VelopeConfig velopeConfig;
    @Inject
    private VelopedServerRepository serverRepository;
    @Inject
    private Velope velope;

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command is only for ingame use."));
            return;
        }
        if (!source.hasPermission(LOBBY_COMMAND_USE_PERMISSION)) {
            source.sendMessage(Component
                    .text("You don't have permission to use that command.")
                    .color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) source;
        Collection<String> excluded = ConnectionUtils.getExclusionListForPlayer(player);
        String currentServerName = player.getCurrentServer()
                .map(ServerConnection::getServerInfo)
                .map(ServerInfo::getName)
                .orElse(null);

        RegisteredServer destination;
        if (currentServerName == null) {
            destination = findWithBalancer(
                    velope.getProxyServer(),
                    serverRepository.getServer(velopeConfig.getRootGroup()),
                    player.getUniqueId(),
                    excluded);
        } else {
            destination = findNearestAvailable(
                    velope.getProxyServer(),
                    player.getUniqueId(),
                    serverRepository.findParent(currentServerName)
                            .map(VelopedServer::getParent)
                            .orElse(null),
                    excluded);
        }

        if (destination == null) {
            source.sendMessage(Component
                    .text("There are no available lobbies. Try again later.")
                    .color(NamedTextColor.RED));
            return;
        }

        connectAndSupervise(player, destination);
    }
}
