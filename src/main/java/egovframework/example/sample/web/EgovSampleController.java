/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.example.sample.web;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springmodules.validation.commons.DefaultBeanValidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.com.cmm.util.MapKeyConverter;
import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;
import lombok.RequiredArgsConstructor;

/**
 * @Class Name : EgovSampleController.java
 * @Description : EgovSample Controller Class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 @ --------- --------- ------------------------------- @
 *   2009.03.16 최초생성
 *
 * @author 개발프레임웍크 실행환경 개발팀
 * @since 2009. 03.16
 * @version 1.0
 * @param <E>
 * @see
 *
 *      Copyright (C) by MOPAS All right reserved.
 */

@Controller
@RequiredArgsConstructor
public class EgovSampleController<E> {
	private static final Logger log = LoggerFactory.getLogger(EgovSampleController.class);
	/** EgovSampleService */
	private final EgovSampleService sampleService;

	/** EgovPropertyService */
	private final EgovPropertyService propertiesService;

	/** Validator */
	private final DefaultBeanValidator beanValidator;

	@SuppressWarnings("unused")
	@Autowired
	private final MapKeyConverter mapKeyConverter;

	// 실제 서버 저장 경로 (properties로 분리 권장)
	private final String uploadPath = "c:/temp/upload/sample/";

	@GetMapping("/sample/{pageName}.do")
	public String dynamicPageMapping(@PathVariable("pageName") String pageName) {

		// 로그에 들어온 pageName 값 출력
		log.info("dynamicPageMapping : {}", pageName);

		// 들어온 URL 값(pageName)을 그대로 HTML 파일명으로 지정하여 동적 이동
		return "sample/" + pageName;
	}

	@RequestMapping(value = "/egovSampleListAjaxDownload.do")
	@ResponseBody
	public ResponseEntity<?> egovSampleListAjaxDownload(@ModelAttribute("searchVO") SampleDefaultVO searchVO) {
	    Map<String, Object> resultMap = new HashMap<>();
	    ObjectMapper mapper = new ObjectMapper();

	    List<?> sampleListAll = null;
	    try {
	        sampleListAll = sampleService.selectSampleListAll(searchVO);
	    } catch (Exception e) {
	        e.printStackTrace();
	        resultMap.put("result", "fail");
	        try {
	            return new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.INTERNAL_SERVER_ERROR);
	        } catch (JsonProcessingException ex) {
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	    //String filePath = "/path/to/output/sample_" + System.currentTimeMillis() + ".txt";
	    long recordCount = 0;

	    // ★ 리스트 3개(writeobj, valueLineList) 만들지 않고 바로 파일에 씀
	    try (BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(uploadPath+"listfile"), StandardCharsets.UTF_8), 1024 * 1024)) {

	        StringBuilder sb = new StringBuilder();
	        int chunkCount = 0;

	        for (Object item : sampleListAll) {
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
	                    id = "[" + StringUtils.rightPad((String) value, 600, "") + "]";
	                } else if ("name".equals(key)) {
	                    name = "[" + StringUtils.rightPad((String) value, 1010, "") + "]";
	                } else if ("description".equals(key)) {
	                    description = "[" + StringUtils.rightPad((String) value, 500, "") + "]";
	                }
	            }

	            sb.append(id).append(name).append(description).append(System.lineSeparator());
	            recordCount++;
	            chunkCount++;

	            // 1000건마다 파일에 flush 하고 StringBuilder 비움 (메모리 누적 방지)
	            if (chunkCount >= 1000) {
	                writer.write(sb.toString());
	                sb.setLength(0);
	                chunkCount = 0;
	            }
	        }

	        // 남은 데이터 마저 쓰기
	        if (sb.length() > 0) {
	            writer.write(sb.toString());
	        }

	        writer.flush();

	    } catch (IOException e) {
	        e.printStackTrace();
	        resultMap.put("result", "fail");
	        try {
	            return new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.INTERNAL_SERVER_ERROR);
	        } catch (JsonProcessingException ex) {
	            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    } finally {
	        // ★ 큰 리스트는 다 쓴 뒤 참조 해제 (GC 대상이 되도록)
	        sampleListAll = null;
	    }

	    log.info("파일 저장 완료 :: filePath:{}, recordCount:{}", uploadPath, recordCount);

	    resultMap.put("result", "success");
	    resultMap.put("filePath", uploadPath);
	    resultMap.put("recordCount", recordCount);

	    try {
	        return new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.OK);
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	/**
	 * AJAX 목록 조회 (JSON 응답)
	 */
	@RequestMapping(value = "/egovSampleListAjax.do")
	@ResponseBody // 리턴되는 Map을 JSON 구조로 자동 변환 (Jackson 라이브러리 필요)
	public ResponseEntity<?> egovSampleListAjax(@ModelAttribute("searchVO") SampleDefaultVO searchVO) {
		log.info("\nSTART::egovSampleListAjax {} ⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥");
		Map<String, Object> resultMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		try {

			log.info("\nsearchVO :\n {}", mapper.writeValueAsString(searchVO), searchVO);

			/** 1. 페이징 설정 (전자정부 표준) */
			searchVO.setPageUnit(10); // 한 페이지에 보여줄 개수
			searchVO.setPageSize(5); // 페이징 네비게이션 크기

			PaginationInfo paginationInfo = new PaginationInfo();

			if (StringUtils.isNoneBlank(searchVO.getSearchKeyword())) {
				searchVO.setPageIndex(1);
			}

			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			/** 2. 데이터 조회 */
			List<?> sampleList = sampleService.selectSampleList(searchVO);
			int totCnt = sampleService.selectSampleListTotCnt(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			/** 3. 결과 Map에 담기 */
			resultMap.put("resultList", sampleList);
			resultMap.put("paginationInfo", paginationInfo);
			resultMap.put("searchVO", searchVO);
			resultMap.put("result", "SUCCESS");

			log.info("resultMap : {}", mapper.writeValueAsString(resultMap), resultMap);
			log.info("END::egovSampleListAjax {}⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤");
			return new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.OK);

		} catch (Exception e) {
			resultMap.put("result", "FAIL");
			resultMap.put("message", e.getMessage());
			return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	/**
	 * @category 리스트의 각 객체를 JSON으로 변환하여, 한 줄에 1건씩 파일로 저장 (JSON Lines 형식)
	 *
	 * @param list 저장할 객체 리스트
	 * @return 저장 결과 정보
	 */
	public Map<String, Object> createNewFileByLine(List<?> list) {
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
	/**
	 * @category create file
	 * 
	 * @param param
	 * @return
	 */
	public Map<String, Object> cteateNewFile(String param) {
	    Map<String, Object> resultMap = new HashMap<>();

	    Path targetPath = Paths.get(uploadPath + "/sampleList_" + System.currentTimeMillis() + ".json");

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

	public void createFile(InputStream inputStream, String targetPath) throws IOException {
		Path _targetPath = Paths.get(targetPath);
		Files.copy(inputStream, _targetPath, StandardCopyOption.REPLACE_EXISTING);
	}

	@RequestMapping(value = "/fileUploadAjax.do", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> fileUploadAjax(MultipartHttpServletRequest multipartRequest) {
		log.info("\nSTART::fileUploadAjax {} ⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			log.info("multipartRequest : {}", objectMapper.writeValueAsString(multipartRequest.getParameterMap()));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			String id = multipartRequest.getParameter("id");
			String name = multipartRequest.getParameter("name");
			String description = multipartRequest.getParameter("description");

			MultipartFile uploadFile = multipartRequest.getFile("uploadFile"); // input name과 일치

			String orgFileName = "";
			String saveFileName = "";

			if (uploadFile != null && !uploadFile.isEmpty()) {
				orgFileName = uploadFile.getOriginalFilename();
				String ext = FilenameUtils.getExtension(orgFileName);
				saveFileName = UUID.randomUUID().toString() + "." + ext;

				File dir = new File(uploadPath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				uploadFile.transferTo(new File(uploadPath + saveFileName));
			}

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("id", id);
			paramMap.put("name", name);
			paramMap.put("description", description);
			paramMap.put("orgFileName", orgFileName);
			paramMap.put("fileName", saveFileName);
			paramMap.put("filePath", uploadPath);

			// sampleService.updateSample(paramMap);

			resultMap.put("resultCode", "success");
			resultMap.put("resultMsg", "정상적으로 처리되었습니다.");

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("resultCode", "fail");
			resultMap.put("resultMsg", e.getMessage());
		}
		log.info("\nEND::fileUploadAjax {}⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤");
		return resultMap;
	}

	/**
	 * 글 목록을 조회한다. (pageing)
	 * 
	 * @param searchVO - 조회할 정보가 담긴 SampleDefaultVO
	 * @param model
	 * @return "egovSampleList"
	 * @exception Exception
	 */
	@GetMapping("/egovSampleList.do")
	public String egovSampleList(@ModelAttribute("searchVO") SampleDefaultVO searchVO, ModelMap model)
			throws Exception {
		log.info("\nSTART::egovSampleList {} ⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			log.info("searchVO : {}", objectMapper.writeValueAsString(searchVO));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/** EgovPropertyService.sample */
		searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
		searchVO.setPageSize(propertiesService.getInt("pageSize"));

		/** pageing setting */
		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
		paginationInfo.setPageSize(searchVO.getPageSize());

		searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
		searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		List<?> sampleList = sampleService.selectSampleList(searchVO);
		model.addAttribute("resultList", sampleList);

		int totCnt = sampleService.selectSampleListTotCnt(searchVO);
		paginationInfo.setTotalRecordCount(totCnt);
		model.addAttribute("paginationInfo", paginationInfo);
		log.info("\nEND::egovSampleList {}⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤");
		return "sample/egovSampleList";
	}

	/**
	 * 글 등록 화면을 조회한다.
	 * 
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param model
	 * @return "egovSampleRegister"
	 * @exception Exception
	 */
	@GetMapping("/addSample.do")
	public String addSampleView(@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		model.addAttribute("sampleVO", new SampleVO());
		return "sample/egovSampleRegister";
	}

	/**
	 * 글을 등록한다.
	 * 
	 * @param sampleVO - 등록할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@PostMapping("/addSample.do")
	public String addSample(@ModelAttribute("searchVO") SampleDefaultVO searchVO, SampleVO sampleVO,
			BindingResult bindingResult, Model model, SessionStatus status) throws Exception {

		// Server-Side Validation
		beanValidator.validate(sampleVO, bindingResult);

		if (bindingResult.hasErrors()) {
			model.addAttribute("sampleVO", sampleVO);
			return "sample/egovSampleRegister";
		}

		sampleService.insertSample(sampleVO);
		status.setComplete();

		model.addAttribute("searchCondition", sampleVO.getSearchCondition());
		model.addAttribute("searchKeyword", sampleVO.getSearchKeyword());
		model.addAttribute("pageIndex", sampleVO.getPageIndex());

		return "redirect:/egovSampleList.do";
	}

	/**
	 * 글 수정화면을 조회한다.
	 * 
	 * @param id       - 수정할 글 id
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param model
	 * @return "egovSampleRegister"
	 * @exception Exception
	 */
	@GetMapping("/updateSampleView.do")
	public String updateSampleView(@RequestParam("selectedId") String id,
			@ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		SampleVO sampleVO = new SampleVO();
		sampleVO.setId(id);
		// 변수명은 CoC 에 따라 sampleVO
		model.addAttribute(selectSample(sampleVO, searchVO));
		return "sample/egovSampleRegister";
	}

	/**
	 * 글을 조회한다.
	 * 
	 * @param sampleVO - 조회할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return @ModelAttribute("sampleVO") - 조회한 정보
	 * @exception Exception
	 */
	public SampleVO selectSample(SampleVO sampleVO, @ModelAttribute("searchVO") SampleDefaultVO searchVO)
			throws Exception {
		return sampleService.selectSample(sampleVO);
	}

	/**
	 * 글을 수정한다.
	 * 
	 * @param sampleVO - 수정할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@PostMapping("/updateSample.do")
	public String updateSample(@ModelAttribute("searchVO") SampleDefaultVO searchVO, SampleVO sampleVO,
			BindingResult bindingResult, Model model, SessionStatus status) throws Exception {

		beanValidator.validate(sampleVO, bindingResult);

		if (bindingResult.hasErrors()) {
			model.addAttribute("sampleVO", sampleVO);
			return "sample/egovSampleRegister";
		}

		sampleService.updateSample(sampleVO);
		status.setComplete();

		model.addAttribute("searchCondition", sampleVO.getSearchCondition());
		model.addAttribute("searchKeyword", sampleVO.getSearchKeyword());
		model.addAttribute("pageIndex", sampleVO.getPageIndex());

		return "redirect:/egovSampleList.do";
	}

	/**
	 * 글을 삭제한다.
	 * 
	 * @param sampleVO - 삭제할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@PostMapping("/deleteSample.do")
	public String deleteSample(SampleVO sampleVO, @ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model,
			SessionStatus status) throws Exception {
		sampleService.deleteSample(sampleVO);
		status.setComplete();

		model.addAttribute("searchCondition", sampleVO.getSearchCondition());
		model.addAttribute("searchKeyword", sampleVO.getSearchKeyword());
		model.addAttribute("pageIndex", sampleVO.getPageIndex());

		return "redirect:/egovSampleList.do";
	}
}
