package com.xaiver.cobbleballworks.item

import com.xaiver.cobbleballworks.content.AnvilRules
import com.xaiver.cobbleballworks.content.HammerMaterial
import com.xaiver.cobbleballworks.registry.ModTags
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.Tool

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
