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
package egovframework.example.sample.service;

/**
 * @Class Name : SampleVO.java
 * @Description : SampleVO Class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 @ --------- --------- ------------------------------- @
 *   2009.03.16 최초생성
 *
 * @author 개발프레임웍크 실행환경 개발팀
 * @since 2009. 03.16
 * @version 1.0
 * @see
 *
 *      Copyright (C) by MOPAS All right reserved.
 */
@SuppressWarnings("serial")
public class SampleVO extends SampleDefaultVO {

	private static long serialVersionUID = 1L;

	/** 아이디 */
	private String id;

	/** 이름 */
	private String name;

	/** 내용 */
	private String description;

	/** 사용여부 */
	private String useYn;

	/** 등록자 */
	private String regUser;

	/** 파일정보 */
	private String fileName;

	/** 상품정보 */
	private String prodNum;

	/** 금액 */
	private int amt;

	/**
	 * @return the serialversionuid
	 */
	public static synchronized long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @param serialversionuid the serialversionuid to set
	 */
	public static synchronized void setSerialversionuid(long serialversionuid) {
		serialVersionUID = serialversionuid;
	}

	/**
	 * @return the id
	 */
	public synchronized String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public synchronized void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public synchronized String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public synchronized void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public synchronized String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public synchronized void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the useYn
	 */
	public synchronized String getUseYn() {
		return useYn;
	}

	/**
	 * @param useYn the useYn to set
	 */
	public synchronized void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	/**
	 * @return the regUser
	 */
	public synchronized String getRegUser() {
		return regUser;
	}

	/**
	 * @param regUser the regUser to set
	 */
	public synchronized void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	/**
	 * @return the fileName
	 */
	public synchronized String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public synchronized void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the prodNum
	 */
	public synchronized String getProdNum() {
		return prodNum;
	}

	/**
	 * @param prodNum the prodNum to set
	 */
	public synchronized void setProdNum(String prodNum) {
		this.prodNum = prodNum;
	}

	/**
	 * @return the amt
	 */
	public synchronized int getAmt() {
		return amt;
	}

	/**
	 * @param amt the amt to set
	 */
	public synchronized void setAmt(int amt) {
		this.amt = amt;
	}

}
