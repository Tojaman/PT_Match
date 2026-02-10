package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.common.util.S2CellRange;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.domain.SportType;
import java.util.List;
import java.util.Map;

public interface TrainerProfileRepositoryCustom {

    Map<Long, CellStat> countTrainersByCellRanges(SportType sportType, List<S2CellRange> ranges);

    List<TrainerProfile> findTrainersByCellRanges(SportType sportType, List<S2CellRange> ranges);

    List<TrainerProfile> findTrainersByClusterCellCursor(SportType sportType, long clusterCellId, long cursor, int limit);
}
