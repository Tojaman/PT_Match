package com.solo.ptmatch.matching.application;

import com.solo.ptmatch.matching.presentation.request.ReservationCreateRequest;
import com.solo.ptmatch.matching.presentation.response.ReservationCancelResponse;
import com.solo.ptmatch.matching.presentation.response.ReservationDetailResponse;
import com.solo.ptmatch.matching.presentation.response.ReservationSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationService {

    public ReservationDetailResponse createReservation(ReservationCreateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<ReservationSummaryResponse> getMyReservations(String status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ReservationDetailResponse getReservationDetail(Long reservationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ReservationCancelResponse cancelReservation(Long reservationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
