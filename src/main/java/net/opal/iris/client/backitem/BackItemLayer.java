package net.opal.iris.client.backitem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemStack;

public final class BackItemLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public BackItemLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer renderedPlayer, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        var player = Minecraft.getInstance().player;
        if (player == null || renderedPlayer != player) return;

        ItemStack stack = BackItemManager.getBackItem(player.getUUID());
        if (stack.isEmpty() || ItemStack.isSameItem(player.getMainHandItem(), stack)
                || ItemStack.isSameItem(player.getOffhandItem(), stack)) return;

        // Attach the item to the body model so the back item inherits the
        // crouching torso rotation instead of remaining upright in world space.
        getParentModel().body.translateAndRotate(poseStack);
        BackItemRenderer.render(stack, poseStack, buffer, packedLight, renderedPlayer.isCrouching());
    }
}
