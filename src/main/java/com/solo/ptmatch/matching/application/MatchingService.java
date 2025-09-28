package com.solo.ptmatch.matching.application;

import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MatchingService {

    public MatchingRequestCreateResponse requestMatching(MatchingRequestCreateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<MatchingSentSummaryResponse> getSentMatchings() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<MatchingReceivedSummaryResponse> getReceivedMatchings() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public MatchingRespondResponse respondMatching(Long matchingId, MatchingRespondRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public MatchingDetailResponse getMatching(Long matchingId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
