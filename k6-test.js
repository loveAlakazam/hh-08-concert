import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭 정의
export let errorCount = new Counter('errors');
export let errorRate = new Rate('error_rate');
export let responseTimeTrend = new Trend('response_time_trend');

export let options = {
  stages: [
    { duration: '2m', target: 10 }, // 2분 동안 10명까지 증가
    { duration: '5m', target: 10 }, // 5분 동안 10명 유지
    { duration: '2m', target: 20 }, // 2분 동안 20명까지 증가
    { duration: '5m', target: 20 }, // 5분 동안 20명 유지
    { duration: '2m', target: 0 },  // 2분 동안 0명까지 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이하
    http_req_failed: ['rate<0.1'],    // 에러율 10% 이하
    error_rate: ['rate<0.1'],
  },
  // InfluxDB로 결과 전송
  ext: {
    influxdb: {
      addr: 'http://localhost:8086',
      db: 'k6',
      username: 'k6',
      password: 'k6',
    },
  },
};

export default function() {
  let response = http.get('http://localhost:8080/api/test');

  let checkResult = check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  if (!checkResult) {
    errorCount.add(1);
    errorRate.add(true);
  } else {
    errorRate.add(false);
  }

  responseTimeTrend.add(response.timings.duration);

  sleep(1);
}

// 테스트 종료 후 요약 출력
export function handleSummary(data) {
  return {
    'summary.json': JSON.stringify(data, null, 2),
  };
}