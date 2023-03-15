package plus.dragons.homingendereye.misc;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class EyeThrowCache {
    Queue<UUID> cache = new LinkedList<>();

    public void putThrowRecord(UUID uuid){
        cache.add(uuid);
    }

    @Nullable
    public UUID retrieveThrowerRecord(){
        return cache.poll();
    }

    public UUID peek(){
        return cache.peek();
    }

}
