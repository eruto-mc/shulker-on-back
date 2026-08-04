package net.erutobusiness.shulkeronback;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * 背負った「中身を持つアイテム」（矢筒など）用の画面。
 *
 * <p>バニラの箱の画面をそのまま使う。独自の MenuType を作らずに済む＝
 * クライアント側の登録が要らない（{@link BackShulkerMenu} と同じ考え方）。
 *
 * <p>何を入れられるかは {@link BackHandlerContainer#canPlaceItem} が持ち主のMODへ委ねるので、
 * ここでは<b>閉じる条件だけ</b>を見る。
 */
public class BackHandlerMenu extends ChestMenu {

    private static final MenuType<?>[] BY_ROWS = {
            MenuType.GENERIC_9x1, MenuType.GENERIC_9x2, MenuType.GENERIC_9x3,
            MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6,
    };

    private final BackHandlerContainer container;

    @SuppressWarnings("unchecked")
    public BackHandlerMenu(int containerId, Inventory playerInventory, BackHandlerContainer container) {
        super((MenuType<ChestMenu>) BY_ROWS[container.getContainerSize() / 9 - 1],
                containerId, playerInventory, container, container.getContainerSize() / 9);
        this.container = container;
    }

    @Override
    public boolean stillValid(Player player) {
        // 参照の同一性で見る（背中スロットの ItemStack そのものを編集しているため）。
        ItemStack onBack = BackSlotAccess.get(player);
        return onBack == this.container.getBackpack() && !onBack.isEmpty();
    }
}
