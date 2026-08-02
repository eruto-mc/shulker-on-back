package net.erutobusiness.shulkeronback;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * 背負っているシュルカーボックスの中身を、そのアイテムの NBT に直接読み書きする入れ物。
 *
 * <p><b>変更のたびに書き戻す</b>。画面を閉じたときだけ保存する作りにすると、
 * 開いている最中にサーバが落ちたり、プレイヤーが死んだりしたときに中身が巻き戻る。
 * 27スロットぶんの NBT 書き込みなので、頻度が上がっても負荷は問題にならない。
 */
public class BackShulkerContainer extends SimpleContainer {

    /** シュルカーボックスの中身が入っているバニラの NBT キー。 */
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    public static final int SIZE = 27;

    private final ItemStack backpack;
    private boolean loading;

    public BackShulkerContainer(ItemStack backpack) {
        super(SIZE);
        this.backpack = backpack;
        load();
    }

    public ItemStack getBackpack() {
        return this.backpack;
    }

    private void load() {
        this.loading = true;
        try {
            CompoundTag tag = this.backpack.getTag();
            if (tag == null || !tag.contains(BLOCK_ENTITY_TAG, CompoundTag.TAG_COMPOUND)) {
                return;
            }
            CompoundTag blockEntity = tag.getCompound(BLOCK_ENTITY_TAG);
            if (!blockEntity.contains("Items", CompoundTag.TAG_LIST)) {
                return;
            }
            NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(blockEntity, items);
            for (int i = 0; i < SIZE; i++) {
                setItem(i, items.get(i));
            }
        } finally {
            this.loading = false;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.loading) {
            save();
        }
    }

    private void save() {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        boolean empty = true;
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = getItem(i);
            items.set(i, stack);
            if (!stack.isEmpty()) {
                empty = false;
            }
        }

        CompoundTag tag = this.backpack.getOrCreateTag();
        // BlockEntityTag の他のキー（Lock・LootTable・CustomName 等）は残す。
        CompoundTag blockEntity = tag.contains(BLOCK_ENTITY_TAG, CompoundTag.TAG_COMPOUND)
                ? tag.getCompound(BLOCK_ENTITY_TAG)
                : new CompoundTag();

        if (empty) {
            // 空になったら Items を消す。中身が無いシュルカーボックスは NBT が無いほうが
            // バニラと同じようにスタックできる。BlockEntityTag ごと空なら丸ごと落とす。
            blockEntity.remove("Items");
            if (blockEntity.isEmpty()) {
                tag.remove(BLOCK_ENTITY_TAG);
                if (tag.isEmpty()) {
                    this.backpack.setTag(null);
                }
                return;
            }
        } else {
            ContainerHelper.saveAllItems(blockEntity, items, false);
        }
        tag.put(BLOCK_ENTITY_TAG, blockEntity);
    }
}
