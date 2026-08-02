package net.erutobusiness.shulkeronback;

import net.erutobusiness.shulkeronback.net.Net;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShulkerOnBack.MODID)
public class ShulkerOnBack {

    public static final String MODID = "shulker_on_back";

    public ShulkerOnBack() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(Net::register);
    }
}
