package net.erutobusiness.shulkeronback.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {

    /**
     * 既定は B。
     *
     * <p>⚠ B はもともと Deeper and Darker のソウルエリトラ加速が使っていた席で、
     * そちらをマウス右へ移して空けた（部のキー方針 selection/keybinding-policy.md）。
     * 他の構成で使うときは衝突しうるので、必ず設定画面で確認できるようにしてある。
     *
     * <p>IN_GAME に限定しているので、画面を開いている間は反応しない
     * （チャットや別のGUIで B を打っても発火しない）。
     */
    public static final KeyMapping OPEN_BACK = new KeyMapping(
            "key.shulker_on_back.open",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.shulker_on_back");

    private Keybinds() {
    }
}
