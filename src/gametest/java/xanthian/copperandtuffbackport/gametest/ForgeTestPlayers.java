package xanthian.copperandtuffbackport.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;

public final class ForgeTestPlayers {
    public static ServerPlayer creative(GameTestHelper context) {
        var level = context.getLevel();
        var player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "test-mock-player"));
        player.connection = new SilentConnection(level.getServer(), player);
        player.setGameMode(GameType.CREATIVE);
        return player;
    }
    private static final class SilentConnection extends ServerGamePacketListenerImpl {
        private SilentConnection(net.minecraft.server.MinecraftServer server, ServerPlayer player) {
            super(server, new Connection(PacketFlow.SERVERBOUND), player);
        }
        @Override public void send(Packet<?> packet) { }
        @Override public void send(Packet<?> packet, PacketSendListener listener) { }
    }
    private ForgeTestPlayers() { }
}
