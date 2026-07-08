<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>egovframe-web-sample</title>
<link
	href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
	rel="stylesheet">
<link type="text/css" rel="stylesheet"
	href="<c:url value='/css/egovframework/index.css'/>" />
<link type="text/css" rel="stylesheet"
	href="<c:url value='/css/egovframework/modern.css'/>" />
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
</head>
<body>

	<div class="container">
		<div class="card">
			<span class="badge"><span class="dot"></span> eGovFrame Sample</span>

			<h1>샘플 페이지에 오신 것을 환영합니다</h1>
			<p class="desc">아래 버튼을 눌러 전자정부 표준프레임워크 샘플 목록으로 이동하세요.</p>

			<a class="btn-primary" href="/egovSampleList.do"> 샘플 목록으로 이동 <svg
					viewBox="0 0 24 24" fill="none" stroke="currentColor"
					stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
			</a>
			<br/>
			<br/>
			<a class="btn-primary" href="/sample/datatables-dynamic-columns-sample.do"> 동적 컬럼 생성 예제 <svg
					viewBox="0 0 24 24" fill="none" stroke="currentColor"
					stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12"></line>
                    <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
			</a>
			<div class="footer-note">egovframe-web-sample</div>
		</div>
	</div>

	<script type="text/javascript">
		$(function() {

		});
	</script>
</body>
</html>
