package net.opal.iris.client.backitem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class BackItemRenderer {
    private BackItemRenderer() {}

    public static void render(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                              boolean crouching) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.42F, 0.18F);
        if (crouching) {
            // The body bends forward while sneaking. Lift the item and move it
            // farther behind the torso so it does not clip through armor.
            poseStack.translate(0.0F, 0.08F, 0.10F);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}
