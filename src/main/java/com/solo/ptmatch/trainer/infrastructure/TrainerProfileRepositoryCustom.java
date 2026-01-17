package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.common.util.S2CellRange;
import java.util.List;

public interface TrainerProfileRepositoryCustom {

    List<LatLngProjection> findLatLngByS2CellRangesCriteria(List<S2CellRange> ranges);
}
