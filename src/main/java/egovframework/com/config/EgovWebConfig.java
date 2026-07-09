package egovframework.com.config; // 프로젝트에 맞는 패키지 경로

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration // 1. 이 어노테이션이 붙은 클래스를 찾으세요!
public class EgovWebConfig { // 클래스 이름은 프로젝트마다 다를 수 있습니다.

	// 2. 이 위치에 메서드를 추가합니다.
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();

		// 최대 업로드 가용 용량 (예: 10MB)
		commonsMultipartResolver.setMaxUploadSize(10485760);
		// 한 요청당 최대 업로드 용량
		commonsMultipartResolver.setMaxUploadSizePerFile(10485760);
		// 인코딩 타입 설정
		commonsMultipartResolver.setDefaultEncoding("UTF-8");

		return commonsMultipartResolver;
	}
}