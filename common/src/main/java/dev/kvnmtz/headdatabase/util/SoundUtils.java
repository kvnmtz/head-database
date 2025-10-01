package dev.kvnmtz.headdatabase.util;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public abstract class SoundUtils {

    public static void playSoundForPlayer(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        var packet = new ClientboundSoundPacket(Holder.direct(sound), SoundSource.PLAYERS, player.getX(),
                player.getY(), player.getZ(), volume, pitch, player.level().getRandom().nextLong());

        player.connection.send(packet);
    }
}
