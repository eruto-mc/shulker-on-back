package net.erutobusiness.shulkeronback;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * 背中のアイテムが持つ {@link IItemHandler}（Forge のアイテム収納機能）の中身を、
 * そのまま読み書きする入れ物。
 *
 * <p>矢筒（Supplementaries）のように<b>中身を capability で持つアイテム</b>のためのもの。
 * シュルカーボックスのように NBT を直接触るのではなく、持ち主のMODが用意した
 * ハンドラ越しに書くので、<b>そのMODの制限（何が入るか・1スロットに何個か）がそのまま効く</b>。
 * 矢筒なら「矢しか入らない」が保たれる（`Cap.isItemValid` が判定する）。
 *
 * <p>画面の広さはバニラの箱に合わせて9の倍数にする必要があるので、
 * <b>足りない分は使えないスロットとして埋める</b>（矢筒は既定6スロット→9枠のうち3枠が飾り）。
 *
 * <p><b>変更のたびに書き戻す</b>のは {@link BackShulkerContainer} と同じ理由
 * （開いている最中に落ちても巻き戻らないようにする）。
 */
public class BackHandlerContainer extends SimpleContainer {

    private final ItemStack backpack;
    private final IItemHandler handler;
    private final int realSlots;
    private boolean loading;

    /** 画面に必要な枠数（9の倍数）を返す。1〜54 に収める。 */
    public static int screenSize(IItemHandler handler) {
        int rows = Math.max(1, Math.min(6, (handler.getSlots() + 8) / 9));
        return rows * 9;
    }

    public BackHandlerContainer(ItemStack backpack, IItemHandler handler) {
        super(screenSize(handler));
        this.backpack = backpack;
        this.handler = handler;
        this.realSlots = handler.getSlots();
        load();
    }

    public ItemStack getBackpack() {
        return this.backpack;
    }

    private void load() {
        this.loading = true;
        try {
            for (int i = 0; i < this.realSlots; i++) {
                setItem(i, this.handler.getStackInSlot(i).copy());
            }
        } finally {
            this.loading = false;
        }
    }

    /**
     * 埋めぶんの枠には何も置けない。実スロットは<b>持ち主のMODの判定に委ねる</b>。
     *
     * <p>バニラの {@code Slot.mayPlace} がここを呼ぶので、これだけで画面側の制限になる。
     */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot >= this.realSlots) {
            return false;
        }
        return this.handler.isItemValid(slot, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.loading) {
            save();
        }
    }

    private void save() {
        if (!(this.handler instanceof IItemHandlerModifiable writable)) {
            return;
        }
        for (int i = 0; i < this.realSlots; i++) {
            ItemStack want = getItem(i);
            if (!ItemStack.matches(writable.getStackInSlot(i), want)) {
                writable.setStackInSlot(i, want.copy());
            }
        }
    }
}
