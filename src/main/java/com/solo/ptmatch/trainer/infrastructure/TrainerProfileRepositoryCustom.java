package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.common.util.S2CellRange;
import java.util.List;
import java.util.Map;

public interface TrainerProfileRepositoryCustom {

    Map<Long, Long> countTrainersByCellRanges(List<S2CellRange> ranges);
}
