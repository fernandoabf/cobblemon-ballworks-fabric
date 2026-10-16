package com.xaiver.cobbleballworks.item

import com.xaiver.cobbleballworks.block.BallAnvilBlock
import com.xaiver.cobbleballworks.block.entity.BallAnvilBlockEntity
import com.xaiver.cobbleballworks.content.AnvilRules
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.registry.ModTags
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.Tool
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * Forge hammer. Hammering is holding left click on the anvil: the hammer mines the anvil at
 * [strength] speed (its TOOL component), and every completed "break" is one strike instead of
 * breaking the block, as in Pixelmon.
 *
 * Like Pixelmon's ItemTool-based hammers it also works as a weak weapon (2 durability per hit) and
 * takes 1 durability per block mined elsewhere; the anvil itself only wears it when a strike
 * advances a stage.
 */
class HammerItem(val material: HammerMaterial) : Item(properties(material)) {

    val strength: Int get() = material.strength

    /**
     * Called by the client before it breaks a block locally and by the server before it breaks it
     * for real (MultiPlayerGameMode/ServerPlayerGameMode.destroyBlock). On the anvil the "break"
     * becomes a strike on the server and the anvil is never broken, as Pixelmon's
     * ItemHammer.onBlockStartBreak did. Mining progress, cracks and hit sounds stay vanilla.
     */
    override fun canAttackBlock(state: BlockState, level: Level, pos: BlockPos, player: Player): Boolean {
        if (state.block !is BallAnvilBlock) return true
        if (player is ServerPlayer) {
            val anvil = level.getBlockEntity(pos) as? BallAnvilBlockEntity
            if (anvil != null) AnvilStrikes.strike(player, anvil, state, pos)
        }
        return false
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean = true

    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND)
    }

    override fun getEnchantmentValue(): Int = material.enchantability

    override fun isValidRepairItem(stack: ItemStack, repairCandidate: ItemStack): Boolean =
        material.isRepairMaterial(repairCandidate)

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        flag: TooltipFlag,
    ) {
        tooltip.add(Component.translatable("item.cobbleballworks.hammer.strength", strength).withStyle(ChatFormatting.GRAY))
        tooltip.add(
            Component.translatable("item.cobbleballworks.hammer.strikes", AnvilRules.strikesPerPiece(strength))
                .withStyle(ChatFormatting.GRAY),
        )
    }

    companion object {
        /** Attack speed modifier of a pickaxe (attack speed 1.2). */
        private const val ATTACK_SPEED = -2.8

        private fun properties(material: HammerMaterial): Properties = Properties()
            .durability(material.durability)
            .apply { if (material.fireResistant) fireResistant() }
            .component(
                DataComponents.TOOL,
                Tool(listOf(Tool.Rule.overrideSpeed(ModTags.Blocks.HAMMERABLE, material.strength.toFloat())), 1.0f, 1),
            )
            .attributes(
                ItemAttributeModifiers.builder()
                    .add(
                        Attributes.ATTACK_DAMAGE,
                        AttributeModifier(BASE_ATTACK_DAMAGE_ID, material.attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND,
                    )
                    .add(
                        Attributes.ATTACK_SPEED,
                        AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND,
                    )
                    .build(),
            )
    }
}
