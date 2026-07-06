package egovframework.com.cmm.util;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Map의 Key 값을 snake_case에서 camelCase로 변환해주는 유틸리티 클래스
 * 
 * <p>주로 DB 조회 결과(snake_case 컬럼명, 예: user_id, first_name)를
 * 화면/API 응답용 JSON(camelCase, 예: userId, firstName)으로 변환할 때 사용한다.</p>
 * 
 * <p>Spring Bean으로 등록되어 있으나, 내부 메소드는 모두 static이므로
 * 반드시 DI(의존성 주입) 없이 MapKeyConverter.convertToCamelCase(...) 형태로
 * 바로 호출해서 사용해도 무방하다.</p>
 */
@Component
public class MapKeyConverter {

    /**
     * Map의 모든 Key를 snake_case에서 camelCase로 변환한다.
     * Value가 중첩된 Map(Map 안에 Map)인 경우 재귀적으로 내부까지 모두 변환한다.
     *
     * 예시)
     *   입력: {"user_id": 1, "user_info": {"first_name": "홍길동"}}
     *   출력: {"userId": 1, "userInfo": {"firstName": "홍길동"}}
     *
     * @param map 변환할 원본 Map (Key: snake_case, Value: Object)
     * @return Key가 camelCase로 변환된 새로운 Map (원본 map은 변경되지 않음)
     */
    @SuppressWarnings("unchecked")
	public static Map<String, Object> convertToCamelCase(Map<String, Object> map) {
        // 변환 결과를 담을 새로운 Map 생성 (원본 map은 그대로 유지하기 위해 복사본 사용)
        Map<String, Object> result = new HashMap<>();

        // 원본 Map의 모든 Key-Value 쌍을 하나씩 순회
        for (Map.Entry<String, Object> entry : map.entrySet()) {

            // 현재 Entry의 Key(예: "user_id")를 camelCase(예: "userId")로 변환
            String camelKey = toCamelCase(entry.getKey());

            // 현재 Entry의 Value를 가져옴 (아직 변환 전 원본 값)
            Object value = entry.getValue();

            // 값이 또 Map이면 재귀적으로 변환 (nested map 대응)
            // 예: user_info 라는 Key의 Value가 또 다른 Map일 경우,
            //     그 내부 Map의 Key들도 camelCase로 변환해야 하므로 재귀 호출
            if (value instanceof Map) {
                // 안전하지 않은 형변환이지만(unchecked cast),
                // 실무에서 Map<String, Object> 형태로만 사용한다는 전제하에 캐스팅
                value = convertToCamelCase((Map<String, Object>) value);
            }

            // 변환된 Key와 (필요시 재귀 변환된) Value를 결과 Map에 저장
            result.put(camelKey, value);
        }

        // 모든 Key 변환이 완료된 새로운 Map을 반환
        return result;
    }

    /**
     * 단일 문자열을 snake_case에서 camelCase로 변환한다.
     * 언더스코어(_) 뒤에 오는 첫 글자를 대문자로 바꾸고, 언더스코어 자체는 제거한다.
     *
     * 예시)
     *   "user_id"    -> "userId"
     *   "first_name" -> "firstName"
     *   "created_at" -> "createdAt"
     *
     * @param snakeCase 변환할 snake_case 형식의 문자열
     * @return camelCase로 변환된 문자열
     */
    public static String toCamelCase(String snakeCase) {
        // 변환된 문자를 하나씩 이어붙일 StringBuilder (문자열 연산 성능 개선을 위해 사용)
        StringBuilder result = new StringBuilder();

        // 다음에 나올 문자를 대문자로 바꿔야 하는지 여부를 나타내는 플래그
        // true가 되는 시점: 바로 직전 문자가 '_'(언더스코어)였을 때
        boolean nextUpper = false;

        // 문자열을 한 글자씩 순회하며 검사
        for (char c : snakeCase.toCharArray()) {

            if (c == '_') {
                // 현재 문자가 언더스코어(_)라면,
                // 이 언더스코어는 결과 문자열에 포함시키지 않고(제거)
                // 다음 문자를 대문자로 변환하라는 플래그만 세워둠
                nextUpper = true;
            } else {
                // 현재 문자가 언더스코어가 아닌 일반 문자인 경우
                // - nextUpper가 true이면: 대문자로 변환해서 추가 (언더스코어 바로 다음 문자)
                // - nextUpper가 false이면: 원래 문자 그대로 추가
                result.append(nextUpper ? Character.toUpperCase(c) : c);

                // 대문자 변환 처리가 끝났으므로 플래그를 다시 false로 초기화
                nextUpper = false;
            }
        }

        // StringBuilder에 쌓인 문자들을 최종 String으로 변환하여 반환
        return result.toString();
    }

    /**
     * 클래스 동작을 테스트하기 위한 메인 메소드
     * (실제 운영 코드에서는 사용되지 않으며, 단순 동작 확인용 예제 코드)
     *
     * @param args 프로그램 실행 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        // 테스트용 snake_case Key를 가진 Map 생성
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", 1);              // Key: user_id, Value: 1 (Integer)
        map.put("first_name", "홍길동");      // Key: first_name, Value: "홍길동" (String)
        map.put("created_at", "2026-07-06"); // Key: created_at, Value: 날짜 문자열

        // convertToCamelCase 메소드를 호출하여 Key들을 camelCase로 변환
        Map<String, Object> converted = convertToCamelCase(map);

        // 변환된 결과를 콘솔에 출력하여 확인
        System.out.println(converted);
        // 출력 예상 결과: {userId=1, firstName=홍길동, createdAt=2026-07-06}
    }
}