package love.marblegate.homingendereye.misc;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderEyeDestroyState extends PersistentState {
    private final Gson gson;
    private final Type type;
    private final boolean shared;
    private int count;
    private Map<UUID,Integer> countMap;

    public EnderEyeDestroyState() {
        shared = !Configuration.getRealTimeConfig().INDIVIDUAL_MODE;
        count = 0;
        gson = new Gson();
        countMap = new HashMap<>();
        type = new TypeToken<HashMap<UUID,Integer>>() {}.getType();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("shared_destroy_count",count);
        nbt.putString("individual_destroy_count",gson.toJson(countMap,type));
        return nbt;
    }

    public static EnderEyeDestroyState get(World world){
        if (!(world instanceof ServerWorld)) {
            throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
        }

        ServerWorld serverWorld = world.getServer().getOverworld();
        PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
        return persistentStateManager.getOrCreate(EnderEyeDestroyState::load, EnderEyeDestroyState::new, "endereyedestroy");
    }

    public int getCount(@Nullable UUID uuid) {
        if(shared){
            return count;
        } else {
            return uuid == null?0:countMap.getOrDefault(uuid,0);
        }
    }

    public void setCount(@Nullable UUID uuid, int count) {
        count = Math.max(count, 0);
        if(shared) {
            this.count = count;
            setDirty(true);
        }
        else{
            if(uuid!=null){
                countMap.put(uuid,count);
                setDirty(true);
            }
        }
    }

    public void increaseCount(@Nullable UUID uuid){
        if(shared) {
            count += 1;
            setDirty(true);
        }
        else{
            if(uuid!=null){
                if(countMap.containsKey(uuid)){
                    countMap.put(uuid,countMap.get(uuid)+1);
                } else {
                    countMap.put(uuid,1);
                }
                setDirty(true);
            }
        }
    }

    public void decreaseCount(@Nullable UUID uuid){
        if(shared) {
            count = Math.max(0,count - 1);
            setDirty(true);
        }
        else{
            if(uuid!=null){
                countMap.put(uuid,Math.max(0,countMap.get(uuid)-1));
                setDirty(true);
            }
        }
    }

    public static EnderEyeDestroyState load(NbtCompound tag){
        EnderEyeDestroyState enderEyeDestroyState = new EnderEyeDestroyState();
        if(tag.contains("shared_destroy_count"))
            enderEyeDestroyState.count = tag.getInt("shared_destroy_count");
        if(tag.contains("individual_destroy_count"))
            enderEyeDestroyState.countMap = enderEyeDestroyState.gson.fromJson(tag.getString("individual_destroy_count"), enderEyeDestroyState.type);
        return enderEyeDestroyState;
    }
}
