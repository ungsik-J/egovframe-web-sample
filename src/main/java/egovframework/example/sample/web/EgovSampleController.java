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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springmodules.validation.commons.DefaultBeanValidator;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;
import lombok.RequiredArgsConstructor;

/**
 * @Class Name : EgovSampleController.java
 * @Description : EgovSample Controller Class
 * @Modification Information
 * @
 * @  수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2009.03.16           최초생성
 *
 * @author 개발프레임웍크 실행환경 개발팀
 * @since 2009. 03.16
 * @version 1.0
 * @see
 *
 *  Copyright (C) by MOPAS All right reserved.
 */

@Controller
@RequiredArgsConstructor
public class EgovSampleController {
	private static final Logger log = LoggerFactory.getLogger(EgovSampleController.class);
	/** EgovSampleService */
	private final EgovSampleService sampleService;

	/** EgovPropertyService */
	private final EgovPropertyService propertiesService;

	/** Validator */
	private final DefaultBeanValidator beanValidator;
	/**
     * AJAX 목록 조회 (JSON 응답)
     */
    @RequestMapping(value = "/egovSampleListAjax.do")
    @ResponseBody // 리턴되는 Map을 JSON 구조로 자동 변환 (Jackson 라이브러리 필요)
    public ResponseEntity<?> selectSampleListAjax(@ModelAttribute("searchVO") SampleDefaultVO searchVO) {
        
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            /** 1. 페이징 설정 (전자정부 표준) */
            searchVO.setPageUnit(10); // 한 페이지에 보여줄 개수
            searchVO.setPageSize(5);  // 페이징 네비게이션 크기
            
            PaginationInfo paginationInfo = new PaginationInfo();

            if(StringUtils.isNoneBlank(searchVO.getSearchKeyword())) {
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

            ObjectMapper mapper = new ObjectMapper();

            return new ResponseEntity<>( mapper.writeValueAsString( resultMap ), HttpStatus.OK);
            
        } catch (Exception e) {
            resultMap.put("result", "FAIL");
            resultMap.put("message", e.getMessage());
            return new ResponseEntity<>( resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(value = "/updateEgovSampleAjax.do")
    @ResponseBody
    public Map<String, Object> updateEgovSampleAjax(MultipartHttpServletRequest multiRequest, HttpServletRequest request) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            // 1. 파라미터 데이터 확인 (formData에 넣었던 일반 텍스트 데이터 추출 예시)
            String sampleId = multiRequest.getParameter("id"); 
            String sampleName = multiRequest.getParameter("name");
            
            // TODO: 기존 데이터 수정 비즈니스 로직 수행 (예: sampleService.updateSample(...))

            // 2. 파일 업로드 처리 (InputStream 활용)
            Map<String, MultipartFile> files = multiRequest.getFileMap();
            Iterator<String> tags = files.keySet().iterator();
            
            // 저장할 파일 경로 설정 (서버 환경에 맞게 수정 필요)
            String uploadPath = request.getServletContext().getRealPath("/upload/");
            File saveDir = new File(uploadPath);
            if (!saveDir.exists()) {
                saveDir.mkdirs(); // 디렉토리가 없으면 생성
            }

            while (tags.hasNext()) {
                String tagName = tags.next();
                MultipartFile multipartFile = files.get(tagName);
                
                if (multipartFile != null && !multipartFile.isEmpty()) {
                    String originalFileName = multipartFile.getOriginalFilename();
                    // 파일명 중복 방지를 위한 UUID 적용
                    String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName; 
                    
                    File targetFile = new File(uploadPath + File.separator + savedFileName);
                    
                    // 핵심: InputStream을 열어서 파일 저장 처리
                    try (InputStream inputStream = multipartFile.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    // (선택) DB에 파일 정보 저장 로직 필요 시 여기에 구현
                    System.out.println("파일 저장 완료: " + targetFile.getAbsolutePath());
                }
            }

            resultMap.put("result", "SUCCESS");
            resultMap.put("message", "정상적으로 수정 및 파일 업로드가 완료되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("result", "FAIL");
            resultMap.put("message", "오류가 발생했습니다: " + e.getMessage());
        }

        return resultMap; // @ResponseBody에 의해 JSON 형태로 클라이언트에 반환됩니다.
    }
    
	/**
	 * 글 목록을 조회한다. (pageing)
	 * @param searchVO - 조회할 정보가 담긴 SampleDefaultVO
	 * @param model
	 * @return "egovSampleList"
	 * @exception Exception
	 */
	@GetMapping("/egovSampleList.do")
	public String selectSampleList(@ModelAttribute("searchVO") SampleDefaultVO searchVO, ModelMap model) throws Exception {
		log.info("\nSTART::selectSampleList {} ************************************************************************");
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
		log.info("\nEND::selectSampleList {} **************************************************************************");
		return "sample/egovSampleList";
	}

	/**
	 * 글 등록 화면을 조회한다.
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
	 * @param sampleVO - 등록할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@PostMapping("/addSample.do")
	public String addSample(@ModelAttribute("searchVO") SampleDefaultVO searchVO, SampleVO sampleVO, BindingResult bindingResult, Model model, SessionStatus status)
			throws Exception {

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
	 * @param id - 수정할 글 id
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param model
	 * @return "egovSampleRegister"
	 * @exception Exception
	 */
	@GetMapping("/updateSampleView.do")
	public String updateSampleView(@RequestParam("selectedId") String id, @ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model) throws Exception {
		SampleVO sampleVO = new SampleVO();
		sampleVO.setId(id);
		// 변수명은 CoC 에 따라 sampleVO
		model.addAttribute(selectSample(sampleVO, searchVO));
		return "sample/egovSampleRegister";
	}

	/**
	 * 글을 조회한다.
	 * @param sampleVO - 조회할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return @ModelAttribute("sampleVO") - 조회한 정보
	 * @exception Exception
	 */
	public SampleVO selectSample(SampleVO sampleVO, @ModelAttribute("searchVO") SampleDefaultVO searchVO) throws Exception {
		return sampleService.selectSample(sampleVO);
	}

	/**
	 * 글을 수정한다.
	 * @param sampleVO - 수정할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@PostMapping("/updateSample.do")
	public String updateSample(@ModelAttribute("searchVO") SampleDefaultVO searchVO, SampleVO sampleVO, BindingResult bindingResult, Model model, SessionStatus status)
			throws Exception {

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
	 * @param sampleVO - 삭제할 정보가 담긴 VO
	 * @param searchVO - 목록 조회조건 정보가 담긴 VO
	 * @param status
	 * @return "forward:/egovSampleList.do"
	 * @exception Exception
	 */
	@PostMapping("/deleteSample.do")
	public String deleteSample(SampleVO sampleVO, @ModelAttribute("searchVO") SampleDefaultVO searchVO, Model model, SessionStatus status) throws Exception {
		sampleService.deleteSample(sampleVO);
		status.setComplete();
		
		model.addAttribute("searchCondition", sampleVO.getSearchCondition());
		model.addAttribute("searchKeyword", sampleVO.getSearchKeyword());
		model.addAttribute("pageIndex", sampleVO.getPageIndex());
		
		return "redirect:/egovSampleList.do";
	}

}
