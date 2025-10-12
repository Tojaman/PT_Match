import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 실행 옵션
export const options = {
  scenarios: {
    // 시나리오 이름 (임의 지정 가능)
    simultaneous_requests: {
      executor: 'per-vu-iterations', // 각 가상 유저가 지정된 횟수만큼 반복 실행
      vus: 10,          // 10명의 가상 유저
      iterations: 1,    // 각 유저가 1번만 실행
      maxDuration: '10s', // 타임아웃
    },
  },
};

// ====================================================================
// 테스트 전 필수 수정 항목
// ====================================================================

// Docker 컨테이너에서 Host PC의 애플리케이션을 타겟으로 할 때 사용하는 특수 주소입니다.
const API_BASE_URL = 'http://host.docker.internal:8080';

const TRAINER_PROFILE_ID = 1;
const PRODUCT_ID = 1;
const CONCURRENT_SCHEDULE_ID = 1; // 모든 사용자가 동시에 요청할 스케줄 ID

// ====================================================================

// k6 테스트의 메인 함수
export default function () {

  const userEmail = `user${String(__VU).padStart(6, '0')}@example.com`;
  const url = `${API_BASE_URL}/api/matchings/test/${userEmail}`;
  const headers = {
    'Content-Type': 'application/json',
  };

  // 요청 본문(Payload) 구성
  const payload = JSON.stringify({
    trainerProfileId: TRAINER_PROFILE_ID,
    productId: PRODUCT_ID,
    availableScheduleIds: [CONCURRENT_SCHEDULE_ID],
    // 인증 정보가 없으므로, 각 사용자를 식별할 수 있는 정보를 userInfo에 담아 보냅니다.
    userInfo: {
      name: `User ${String(__VU).padStart(6, '0')}`,
      email: userEmail,
      phoneNumber: '010-1234-5678',
    },
    message: 'PT 신청합니다!',
  });

  // HTTP POST 요청 실행
  const res = http.post(url, payload, { headers: headers });

  // 서버의 실제 응답 코드와 내용을 로그로 출력
  console.log(`[VU=${__VU}] Response: ${res.status} Body: ${res.body}`);

  // 응답 결과 확인 (Check)
  check(res, {
    'SUCCESS - is status 201 (Created)': (r) => r.status === 201,
    'FAIL - is status 409 (Conflict)': (r) => r.status === 409, // 서버가 409를 응답한다고 가정
  });
}
