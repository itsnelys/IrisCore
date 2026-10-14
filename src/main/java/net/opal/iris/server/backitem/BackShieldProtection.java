package net.opal.iris.server.backitem;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.opal.iris.client.backitem.BackItemWhitelist;
import net.opal.iris.network.BackItemDetachPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side rear-only shield blocking for an eligible shield displayed as a BackItem. */
public final class BackShieldProtection {
    private static final double REAR_BLOCK_DOT_PRODUCT = -0.5D;
    /** Back protection is passive, so each armor-timed block costs three durability points. */
    private static final int BACK_SHIELD_DURABILITY_COST = 3;
    private static final Map<UUID, ItemStack> LOCKED_SHIELDS = new HashMap<>();

    private BackShieldProtection() {}

    public static void updateLockedShield(ServerPlayer player, ItemStack requestedStack) {
        if (!(requestedStack.getItem() instanceof ShieldItem)
                || !BackItemWhitelist.isWhitelisted(requestedStack.getItem())
                || findInventoryStack(player, requestedStack).isEmpty()) {
            LOCKED_SHIELDS.remove(player.getUUID());
            return;
        }
        LOCKED_SHIELDS.put(player.getUUID(), requestedStack.copy());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack lockedShield = LOCKED_SHIELDS.get(player.getUUID());
        if (lockedShield == null) return;

        ItemStack shield = findInventoryStack(player, lockedShield);
        if (shield.isEmpty()) {
            LOCKED_SHIELDS.remove(player.getUUID());
            return;
        }
        if (ItemStack.isSameItemSameComponents(player.getMainHandItem(), lockedShield)
                || ItemStack.isSameItemSameComponents(player.getOffhandItem(), lockedShield)) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) attacker = event.getSource().getDirectEntity();
        if (attacker == null || attacker == player || !isBehind(player, attacker)) return;

        // Match vanilla hurt timing: while the player is in their normal damage
        // immunity window, rear hits are still stopped but do not cost durability.
        event.setCanceled(true);
        if (player.invulnerableTime > 0) return;

        shield.hurtAndBreak(BACK_SHIELD_DURABILITY_COST, player, net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        player.invulnerableTime = player.invulnerableDuration;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (shield.isEmpty()) {
            LOCKED_SHIELDS.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new BackItemDetachPayload());
        } else {
            // Damage changes the stack components. Keep the server-side reference
            // in sync so the shield can block every later rear attack too.
            LOCKED_SHIELDS.put(player.getUUID(), shield.copy());
        }
    }

    private static boolean isBehind(ServerPlayer player, Entity attacker) {
        Vec3 offset = attacker.position().subtract(player.position());
        if (offset.horizontalDistanceSqr() < 0.0001D) return false;
        Vec3 attackerDirection = new Vec3(offset.x, 0.0D, offset.z).normalize();
        Vec3 facing = player.getLookAngle();
        Vec3 horizontalFacing = new Vec3(facing.x, 0.0D, facing.z).normalize();
        return horizontalFacing.dot(attackerDirection) <= REAR_BLOCK_DOT_PRODUCT;
    }

    private static ItemStack findInventoryStack(ServerPlayer player, ItemStack expected) {
        // A lock is created while the shield is still held. Include both hands
        // so the server accepts it before the player switches to another item.
        if (sameStack(player.getMainHandItem(), expected)) return player.getMainHandItem();
        if (sameStack(player.getOffhandItem(), expected)) return player.getOffhandItem();
        for (ItemStack candidate : player.getInventory().items) {
            if (sameStack(candidate, expected)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first != null && second != null && ItemStack.isSameItemSameComponents(first, second);
    }
}
