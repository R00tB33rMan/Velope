package me.theseems.velope.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.theseems.velope.server.VelopedServer;
import me.theseems.velope.server.VelopedServerRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class ConnectionUtils {
    public static void connectAndSupervise(ProxyServer proxyServer, Player player, VelopedServer velopedServer) {
        velopedServer.getBalanceStrategy()
                .getStrategy()
                .getOptimalServer(velopedServer)
                .flatMap(serverInfo -> proxyServer.getServer(serverInfo.getName()))
                .ifPresentOrElse(
                        (server) -> connectAndSupervise(player, server),
                        () -> player.sendMessage(
                                Component.text("Sorry. Could not find destination. Try again later.")
                                        .color(NamedTextColor.RED)
                        )
                );
    }

    public static void connectAndSupervise(Player player, RegisteredServer registeredServer) {
        player.createConnectionRequest(registeredServer)
                .connect()
                .whenCompleteAsync((result, throwable) -> {
                    if (!result.isSuccessful() || throwable != null) {
                        player.sendMessage(
                                Component.text("Cannot connect you: ")
                                        .color(NamedTextColor.RED)
                                        .append(throwable != null
                                                ? Component.text(throwable.getMessage())
                                                : result.getReasonComponent().orElse(
                                                Component.text(result.getStatus().name()))));
                    }
                });
    }

    public static RegisteredServer findNearestAvailable(VelopedServerRepository repository, ProxyServer proxyServer, String serverName) {
        if (serverName == null) {
            return null;
        }

        VelopedServer parent = repository.getParent(serverName);
        return findNearestAvailable(proxyServer, parent == null ? repository.getServer(serverName) : parent);
    }

    public static RegisteredServer findNearestAvailable(ProxyServer proxyServer, VelopedServer origin) {
        if (origin == null) {
            return null;
        }

        RegisteredServer registeredServer = null;
        while (origin != null) {
            Optional<RegisteredServer> server = origin.getBalanceStrategy()
                    .getStrategy()
                    .getOptimalServer(origin)
                    .flatMap(serverInfo -> proxyServer.getServer(serverInfo.getName()));

            if (server.isPresent()) {
                registeredServer = server.get();
                break;
            }

            origin = origin.getParent();
        }

        return registeredServer;
    }

    public static RegisteredServer findWithBalancer(ProxyServer proxyServer, VelopedServer velopedServer) {
        return velopedServer
                .getBalanceStrategy()
                .getStrategy()
                .getOptimalServer(velopedServer)
                .flatMap(serverInfo -> proxyServer.getServer(serverInfo.getName()))
                .orElse(null);
    }
}
