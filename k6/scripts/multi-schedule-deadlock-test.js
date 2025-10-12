import http from 'k6/http';
import { check } from 'k6';

// 테스트 실행 옵션
export const options = {
  scenarios: {
    // 시나리오 이름 (임의 지정 가능)
    multi_schedule_deadlock_test: {
      executor: 'per-vu-iterations', // 각 가상 유저가 지정된 횟수만큼 반복 실행
      vus: 10,          // 10명의 가상 유저 (짝수 권장)
      iterations: 1,    // 각 유저가 1번만 실행
      maxDuration: '20s', // 타임아웃
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

// 중요: 데드락을 유발할 두 개의 스케줄 ID입니다.
// 테스트 전에 DB에 해당 ID를 가진 AvailableSchedule 레코드가 있는지 확인해야 합니다.
const SCHEDULE_ID_1 = 1;
const SCHEDULE_ID_2 = 2;

// ====================================================================

// k6 테스트의 메인 함수
export default function () {

  // 가상 유저(VU) 번호에 따라 스케줄 ID 요청 순서를 다르게 하여 데드락 유발
  let scheduleIdsInOrder;
  if (__VU % 2 === 0) {
    // 짝수 VU는 ID를 오름차순으로 요청
    scheduleIdsInOrder = [SCHEDULE_ID_1, SCHEDULE_ID_2];
  } else {
    // 홀수 VU는 ID를 내림차순으로 요청
    scheduleIdsInOrder = [SCHEDULE_ID_2, SCHEDULE_ID_1];
  }

  const userEmail = `user${String(__VU).padStart(6, '0')}@example.com`;
  const url = `${API_BASE_URL}/api/matchings/test/${userEmail}`;
  const headers = {
    'Content-Type': 'application/json',
  };

  // 요청 본문(Payload) 구성
  const payload = JSON.stringify({
    trainerProfileId: TRAINER_PROFILE_ID,
    productId: PRODUCT_ID,
    availableScheduleIds: scheduleIdsInOrder,
    userInfo: {
      name: `User Multi ${String(__VU).padStart(6, '0')}`,
      email: userEmail,
      phoneNumber: '010-1234-5678',
    },
    message: '다중 스케줄 PT 신청합니다!',
  });

  // HTTP POST 요청 실행
  const res = http.post(url, payload, { headers: headers });

  // 서버의 실제 응답 코드와 내용을 로그로 출력
  console.log(`[VU=${__VU}] Requesting IDs: [${scheduleIdsInOrder}] -> Response: ${res.status} Body: ${res.body}`);

  // 응답 결과 확인 (Check)
  // 일부는 성공(201)하고, 일부는 데드락으로 인해 서버 에러(500)가 발생할 것을 예상
  check(res, {
    'SUCCESS - is status 201 (Created)': (r) => r.status === 201,
    'FAIL - is status 500 (Internal Server Error)': (r) => r.status === 500,
  });
}
