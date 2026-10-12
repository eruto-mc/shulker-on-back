package net.erutobusiness.shulkeronback;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;

/**
 * 背負ったシュルカーボックス用の画面。
 *
 * <p>バニラの {@link ShulkerBoxMenu} をそのまま使う。理由は2つ:
 * <ul>
 *   <li>スロットが {@code ShulkerBoxSlot} なので、<b>シュルカーの中にシュルカーを入れられない</b>
 *       というバニラの制限がそのまま効く。自前でスロットを組むとこれを作り直す必要がある</li>
 *   <li>{@code MenuType.SHULKER_BOX} はクライアント側に画面が登録済み。
 *       独自の MenuType を作らずに済む＝クライアント側の登録が要らない</li>
 * </ul>
 *
 * <p>閉じる条件は「背中スロットに同じアイテムが入り続けていること」。
 * 開いている最中に抜かれた・死んだ・別の物に替えられた場合は画面を閉じる。
 */
public class BackShulkerMenu extends ShulkerBoxMenu {

    private final BackShulkerContainer container;

    public BackShulkerMenu(int containerId, Inventory playerInventory, BackShulkerContainer container) {
        super(containerId, playerInventory, container);
        this.container = container;
    }

    /**
     * 開いている間に箱の中身がほかの経路で書き換わったら、画面のコピーを読み直してから同期する。
     *
     * <p>サーバは開いている画面ごとに毎 tick これを呼ぶ（{@code ServerPlayer.tick}）ので、
     * 部員の画面にも1 tick 遅れで新しい中身が出る。
     */
    @Override
    public void broadcastChanges() {
        this.container.reloadIfChangedElsewhere();
        super.broadcastChanges();
    }

    /**
     * クリックの前にも読み直す。⚠ 窓口からの書き込みとクリックの間に {@code broadcastChanges} が
     * 挟まるとは限らない（クリックの通信はプレイヤーの tick とは別に処理される）ので、
     * 古いコピーを土台にクリックを処理して書き戻さないよう、ここでも見る。
     */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        this.container.reloadIfChangedElsewhere();
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        // 参照の同一性で見る。背中スロットの ItemStack そのものを編集しているので、
        // 別のスタックに替わった時点で編集先が宙に浮く。
        ItemStack onBack = BackSlotAccess.get(player);
        return onBack == this.container.getBackpack() && !onBack.isEmpty();
    }
}
