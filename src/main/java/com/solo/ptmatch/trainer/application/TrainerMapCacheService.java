package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.cache.enums.CacheTarget;
import com.solo.ptmatch.common.cache.facade.CacheRouter;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.presentation.response.TrainerLatLon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class TrainerMapCacheService {

    private final CacheRouter cacheManagerFacade;

    public Map<Long, Long> getCountsOrLoad(
            SportType sportType,
            List<Long> cellIds,
            Function<List<Long>, Map<Long, Long>> loader
    ) {
        return getAllOrLoad(CacheTarget.TRAINER_COUNT, sportType, cellIds, loader, () -> 0L);
    }

    public Map<Long, List<TrainerLatLon>> getMarkersOrLoad(
            SportType sportType,
            List<Long> cellIds,
            Function<List<Long>, Map<Long, List<TrainerLatLon>>> loader
    ) {
        return getAllOrLoad(CacheTarget.TRAINER_MARKER, sportType, cellIds, loader, List::of);
    }

    private <V> Map<Long, V> getAllOrLoad(
            CacheTarget cacheDef,
            SportType sportType,
            List<Long> cellIds,
            Function<List<Long>, Map<Long, V>> loader,
            Supplier<V> emptyValue
    ) {
        if (cellIds == null || cellIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> idToKey = new HashMap<>(cellIds.size());
        List<String> keys = new ArrayList<>(cellIds.size());
        for (Long id : cellIds) {
            String key = cacheDef.buildKey(sportType, id);
            idToKey.put(id, key);
            keys.add(key);
        }

        Map<String, V> hits = cacheManagerFacade.getAll(cacheDef, keys);
        Map<Long, V> result = new HashMap<>(cellIds.size());
        List<Long> missIds = new ArrayList<>();
        for (Long id : cellIds) {
            String key = idToKey.get(id);
            if (hits.containsKey(key)) {
                result.put(id, hits.get(key));
            } else {
                missIds.add(id);
            }
        }

        if (!missIds.isEmpty()) {
            Map<Long, V> loaded = loader.apply(missIds);

            Map<String, V> toCache = new HashMap<>(missIds.size());
            for (Long missId : missIds) {
                V value = loaded.get(missId);
                if (value == null) {
                    value = emptyValue.get();
                }
                result.put(missId, value);
                toCache.put(idToKey.get(missId), value);
            }
            cacheManagerFacade.putAll(cacheDef, toCache);
        }

        return result;
    }
}
