package net.erutobusiness.shulkeronback;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

    /**
     * 最後に自分が読んだ、または書いた {@code BlockEntityTag.Items}（無ければ null）。
     *
     * <p>画面を開いている間に<b>ほかの経路が箱の中身を書き換える</b>ことがある——
     * Spoiled が箱の中の食べ物を腐らせるとき、Forge の {@code ShulkerItemStackInvWrapper} 経由で
     * 箱の NBT を書く。そのまま次のクリックで自分のコピーを書くと、腐った肉がパンに戻り、
     * 画面を開いている間に窓口から入った物は消える（2026-09-26・mod-notes/shulker-box-item-contents.md）。
     * ⚠ 書く側（バニラの {@code ContainerHelper.saveAllItems}・Forge の窓口・自分）は書くたびに
     * 新しい一覧を作って {@code Items} に入れるので、<b>物の同一性</b>で差し替わったかが分かる。
     * ⚠ 中身で比べない——Spoiled は腐敗の進みをその場で書き換えるので、毎回「変わった」になる。
     */
    private Tag lastItems;

    public BackShulkerContainer(ItemStack backpack) {
        super(SIZE);
        this.backpack = backpack;
        load();
    }

    public ItemStack getBackpack() {
        return this.backpack;
    }

    /** 箱の中身の一覧が、自分の知らない間に差し替わっていたら読み直す。 */
    public void reloadIfChangedElsewhere() {
        if (currentItems() != this.lastItems) {
            load();
        }
    }

    private Tag currentItems() {
        CompoundTag blockEntity = this.backpack.getTagElement(BLOCK_ENTITY_TAG);
        return blockEntity == null ? null : blockEntity.get("Items");
    }

    private void load() {
        this.loading = true;
        try {
            // 読み直しにも使うので、Items が無いときも全枠を空で上書きする（前の中身を残さない）。
            NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            CompoundTag blockEntity = this.backpack.getTagElement(BLOCK_ENTITY_TAG);
            if (blockEntity != null && blockEntity.contains("Items", CompoundTag.TAG_LIST)) {
                ContainerHelper.loadAllItems(blockEntity, items);
            }
            for (int i = 0; i < SIZE; i++) {
                setItem(i, items.get(i));
            }
            this.lastItems = currentItems();
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
                this.lastItems = currentItems();
                return;
            }
        } else {
            ContainerHelper.saveAllItems(blockEntity, items, false);
        }
        tag.put(BLOCK_ENTITY_TAG, blockEntity);
        this.lastItems = currentItems();
    }
}
