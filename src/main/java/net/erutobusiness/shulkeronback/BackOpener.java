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
            // ⚠ **開けないものは「入れ替える」に回す**（2026-08-10）。
            //    背中の枠は1つしかないのに、入れ替え（V）と開く（B）で
            //    キーが分かれていた。B に寄せて V を明け渡した。
            case NONE, OTHER -> swapOrTell(player);
            case SHULKER_BOX -> openShulker(player, onBack);
            case ENDER_CHEST -> openEnderChest(player);
            case ITEM_CONTAINER -> openItemContainer(player, onBack);
        }
    }

    /**
     * 背中と主手を入れ替える。できないときだけ理由を出す。
     *
     * <p>断り方を2つに分けている。<b>何も持っていない</b>のと
     * <b>持っているが背負えない</b>のとでは、次にやることが違うため。
     */
    private static void swapOrTell(ServerPlayer player) {
        if (BackSlotAccess.swap(player)) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.6F, 1.0F);
            return;
        }
        if (player.getMainHandItem().isEmpty()) {
            tell(player, "message.shulker_on_back.empty");
        } else {
            tell(player, "message.shulker_on_back.cannot_equip");
        }
    }

    /**
     * 中身を capability で持つアイテム（矢筒など）を開く。
     *
     * <p>音は素材が分からないので<b>持ち物をしまう音</b>を使う。
     */
    private static void openItemContainer(ServerPlayer player, ItemStack onBack) {
        BackSlotAccess.handlerOf(onBack).ifPresentOrElse(handler -> {
            BackHandlerContainer container = new BackHandlerContainer(onBack, handler);
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (id, inv, p) -> new BackHandlerMenu(id, inv, container),
                    onBack.getHoverName()));
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.6F, 1.0F);
        }, () -> tell(player, "message.shulker_on_back.not_storage"));
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
