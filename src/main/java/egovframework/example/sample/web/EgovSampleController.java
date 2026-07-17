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

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Property;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
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
import egovframework.example.cmmn.FileUnit;
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

	/** Validator */
	private final DefaultBeanValidator beanValidator;

	@SuppressWarnings("unused")
	@Autowired
	private final MapKeyConverter mapKeyConverter;

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

	@Autowired
	private FileUnit fileUnit;

	private static final List<String> COLUMNS = List.of("HEADER", "ID", "NAME", "DESCRIPTION", "USE_YN", "REG_USER");

	@GetMapping("/sample/{pageName}.do")
	public String dynamicPageMapping(@PathVariable("pageName") String pageName) {

		// 로그에 들어온 pageName 값 출력
		log.info("dynamicPageMapping : {}", pageName);

		// 들어온 URL 값(pageName)을 그대로 HTML 파일명으로 지정하여 동적 이동
		return "sample/" + pageName;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/fileUploadAjax.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> fileUploadAjax(MultipartHttpServletRequest multipartRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
		log.info("\nSTART::fileUploadAjax {} ⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥⩥");
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			log.info("multipartRequest : {}", objectMapper.writeValueAsString(multipartRequest.getParameterMap()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			resultMap = fileUnit.fileUploadAjax(multipartRequest);

			log.info("fileUploadAjax.resultMap:{}",
					objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap));

			if ("sucess".equals(resultMap.get("result"))) {
				Object paramMapObj = resultMap.get("paramMap");
				Map<String, Object> paramMap = (Map<String, Object>) paramMapObj;

				log.info("1{}", paramMap.get("fileName"));
				log.info("1{}", paramMap.get("filePath"));
				log.info("1{}", paramMap.get("id"));

				SampleVO samplevo = new SampleVO();

				String fileName = paramMap.get("filePath") + "" + paramMap.get("fileName");
				samplevo.setId(String.valueOf(paramMap.get("id")));
				samplevo.setName(String.valueOf(paramMap.get("name")));
				samplevo.setDescription(String.valueOf(paramMap.get("description")));
				samplevo.setUseYn(String.valueOf(paramMap.get("useYn")));
				samplevo.setFileName(fileName);

				int updateCnt = sampleService.updateSample(samplevo);

				if (updateCnt > 0) {
					resultMap.put("result", resultMap.get("result"));
				} else {
					resultMap.put("result", "updatefail");
				}
			}

			return new ResponseEntity<>(objectMapper.writeValueAsString(resultMap), headers, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("resultCode", "fail");
			resultMap.put("resultMsg", e.getMessage());
			try {
				return new ResponseEntity<>(objectMapper.writeValueAsString(resultMap), headers,
						HttpStatus.valueOf(-1));
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		log.info("\nEND::fileUploadAjax {}⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤⩤");
		return null;
	}

	@RequestMapping(value = "/egovSampleListAjaxDownload.do")
	@ResponseBody
	public ResponseEntity<?> egovSampleListAjaxDownload(@ModelAttribute("searchVO") SampleDefaultVO searchVO)
			throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> resultMap = new HashMap<>();
		ResponseEntity<?> responseentity = new ResponseEntity<>(HttpStatus.NO_CONTENT);

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			long startTime = System.currentTimeMillis();
			log.info("egovSampleListAjaxDownload.startTime : {}", sdf.format(new Date(startTime)));

			resultMap = fileUnit.createChunkFile(sampleService.selectSampleListAll(searchVO));

			responseentity = new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", "fail");
			responseentity = new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.EXPECTATION_FAILED);
		}
		return responseentity;
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

			log.info("sampleList:{}", sampleList);

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

	@RequestMapping(value = "/egovSampleImageView.do")
	@ResponseBody
	public ResponseEntity<?> egovSampleImageView(@RequestParam("fileName") String fileName) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		// String uploadPath = propertiesService.getString("Globals.FileUpload.Path");
		// // /home/john/devHome/file/upload/

		try {
			// 보안: 경로 조작(../) 방지 필수!
			if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
				resultMap.put("success", false);
				resultMap.put("message", "잘못된 파일명입니다.");
				
				return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			log.info("uploadPath:{}, fileName:{}", uploadPath , fileName);
			File imageFile = new File(uploadPath + fileName);

			if (!imageFile.exists()) {
				resultMap.put("success", false);
				resultMap.put("message", "파일이 존재하지 않습니다.");
				return new ResponseEntity<>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
			}

			// 파일을 바이트 배열로 읽어서 base64 인코딩
			byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
			String base64Image = Base64.getEncoder().encodeToString(fileBytes);

			// 확장자 추출 (image/jpeg, image/png 등 MIME 타입 결정용)
			String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

			resultMap.put("success", true);
			resultMap.put("imageBase64", base64Image);
			resultMap.put("fileType", fileType);
			resultMap.put("fileName", fileName);

			return new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.OK);
		} catch (IOException e) {
			resultMap.put("success", false);
			resultMap.put("message", "이미지를 읽는 중 오류가 발생했습니다: " + e.getMessage());
		}

		try {
			return new ResponseEntity<>(mapper.writeValueAsString(resultMap), HttpStatus.OK);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return null;
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

		/////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * private static final List<String> COLUMNS = List.of("HEADER", "ID", "NAME",
		 * "DESCRIPTION", "USE_YN", "REG_USER");
		 **/
		List<Map<String, Object>> hashMapList = sampleService.selectSampleListHashMap(searchVO);
		List<Map<String, Object>> resultList = new ArrayList<>();
		for (Map<String, Object> data : hashMapList) {
			Map<String, Object> rowMap = new LinkedHashMap<>();
			for (String col : COLUMNS) {
				rowMap.put(col, data.get(col));
			}
			resultList.add(rowMap);
		}
		log.info("\n🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨\ncolArry:{}",
				objectMapper.writeValueAsString(resultList) + "\n🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨🚨");
		/////////////////////////////////////////////////////////////////////////////////////////

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
