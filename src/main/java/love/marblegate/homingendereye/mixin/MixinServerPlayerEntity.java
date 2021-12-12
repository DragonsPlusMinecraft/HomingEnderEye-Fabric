package love.marblegate.homingendereye.mixin;

import com.mojang.authlib.GameProfile;
import love.marblegate.homingendereye.misc.Configuration;
import love.marblegate.homingendereye.misc.EnderEyeDestroyState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow public abstract ServerWorld getWorld();

    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        if(world!=null && world.getRegistryKey().equals(World.OVERWORLD)){
            if(world.getTimeOfDay() % Configuration.getRealTimeConfig().SCANNING_RATE == 0){
                EnderEyeDestroyState data = EnderEyeDestroyState.get(getWorld());
                if(data.getCount(getUuid()) > 0){
                    BlockPos center = getBlockPos();
                    int offSet = Configuration.getRealTimeConfig().SCANNING_RADIUS;
                    for(BlockPos blockpos : BlockPos.iterate(center.add(offSet,offSet,offSet),center.add(-offSet,-offSet,-offSet))){
                        BlockState blockstate = world.getBlockState(blockpos);

                        // Scanning for Frame and Filling
                        if (blockstate.isOf(Blocks.END_PORTAL_FRAME) && !blockstate.get(EndPortalFrameBlock.EYE)){
                            data.decreaseCount(getUuid());
                            BlockState newBlockState = blockstate.with(EndPortalFrameBlock.EYE, Boolean.TRUE);
                            world.setBlockState(blockpos, newBlockState, 2);
                            world.updateComparators(blockpos, Blocks.END_PORTAL_FRAME);

                            // Check if portal is qualified or not
                            BlockPattern.Result result = EndPortalFrameBlock.getCompletedFramePattern().searchAround(world, blockpos);
                            if (result != null) {
                                BlockPos blockPos2 = result.getFrontTopLeft().add(-3, 0, -3);
                                for(int i = 0; i < 3; ++i) {
                                    for(int j = 0; j < 3; ++j) {
                                        world.setBlockState(blockPos2.add(i, 0, j), Blocks.END_PORTAL.getDefaultState(), 2);
                                    }
                                }
                                break;
                            }

                        }

                        // Check for remaining ender eye
                        if (data.getCount(getUuid()) == 0)
                            break;
                    }
                }
            }
        }
    }
}
