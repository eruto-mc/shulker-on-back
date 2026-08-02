package net.erutobusiness.shulkeronback;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Curios の背中スロットに入っているものを読むだけの入口。
 *
 * <p>スロット名は Back Weapon Slot（curiosbackslot）が定義する {@code back_weapon} を使う。
 * そのMODが入っていなければ何も見つからないだけで、例外にはしない。
 */
public final class BackSlotAccess {

    /** Back Weapon Slot が data/curiosbackslot/curios/slots/back_weapon.json で定義しているスロット名。 */
    public static final String SLOT = "back_weapon";

    private BackSlotAccess() {
    }

    /** 背中スロットの中身を返す。空・スロット無し・Curios 無しはすべて空スタック。 */
    public static ItemStack get(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.getStacksHandler(SLOT))
                .map(handler -> handler.getStacks().getStackInSlot(0))
                .orElse(ItemStack.EMPTY);
    }

    /** 開ける対象か。シュルカーボックス（全色）とエンダーチェストだけを許す。 */
    public static Kind kindOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return Kind.NONE;
        }
        if (stack.is(Items.ENDER_CHEST)) {
            return Kind.ENDER_CHEST;
        }
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            return Kind.SHULKER_BOX;
        }
        return Kind.OTHER;
    }

    public enum Kind {
        /** 背中が空。 */
        NONE,
        /** 開けないものを背負っている（剣など）。 */
        OTHER,
        SHULKER_BOX,
        ENDER_CHEST
    }
}
