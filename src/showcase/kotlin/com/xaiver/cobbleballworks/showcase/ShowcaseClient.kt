package com.xaiver.cobbleballworks.showcase

import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import com.xaiver.cobbleballworks.content.ForgeBall
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.registry.ModBlocks
import com.xaiver.cobbleballworks.registry.ModCreativeTab
import com.xaiver.cobbleballworks.registry.ModItems
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Difficulty
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.WorldDataConfiguration
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.levelgen.presets.WorldPresets
import org.slf4j.LoggerFactory

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

    /** A camera position for a screenshot: eye position and the point it looks at. */
    private data class View(val name: String, val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float)

    /** Ground level of the default flat world (grass top). */
    private const val Y = -60

    /** Eye heights stay at least 1.7 above the ground, or the player is pushed into a crawl. */
    private val views = listOf(
        View("anvils_overview", 5.0, Y + 2.6, -2.2, 0f, 28f),
        View("anvils_stages_closeup", 2.0, Y + 1.8, 1.25, 0f, 52f),
        View("anvils_lids_closeup", 6.0, Y + 1.8, 1.25, 0f, 52f),
        View("anvils_discs_row", 5.0, Y + 1.9, 2.4, 0f, 45f),
    )

    private enum class Phase { WAIT_TITLE, LOADING, SETTLING, SHOOTING, INVENTORY, DONE }

    private var phase = Phase.WAIT_TITLE
    private var timer = 0
    private var viewIndex = 0

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
                goTo(mc, views[0])
                timer = 60
                phase = Phase.SHOOTING
            }
            Phase.SHOOTING -> if (--timer <= 0) {
                shoot(mc, views[viewIndex].name)
                viewIndex++
                if (viewIndex < views.size) {
                    goTo(mc, views[viewIndex])
                    timer = 60
                } else {
                    mc.options.hideGui = false
                    openBallworksTab(mc)
                    timer = 40
                    phase = Phase.INVENTORY
                }
            }
            Phase.INVENTORY -> if (--timer <= 0) {
                shoot(mc, "inventory")
                phase = Phase.DONE
                LOGGER.info("[showcase] done, quitting")
                mc.stop()
            }
            Phase.DONE -> Unit
        }
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
        player.abilities.flying = true
        player.onUpdateAbilities()

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
        val discs = listOf(
            ForgeBall.GREAT, ForgeBall.ULTRA, ForgeBall.MASTER, ForgeBall.BEAST, ForgeBall.DREAM,
            ForgeBall.ANCIENT_JET, ForgeBall.LEVEL, ForgeBall.SAFARI, ForgeBall.LUXURY, ForgeBall.PREMIER,
        )
        discs.forEachIndexed { i, ball -> anvil(level, player, BlockPos(i, Y, 4), ItemStack(ModItems.DISCS.getValue(ball)), 0) }

        player.inventory.clearContent()
        val showcaseItems = ModItems.HAMMERS.values.take(9) + ModItems.COOKED_APRICORNS.values + listOf(ModItems.IRON_DISC, ModItems.IRON_BASE) +
            listOf(ForgeBall.POKE, ForgeBall.GREAT, ForgeBall.ULTRA, ForgeBall.MASTER, ForgeBall.ANCIENT_POKE, ForgeBall.BEAST, ForgeBall.DREAM, ForgeBall.LUXURY, ForgeBall.PREMIER)
                .flatMap { listOf(ModItems.DISCS.getValue(it), ModItems.LIDS.getValue(it)) }
        showcaseItems.take(36).forEach { player.inventory.add(ItemStack(it)) }
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

    private fun goTo(mc: Minecraft, view: View) {
        val server = mc.singleplayerServer ?: return
        server.execute {
            val player = server.playerList.players.first()
            player.teleportTo(server.overworld(), view.x, view.y - player.eyeHeight, view.z, view.yaw, view.pitch)
        }
    }

    /** Opens the creative inventory on the mod's tab (the screen keeps the last tab in a static field). */
    private fun openBallworksTab(mc: Minecraft) {
        val field = CreativeModeInventoryScreen::class.java.getDeclaredField("selectedTab")
        field.isAccessible = true
        field.set(null, ModCreativeTab.BALLWORKS)
        val player = mc.player!!
        mc.setScreen(CreativeModeInventoryScreen(player, player.connection.enabledFeatures(), true))
    }

    private fun shoot(mc: Minecraft, name: String) {
        Screenshot.grab(mc.gameDirectory, "$name.png", mc.mainRenderTarget) { message -> LOGGER.info("[showcase] {}: {}", name, message.string) }
    }
}
