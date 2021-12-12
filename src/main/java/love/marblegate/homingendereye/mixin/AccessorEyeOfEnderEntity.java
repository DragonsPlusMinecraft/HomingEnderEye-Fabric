package love.marblegate.homingendereye.mixin;

import net.minecraft.entity.EyeOfEnderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EyeOfEnderEntity.class)
public interface AccessorEyeOfEnderEntity {
    @Accessor
    boolean getDropsItem();
}
