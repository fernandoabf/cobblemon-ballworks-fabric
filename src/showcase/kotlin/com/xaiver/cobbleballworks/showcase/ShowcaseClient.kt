package com.xaiver.cobbleballworks.showcase

import com.xaiver.cobbleballworks.block.MechanicalAnvilBlock
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import com.xaiver.cobbleballworks.block.entity.MechanicalAnvilBlockEntity
import com.xaiver.cobbleballworks.client.compat.emi.BallworksEmiPlugin
import com.xaiver.cobbleballworks.content.ForgeBall
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModCreativeTab
import com.xaiver.cobbleballworks.registry.ModItems
import dev.emi.emi.api.EmiApi
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiStack
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Difficulty
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.WorldDataConfiguration
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.levelgen.presets.WorldPresets
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier

/**
 * Dev-only visual check (`./gradlew runShowcase`, under Xvfb on a headless machine): creates a
 * flat creative world, builds scenes, takes screenshots into build/showcase/screenshots and quits.
 * Only active with -Dcobbleballworks.showcase=true (set by the run config).
 */
class ShowcaseClient : ClientModInitializer {
    override fun onInitializeClient() {
        if (!java.lang.Boolean.getBoolean("cobbleballworks.showcase")) return
        ClientTickEvents.END_CLIENT_TICK.register(Showcase::tick)
    }
}

private object Showcase {
    private val LOGGER = LoggerFactory.getLogger("cobbleballworks-showcase")

    /** Ground level of the default flat world (top of the grass). */
    private const val Y = -60

    /** One screenshot: what to set up on the client first, then how long to wait before shooting. */
    private class Shot(val name: String, val wait: Int = 60, val setup: (Minecraft) -> Unit)

    /** Eye heights stay at least 1.7 above the ground, or the player is pushed into a crawl. */
    private val shots = listOf(
        Shot("anvils_overview") { view(it, 5.0, Y + 2.6, -2.2, 0f, 28f) },
        Shot("anvils_stages_closeup") { view(it, 2.0, Y + 1.8, 1.25, 0f, 52f) },
        Shot("anvils_lids_closeup") { view(it, 6.0, Y + 1.8, 1.25, 0f, 52f) },
        Shot("anvils_discs_row") { view(it, 5.0, Y + 1.9, 2.4, 0f, 45f) },
        Shot("anvils_iron_closeup") { view(it, 8.5, Y + 1.8, 1.25, 0f, 52f) },
        Shot("anvil_master_lid_macro") { view(it, 6.5, Y + 1.7, 1.75, 0f, 62f) },
        Shot("mechanical_anvils") { view(it, 2.5, Y + 2.2, 4.6, 0f, 35f) },
        Shot("mechanical_anvil_closeup") { view(it, 2.5, Y + 1.9, 6.4, 0f, 40f) },
        // The press moves once a second: three shots a few ticks apart catch it at different heights.
        Shot("mechanical_press_1", wait = 20) { view(it, 2.5, Y + 1.75, 7.1, 0f, 32f) },
        Shot("mechanical_press_2", wait = 5) { view(it, 2.5, Y + 1.75, 7.1, 0f, 32f) },
        Shot("mechanical_press_3", wait = 5) { view(it, 2.5, Y + 1.75, 7.1, 0f, 32f) },
        Shot("mechanical_anvil_gui", wait = 40) { openMechanicalAnvil(it) },
        Shot("creative_tab", wait = 40) { openBallworksTab(it) },
        Shot("emi_poke_lid", wait = 100) { emi(it, EmiStack.of(ModItems.LIDS.getValue(ForgeBall.POKE))) },
        Shot("emi_ultra_disc", wait = 40) { emi(it, EmiStack.of(ModItems.DISCS.getValue(ForgeBall.ULTRA))) },
        Shot("emi_ultra_ball", wait = 40) { emi(it, EmiStack.of(BuiltInRegistries.ITEM.get(ForgeBall.ULTRA.ballId))) },
        Shot("emi_master_lid", wait = 40) { emi(it, EmiStack.of(ModItems.LIDS.getValue(ForgeBall.MASTER))) },
        Shot("emi_category_ball_anvil", wait = 40) { emiCategory(it, BallworksEmiPlugin.BALL_ANVIL) },
        Shot("emi_category_mechanical_anvil", wait = 40) { emiCategory(it, BallworksEmiPlugin.MECHANICAL_ANVIL) },
    )

    private enum class Phase { WAIT_TITLE, LOADING, SETTLING, SHOOTING, DONE }

    private var phase = Phase.WAIT_TITLE
    private var timer = 0
    private var shot = 0

    fun tick(mc: Minecraft) {
        when (phase) {
            Phase.WAIT_TITLE -> if (mc.screen is TitleScreen) {
                createWorld(mc)
                phase = Phase.LOADING
            }
            Phase.LOADING -> if (mc.player != null && mc.level != null && mc.screen == null && mc.singleplayerServer != null) {
                mc.options.hideGui = true
                val server = mc.singleplayerServer!!
                server.execute { buildScene(server.overworld(), server.playerList.players.first()) }
                timer = 200
                phase = Phase.SETTLING
            }
            Phase.SETTLING -> if (--timer <= 0) {
                startShot(mc)
                phase = Phase.SHOOTING
            }
            Phase.SHOOTING -> if (--timer <= 0) {
                shoot(mc, shots[shot].name)
                shot++
                if (shot < shots.size) {
                    startShot(mc)
                } else {
                    phase = Phase.DONE
                    LOGGER.info("[showcase] done, quitting")
                    mc.stop()
                }
            }
            Phase.DONE -> Unit
        }
    }

    private fun startShot(mc: Minecraft) {
        shots[shot].setup(mc)
        timer = shots[shot].wait
    }

    private fun createWorld(mc: Minecraft) {
        LOGGER.info("[showcase] creating a flat world")
        val rules = GameRules()
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null)
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null)
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null)
        val settings = LevelSettings("showcase", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT)
        mc.createWorldOpenFlows().createFreshLevel(
            "showcase",
            settings,
            WorldOptions(0L, false, false),
            { registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions() },
            TitleScreen(),
        )
    }

    /** Runs on the server thread. */
    private fun buildScene(level: ServerLevel, player: ServerPlayer) {
        level.setDayTime(6000)
        val server = level.server
        for (command in listOf("gamerule doPokemonSpawning false", "kill @e[type=!minecraft:player]")) {
            server.commands.performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command)
        }
        player.abilities.flying = true
        player.onUpdateAbilities()

        // Row 1: a Poké Ball disc at each stage, finished lids, the iron disc and base.
        val stages = listOf(
            ModItems.DISCS.getValue(ForgeBall.POKE) to 0,
            ModItems.DISCS.getValue(ForgeBall.POKE) to 1,
            ModItems.DISCS.getValue(ForgeBall.POKE) to 2,
            ModItems.LIDS.getValue(ForgeBall.POKE) to 0,
            ModItems.LIDS.getValue(ForgeBall.GREAT) to 0,
            ModItems.LIDS.getValue(ForgeBall.ULTRA) to 0,
            ModItems.LIDS.getValue(ForgeBall.MASTER) to 0,
            ModItems.LIDS.getValue(ForgeBall.ANCIENT_GREAT) to 0,
            ModItems.IRON_DISC to 1,
            ModItems.IRON_BASE to 0,
        )
        stages.forEachIndexed { i, (item, stage) -> anvil(level, player, BlockPos(i, Y, 2), ItemStack(item), stage) }
        // Row 2: discs of different balls.
        val discs = listOf(
            ForgeBall.GREAT, ForgeBall.ULTRA, ForgeBall.MASTER, ForgeBall.BEAST, ForgeBall.DREAM,
            ForgeBall.ANCIENT_JET, ForgeBall.LEVEL, ForgeBall.SAFARI, ForgeBall.LUXURY, ForgeBall.PREMIER,
        )
        discs.forEachIndexed { i, ball -> anvil(level, player, BlockPos(i, Y, 4), ItemStack(ModItems.DISCS.getValue(ball)), 0) }
        // Row 3: Mechanical Anvils, idle and working.
        mechanicalAnvil(level, BlockPos(0, Y, 8), ItemStack.EMPTY, ItemStack.EMPTY)
        mechanicalAnvil(level, BlockPos(2, Y, 8), ItemStack(ModItems.DISCS.getValue(ForgeBall.ULTRA), 16), ItemStack(Items.COAL, 4))
        mechanicalAnvil(level, BlockPos(4, Y, 8), ItemStack(ModItems.DISCS.getValue(ForgeBall.POKE), 16), ItemStack(Items.CHARCOAL, 2))

        player.inventory.clearContent()
        LOGGER.info("[showcase] scene built")
    }

    private fun anvil(level: ServerLevel, player: ServerPlayer, pos: BlockPos, piece: ItemStack, stage: Int) {
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState())
        level.setBlockAndUpdate(pos, ModBlocks.BALL_ANVIL.defaultBlockState())
        val anvil = level.getBlockEntity(pos) as BallAnvilBlockEntity
        anvil.place(piece)
        val hammer = ItemStack(ModItems.hammer(HammerMaterial.DIAMOND))
        repeat(stage) { anvil.strike(player, hammer, 5) }
    }

    private fun mechanicalAnvil(level: ServerLevel, pos: BlockPos, input: ItemStack, fuel: ItemStack) {
        // Facing north, towards the cameras.
        level.setBlockAndUpdate(pos, ModBlocks.MECHANICAL_ANVIL.defaultBlockState().setValue(MechanicalAnvilBlock.FACING, Direction.NORTH))
        val anvil = level.getBlockEntity(pos) as MechanicalAnvilBlockEntity
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_INPUT, input)
        anvil.setItem(MechanicalAnvilBlockEntity.SLOT_FUEL, fuel)
    }

    private fun view(mc: Minecraft, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        mc.setScreen(null)
        mc.options.hideGui = true
        val server = mc.singleplayerServer ?: return
        server.execute {
            val player = server.playerList.players.first()
            player.teleportTo(server.overworld(), x, y - player.eyeHeight, z, yaw, pitch)
        }
    }

    private fun openMechanicalAnvil(mc: Minecraft) {
        mc.options.hideGui = false
        val server = mc.singleplayerServer ?: return
        server.execute {
            val player = server.playerList.players.first()
            val anvil = server.overworld().getBlockEntity(BlockPos(2, Y, 8)) as? MechanicalAnvilBlockEntity
            if (anvil != null) player.openMenu(anvil)
        }
    }

    /**
     * Opens the creative inventory on the mod's tab. The screen keeps the last tab in its only static
     * CreativeModeTab field, found by type so it also works with intermediary names in production.
     */
    private fun openBallworksTab(mc: Minecraft) {
        mc.options.hideGui = false
        mc.player?.closeContainer()
        val field = CreativeModeInventoryScreen::class.java.declaredFields.single {
            Modifier.isStatic(it.modifiers) && it.type == CreativeModeTab::class.java
        }
        field.isAccessible = true
        field.set(null, ModCreativeTab.BALLWORKS)
        val player = mc.player!!
        mc.setScreen(CreativeModeInventoryScreen(player, player.connection.enabledFeatures(), true))
    }

    /** EMI's recipe screen for [stack] (EMI runs in dev through modLocalRuntime). */
    private fun emi(mc: Minecraft, stack: EmiStack) {
        mc.options.hideGui = false
        mc.setScreen(null)
        EmiApi.displayRecipes(stack)
    }

    /** Every recipe of one of our EMI categories. */
    private fun emiCategory(mc: Minecraft, category: EmiRecipeCategory) {
        mc.options.hideGui = false
        mc.setScreen(null)
        EmiApi.displayRecipeCategory(category)
    }

    private fun shoot(mc: Minecraft, name: String) {
        Screenshot.grab(mc.gameDirectory, "$name.png", mc.mainRenderTarget) { message -> LOGGER.info("[showcase] {}: {}", name, message.string) }
    }
}
