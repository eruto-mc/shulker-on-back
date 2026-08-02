package net.erutobusiness.shulkeronback.net;

import java.util.function.Supplier;

import net.erutobusiness.shulkeronback.BackOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 「背中を開いて」だけを伝える。中身の情報は一切載せない
 * （クライアントを信用せず、サーバー側で背中スロットを読み直す）。
 */
public class OpenBackPacket {

    public OpenBackPacket() {
    }

    public static void encode(OpenBackPacket packet, FriendlyByteBuf buf) {
    }

    public static OpenBackPacket decode(FriendlyByteBuf buf) {
        return new OpenBackPacket();
    }

    public static void handle(OpenBackPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                BackOpener.open(player);
            }
        });
        context.setPacketHandled(true);
    }
}
