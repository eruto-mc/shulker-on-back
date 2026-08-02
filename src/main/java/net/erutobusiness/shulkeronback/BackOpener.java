package net.erutobusiness.shulkeronback;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

/**
 * 背中スロットの中身を開く（サーバー側）。
 */
public final class BackOpener {

    private BackOpener() {
    }

    public static void open(ServerPlayer player) {
        ItemStack onBack = BackSlotAccess.get(player);
        BackSlotAccess.Kind kind = BackSlotAccess.kindOf(onBack);

        switch (kind) {
            case NONE -> tell(player, "message.shulker_on_back.empty");
            case OTHER -> tell(player, "message.shulker_on_back.not_storage");
            case SHULKER_BOX -> openShulker(player, onBack);
            case ENDER_CHEST -> openEnderChest(player);
        }
    }

    private static void openShulker(ServerPlayer player, ItemStack onBack) {
        BackShulkerContainer container = new BackShulkerContainer(onBack);
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inv, p) -> new BackShulkerMenu(id, inv, container),
                onBack.getHoverName()));
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    private static void openEnderChest(ServerPlayer player) {
        PlayerEnderChestContainer ender = player.getEnderChestInventory();
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inv, p) -> ChestMenu.threeRows(id, inv, ender),
                Component.translatable("container.enderchest")));
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    private static void tell(ServerPlayer player, String key) {
        player.displayClientMessage(
                Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
    }
}
