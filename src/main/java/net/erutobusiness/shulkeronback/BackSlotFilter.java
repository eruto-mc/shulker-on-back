package net.erutobusiness.shulkeronback;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

/**
 * 背中スロットに入れてよいものを、アイテムタグ {@code curios:back_weapon} で決める。
 *
 * <p><b>なぜ要るか</b>: Back Weapon Slot（{@code curiosbackslot}）は
 * {@code CurioEquipEvent} で {@code Result.ALLOW} を立てるので、
 * <b>Curios のタグ判定を丸ごと素通りする</b>。設定 {@code disallow_curio_items} を
 * true にしても、弾かれるのは「自分の curios 枠を持つアイテム」（指輪・首飾りなど）だけで、
 * 土でも松明でも背負えたままになる。
 *
 * <p>そこで <b>後から結果を上書きする</b>。Forge のイベントは優先度の低いものが後に走るので、
 * {@code LOWEST} で登録すれば Back Weapon Slot（既定＝{@code NORMAL}）の結果を置き換えられる。
 *
 * <p><b>もう1つの入口</b>: {@link BackSlotAccess#swap(net.minecraft.world.entity.player.Player)}
 * （B キーの持ち替え）は {@code CuriosApi.isStackValid} を直接呼ぶ。こちらはイベントを投げないので、
 * <b>この class では直せない</b>。スロット定義に {@code "validators": ["curios:tag"]} を足す
 * datapack（{@code back_slot_filter}）が同じタグを見るようにしてある。
 * <b>2つの入口が同じタグ1本で決まる</b>のが、この作りの狙い。
 *
 * <p><b>タグが空のときは何もしない。</b> datapack はサーバ側にしか入らず、置き忘れると
 * タグが空になる。そこで DENY を返すと<b>背中スロットが丸ごと使えなくなる</b>ので、
 * 何もせず（＝Back Weapon Slot の判断のまま）ログに1回だけ残す。
 * 置き忘れそのものは {@code dev/verify/check_datapack_sync.py} が名指しする。
 */
@Mod.EventBusSubscriber(modid = ShulkerOnBack.MODID)
public final class BackSlotFilter {

    /** 背中に置いてよいものの一覧。datapack {@code back_slot_filter} が中身を書く。 */
    public static final TagKey<Item> ALLOWED =
            ItemTags.create(new ResourceLocation("curios", BackSlotAccess.SLOT));

    private static final Logger LOGGER = LogUtils.getLogger();

    /** タグが空だと分かったことを1回だけ知らせる（毎回の装備で鳴らさない）。 */
    private static boolean warned = false;

    private BackSlotFilter() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCurioEquip(CurioEquipEvent event) {
        if (!BackSlotAccess.SLOT.equals(event.getSlotContext().identifier())) {
            return;
        }
        if (allowListIsEmpty()) {
            if (!warned) {
                warned = true;
                LOGGER.warn("アイテムタグ curios:{} が空なので、背中に置けるものを絞らない"
                        + "（datapack back_slot_filter が置かれているか確かめること）",
                        BackSlotAccess.SLOT);
            }
            return;
        }
        event.setResult(event.getStack().is(ALLOWED) ? Event.Result.ALLOW : Event.Result.DENY);
    }

    /** タグが未読み込み・空のとき true。 */
    private static boolean allowListIsEmpty() {
        var tag = ForgeRegistries.ITEMS.tags();
        return tag == null || tag.getTag(ALLOWED).isEmpty();
    }
}
