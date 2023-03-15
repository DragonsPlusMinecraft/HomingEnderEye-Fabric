package plus.dragons.homingendereye;

import plus.dragons.homingendereye.misc.Configuration;
import plus.dragons.homingendereye.misc.EyeThrowCache;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class HomingEnderEye implements ModInitializer {

    public static EyeThrowCache EYE_THROW_CACHE;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register((server -> {
            if(HomingEnderEye.EYE_THROW_CACHE == null){
                HomingEnderEye.EYE_THROW_CACHE = new EyeThrowCache();
            }
        }));

        AutoConfig.register(Configuration.class, JanksonConfigSerializer::new);
    }
}
