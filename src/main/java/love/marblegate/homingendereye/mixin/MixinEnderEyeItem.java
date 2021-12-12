package love.marblegate.homingendereye.mixin;

import love.marblegate.homingendereye.HomingEnderEye;
import love.marblegate.homingendereye.misc.Configuration;
import love.marblegate.homingendereye.misc.EnderEyeDestroyState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(EnderEyeItem.class)
public class MixinEnderEyeItem {

    @Inject(method ="use",
            at= @At(value ="INVOKE", target = "Lnet/minecraft/entity/EyeOfEnderEntity;initTargetPos(Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.AFTER))
    public void captureThrowSource(World world, PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
        // Only Signal Cache to Remember Who just throw an ender eye
        // Only works if the mode is running on Individual Mode
        if(world instanceof ServerWorld && world.getRegistryKey().equals(World.OVERWORLD) ){
            if(Configuration.getRealTimeConfig().INDIVIDUAL_MODE){
                HomingEnderEye.EYE_THROW_CACHE.putThrowRecord(playerEntity.getUuid());
            }
        }

    }

    @ModifyArg(method="use",
            at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), index = 0)
    public Entity captureEnderEyeBreak(Entity entity){
        // Pre Handling Ender Eye Break
        if(entity.world instanceof ServerWorld && entity.world.getRegistryKey().equals(World.OVERWORLD)){
            if(!((AccessorEyeOfEnderEntity) (Object) (EyeOfEnderEntity) entity).getDropsItem()){
                EnderEyeDestroyState data = EnderEyeDestroyState.get(entity.world);
                if(Configuration.getRealTimeConfig().INDIVIDUAL_MODE){
                    UUID throwerUUID = HomingEnderEye.EYE_THROW_CACHE.peek();
                    if(throwerUUID!=null){
                        data.increaseCount(throwerUUID);
                    }
                } else {
                    data.increaseCount(null);
                }
            }
            // Once a eye is thrown, remove a record
            if(Configuration.getRealTimeConfig().INDIVIDUAL_MODE){
                HomingEnderEye.EYE_THROW_CACHE.retrieveThrowerRecord();
            }
        }
        return entity;
    }

}
