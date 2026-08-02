package net.erutobusiness.shulkeronback.client;

import net.erutobusiness.shulkeronback.ShulkerOnBack;
import net.erutobusiness.shulkeronback.net.Net;
import net.erutobusiness.shulkeronback.net.OpenBackPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ClientEvents {

    private ClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = ShulkerOnBack.MODID, value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(Keybinds.OPEN_BACK);
        }
    }

    @Mod.EventBusSubscriber(modid = ShulkerOnBack.MODID, value = Dist.CLIENT,
            bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) {
                return;
            }
            // consumeClick は押し下げの回数を消費する＝押しっぱなしで連射しない
            while (Keybinds.OPEN_BACK.consumeClick()) {
                Net.CHANNEL.sendToServer(new OpenBackPacket());
            }
        }
    }
}
