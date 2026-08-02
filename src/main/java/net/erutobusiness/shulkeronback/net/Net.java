package net.erutobusiness.shulkeronback.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("shulker_on_back", "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals);

    private Net() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenBackPacket.class,
                OpenBackPacket::encode, OpenBackPacket::decode, OpenBackPacket::handle);
    }
}
