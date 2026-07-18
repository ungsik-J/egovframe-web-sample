package egovframework.example.cmmn;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springmodules.validation.bean.conf.loader.annotation.handler.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FileUnit {
	private static final Logger log = LoggerFactory.getLogger(FileUnit.class);

	@Resource(name = "propertiesService")
	private EgovPropertyService propertiesService;
	private String uploadPath;
	private String createPath;

	/**
	 * 의존성 주입이 완료된 후 자동으로 실행되는 초기화 메서드
	 */
	@PostConstruct
	public void init() {
		this.createPath = propertiesService.getString("Globals.FileCreate.Path");
		this.uploadPath = propertiesService.getString("Globals.FileUpload.Path");
	}
	
	/** public static void main(String[] args) { } **/
	
	public Map<String, Object> createChunkFile(List<?> param) throws IOException {
		
		log.info("createPath:{}", createPath);
		Map<String, Object> resultMap = new HashMap<>();
		String filePath = createPath;
		String fileName = "chunkFile";
		
		// files checked
		// Validator Directory
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			log.info("\n유효하지 않은 디렉토리입니다: " + filePath);
			throw new IllegalArgumentException("#1, 유효하지 않은 디렉토리입니다: " + filePath);
		}
		// init files
		dir = new File(filePath);
		File[] files = dir.listFiles((d, name) -> name.startsWith(fileName));
		for (File file : files) {
			if (file.isFile()) {
				boolean deleted = file.delete();
				if (deleted) {
					log.info("\n1.기존파일 삭제 완료: " + file.getName());
				} else {
					log.info("\n삭제 실패: " + file.getName());
					throw new IllegalArgumentException("#2, 파일 삭제 실패" + filePath);
				}
			}
		}
		
		long recordCount = 0;
		int chunkCount = 0;
		boolean isFirstLine = true; // ★ 전체 데이터 기준 첫 줄 여부 (청크와 무관하게 한 번만 true)
	
		UUID uuid = UUID.randomUUID();
		File file = new File(filePath + fileName + "_" + uuid.toString());
		// ★ 리스트 3개(writeobj, valueLineList) 만들지 않고 바로 파일에 씀
		StringBuilder sb = new StringBuilder(1000 * 2200);
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), 1024 * 1024)) {

			for (Object item : param) {
				if (!(item instanceof Map)) {
					continue;
				}
				Map<?, ?> map = (Map<?, ?>) item;
				String id = "";
				String name = "";
				String description = "";
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					Object key = entry.getKey();
					Object value = entry.getValue();
					if ("id".equals(key)) {
						id = "[" + StringUtils.rightPad((String) value, 256, "") + "]";
					} else if ("name".equals(key)) {
						name = "[" + StringUtils.rightPad((String) value, 512, "") + "]";
					} else if ("description".equals(key)) {
						description = "[" + StringUtils.rightPad((String) value, 1024, "") + "]";
					}
				}

				// ★ 첫 줄이 아니면 "이전 줄과 구분하는 개행"을 먼저 붙임 (줄 뒤가 아니라 줄 앞에 붙이는 방식)
				if (!isFirstLine) {
					sb.append(System.lineSeparator());
				}
				sb.append(id).append(name).append(description);
				isFirstLine = false;

				recordCount++;
				chunkCount++;
				// 1000건마다 파일에 flush 하고 StringBuilder 비움 (메모리 누적 방지)
				if (chunkCount >= 1000) {
					writer.write(String.valueOf(sb));
					writer.flush();
					sb.setLength(0);
					chunkCount = 0;
				}
			}
			log.info("length:{}", sb.length());
			// 남은 데이터 마저 쓰기
			if (sb.length() > 0) {
				writer.write(String.valueOf(sb));
			}

		} catch (IOException e) {
			e.printStackTrace();
			resultMap.put("result", "fail");
			return resultMap;
		} finally {
			// ★ 큰 리스트는 다 쓴 뒤 참조 해제 (GC 대상이 되도록)
			
			if (file.exists()) {
				log.info("\n2.새로운 파일 생성 완료: " + file.getName());
				file = new File(filePath + file.getName() + ".END");
				file.createNewFile();
				log.info("\n3.새로운 END파일 생성 완료: " + file.getName());
			}
			
			param = null;
			sb = null;
			resultMap.put("result", "success");
			resultMap.put("chunkCount", chunkCount);
			resultMap.put("recordCount", recordCount);
			resultMap.put("createFilePath", filePath);
		}

		return resultMap;
	}
	
	/**
	 * @category 리스트의 각 객체를 JSON으로 변환하여, 한 줄에 1건씩 파일로 저장 (JSON Lines 형식)
	 *
	 * @param list 저장할 객체 리스트
	 * @return 저장 결과 정보
	 */
	public Map<String, Object> createNewFileByLine(List<?> list, String path) {
		Map<String, Object> resultMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		Path targetPath = Paths.get(path, "sampleListAllWriteFile");

		log.info("START::currentTimeMillis{}", System.currentTimeMillis());
		try {
			Files.createDirectories(targetPath.getParent());

			try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

				for (int i = 0; i < list.size(); i++) {
					Object item = list.get(i);
					// ★ String이면 큰따옴표 없이 그대로, 아니면 JSON 직렬화
					String line = (item instanceof String) ? (String) item : mapper.writeValueAsString(item);
					writer.write(line);
					if (i < list.size() - 1) {
						writer.newLine();
					}
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

		log.info("END::currentTimeMillis{}", System.currentTimeMillis());

		return resultMap;
	}
	
	/**
	 * @category create file
	 * 
	 * @param param
	 * @return
	 */
	public Map<String, Object> cteateNewFile(String param, String path) {
		Map<String, Object> resultMap = new HashMap<>();

		Path targetPath = Paths.get(path + System.currentTimeMillis() + ".json");

		try (InputStream in = new ByteArrayInputStream(param.getBytes(StandardCharsets.UTF_8))) {
			Files.createDirectories(targetPath.getParent()); // 디렉터리 없으면 생성
			Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

			/** ===== 파일 생성 여부 확인 ===== */
			if (Files.exists(targetPath) && Files.size(targetPath) > 0) {
				log.info("파일 생성 성공 : {} ({} bytes)", targetPath, Files.size(targetPath));
				resultMap.put("result", "SUCCESS");
				resultMap.put("filePath", targetPath.toString());
				resultMap.put("fileSize", Files.size(targetPath));
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

		return resultMap;
	}
	
	public Map<String, Object> fileUploadAjax(MultipartHttpServletRequest multipartRequest) {
		log.info("\nSTART::fileUploadAjax {} ⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥");
		ObjectMapper objectMapper = new ObjectMapper();
		String filePath = uploadPath;  //FileUpload+"sampleList/"; // {Globals.filePath}/file/uplpad/
		
		log.info("filePath:{}", filePath);
		
		try {
			log.info("multipartRequest : {}", objectMapper.writeValueAsString(multipartRequest.getParameterMap()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			String id = multipartRequest.getParameter("id");
			String name = multipartRequest.getParameter("name");
			String description = multipartRequest.getParameter("description");
			String useYn = multipartRequest.getParameter("useYn");

			MultipartFile uploadFile = multipartRequest.getFile("uploadFile"); // input name과 일치

			String orgFileName = "";
			String saveFileName = "";

			if (uploadFile != null && !uploadFile.isEmpty()) {
				orgFileName = uploadFile.getOriginalFilename();
				String ext = FilenameUtils.getExtension(orgFileName);
				saveFileName = UUID.randomUUID().toString() + "." + ext;

				File dir = new File(filePath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				uploadFile.transferTo(new File(filePath + saveFileName));
			}

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("id", id);
			paramMap.put("name", name);
			paramMap.put("description", description);
			paramMap.put("useYn", useYn);
			paramMap.put("orgFileName", orgFileName);
			paramMap.put("fileName", saveFileName);
			paramMap.put("filePath", filePath);

			resultMap.put("result", "sucess");
			resultMap.put("resultCode", "0000");
			resultMap.put("resultMsg", "정상적으로 처리되었습니다.");
			resultMap.put("paramMap", paramMap);

		} catch (Exception e) {
			resultMap.put("result", "fail");
			resultMap.put("resultCode", "-1");
			resultMap.put("resultMsg", e.getMessage());
			e.printStackTrace();
		}
		log.info("\nEND::fileUploadAjax {}⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤");
		return resultMap;
	}
	
	public void createFile(InputStream inputStream, String targetPath) throws IOException {
		Path _targetPath = Paths.get(targetPath);
		Files.copy(inputStream, _targetPath, StandardCopyOption.REPLACE_EXISTING);
	}
}
