package egovframework.example.sample;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HelloWorld {
	private static final Logger log = LoggerFactory.getLogger(HelloWorld.class);
	private final static String uploadPath = "c:/temp/upload/sample/";
	public static <E> void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<E> createDATA = new ArrayList<E>();
		for(int i = 0; i < 999999; i++) {
			String txt = "SAMPLE-"+i+",Runtime Environment_"+i+",Presentation Layer"+i+",Y,eGov";
			
			createDATA.add((E) txt);
			
		}
		
		Map<String, Object> isFileAll = createNewFileByLine(createDATA);
	}
	/**
	 * @category 리스트의 각 객체를 JSON으로 변환하여, 한 줄에 1건씩 파일로 저장 (JSON Lines 형식)
	 *
	 * @param list 저장할 객체 리스트
	 * @return 저장 결과 정보
	 */
	public static  Map<String, Object> createNewFileByLine(List<?> list) {
	    Map<String, Object> resultMap = new HashMap<>();
	    ObjectMapper mapper = new ObjectMapper();

	    Path targetPath = Paths.get(uploadPath, "sampleListAllWriteFile");

	    log.info("START::currentTimeMillis{}" , System.currentTimeMillis());
	    try {
	        Files.createDirectories(targetPath.getParent());

	        try (BufferedWriter writer = Files.newBufferedWriter(
	                targetPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

	            for (Object item : list) {
	                // ★ String이면 큰따옴표 없이 그대로, 아니면 JSON 직렬화
	                String line = (item instanceof String) ? (String) item : mapper.writeValueAsString(item);
	                writer.write(line);
	                writer.newLine();
	            }
	        }

	        if (Files.exists(targetPath) && Files.size(targetPath) > 0) {
	            log.info("파일 생성 성공 : {} ({} bytes, {} records)", targetPath, Files.size(targetPath), list.size());
	            resultMap.put("result", "SUCCESS");
	            resultMap.put("filePath", targetPath.toString());
	            resultMap.put("fileSize", Files.size(targetPath));
	            resultMap.put("recordCount", list.size());
	        } else {
	            log.warn("파일 생성 실패(파일 없음 또는 크기 0) : {}", targetPath);
	            resultMap.put("result", "FAIL");
	            resultMap.put("message", "파일이 생성되지 않았습니다.");
	        }

	    } catch (IOException e) {
	        log.error("파일 생성 중 오류 발생 : {}", targetPath, e);
	        resultMap.put("result", "FAIL");
	        resultMap.put("message", e.getMessage());
	    }
	    
	    log.info("END::currentTimeMillis{}" , System.currentTimeMillis());
	    
	    return resultMap;
	}
}
