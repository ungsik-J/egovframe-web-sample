package egovframework.example.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HelloWorld {
	private static final Logger log = LoggerFactory.getLogger(HelloWorld.class);
	private final static String uploadPath = "c:/temp/upload/sample/";
	public static void main(String[] args) throws IOException {
		
		int rownum = 9999999;
		
		runCreateSampleData();
		
		
		//runCreateSampleData();
	}
	
	public static void runCreateSampleData() throws FileNotFoundException, IOException {
		String filePath = "C:\\home\\john\\devHome\\file\\create\\sampleData_9999.csv"; // C:\home\john\devHome\file\create
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		long startTime = System.currentTimeMillis();
		log.info("startTime : {}", sdf.format(new Date(startTime)));
		
		File file = new File(filePath);
		
		file.delete();
		
	    try (BufferedWriter writer = new BufferedWriter(
	            new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8),
	            1024 * 1024)) {

	    	
	        Random random = new Random();
	        StringBuilder sb = new StringBuilder(1000 * 100);
	        int chunkCount = 0;
	        sb.append("ID,NAME,DESCRIPTION,USE_YN,REG_USER,FILE_NAME,PROD_NUM,AMT");
	        sb.append(System.lineSeparator());
	        for (int i = 0; i < 9999; i++) {
	        	char randomYN = (Math.random() < 0.5) ? 'Y' : 'N';
	        	int random5Digits = (int) (Math.random() * 90000) + 10000;
	        	int random4Digits = (int) (Math.random() * 9000) + 1000;
	            sb.append("SAMPLE-").append(i).append(",")
	              .append("Runtime Environment#").append(i).append(",")
	              .append("Presentation Layer#").append(i).append(",")
	              .append( randomYN ).append(",")
	              .append("eGov").append(",")
	              .append("is not file").append(",")
	              .append(random4Digits).append(",") //prod_num
	              .append(random5Digits) //amt
	              .append(System.lineSeparator());

	            chunkCount++;
	            if (chunkCount >= 1000) {
	                writer.write(sb.toString());
	                sb.setLength(0);
	                chunkCount = 0;
	            }
	        }
	        if (sb.length() > 0) {
	            writer.write(sb.toString());
	        }
	        
			long endTime = System.currentTimeMillis();
			long elapsedMillis = endTime - startTime;
			// 소요시간(기간)은 시:분:초 형식으로 별도 변환
			String elapsedFormatted = String.format("%02d:%02d:%02d", (elapsedMillis / 1000) / 3600,
					((elapsedMillis / 1000) % 3600) / 60, (elapsedMillis / 1000) % 60);
			log.info("endTime : {}, elapsedTime : {}", sdf.format(new Date(endTime)), elapsedFormatted);
	    }
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
