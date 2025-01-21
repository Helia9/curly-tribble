package mod.epimap

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class keybindListener {
    private val openCustomScreenKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.setting-up.open_custom_screen",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.setting-up"
        )
    )

    private val closeCustomScreenKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.setting-up.close_custom_screen",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            "category.setting-up"
        )
    )

    private val moveOffsetUpKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.setting-up.move_offset_up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "category.setting-up"
        )
    )
    private val moveOffsetDownKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.setting-up.move_offset_down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "category.setting-up"
        )
    )

    private val moveOffsetLeftKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.setting-up.move_offset_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            "category.setting-up"
        )
    )

    private val moveOffsetRightKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.setting-up.move_offset_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT,
            "category.setting-up"
        )
    )

    init {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            val screen = client.currentScreen

            while (openCustomScreenKey.wasPressed()) {
                client?.setScreen(CustomScreen(Text.of("Custom Screen")))

            }
            if (closeCustomScreenKey.wasPressed()) {
                client?.setScreen(null)
            }
        })
    }
}