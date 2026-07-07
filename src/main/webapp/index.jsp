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
	href="<c:url value='/css/egovframework/modern.css'/>" />
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>

<style>
:root {
	--primary: #4f46e5;
	--primary-dark: #4338ca;
	--primary-light: #eef2ff;
	--text-main: #111827;
	--text-sub: #6b7280;
	--bg-grad-1: #eef2ff;
	--bg-grad-2: #f5f3ff;
	--card-bg: #ffffff;
	--border: #e5e7eb;
}

* {
	box-sizing: border-box;
}

html, body {
	height: 100%;
	margin: 0;
	padding: 0;
}

body {
	font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI',
		sans-serif;
	background: linear-gradient(135deg, var(--bg-grad-1) 0%,
		var(--bg-grad-2) 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 100vh;
	color: var(--text-main);
}

.container {
	width: 100%;
	max-width: 800px;
	padding: 24px;
}

.card {
	background: var(--card-bg);
	border: 1px solid var(--border);
	border-radius: 20px;
	padding: 48px 40px;
	text-align: center;
	box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 20px 40px -10px
		rgba(79, 70, 229, 0.15);
	animation: fadeUp 0.5s ease-out;
}

@
keyframes fadeUp {from { opacity:0;
	transform: translateY(12px);
}

to {
	opacity: 1;
	transform: translateY(0);
}

}
.badge {
	display: inline-flex;
	align-items: center;
	gap: 6px;
	padding: 6px 14px;
	background: var(--primary-light);
	color: var(--primary);
	font-size: 13px;
	font-weight: 600;
	border-radius: 999px;
	margin-bottom: 20px;
	letter-spacing: 0.02em;
}

.badge .dot {
	width: 6px;
	height: 6px;
	border-radius: 50%;
	background: var(--primary);
}

h1 {
	font-size: 24px;
	font-weight: 900;
	margin: 0 0 8px;
	color: var(--text-main);
}

p.desc {
	font-size: 14.5px;
	color: var(--text-sub);
	margin: 0 0 32px;
	line-height: 1.6;
}

.btn-primary {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 8px;
	width: 100%;
	padding: 14px 20px;
	background: var(--primary);
	color: #fff;
	font-size: 15px;
	font-weight: 600;
	border-radius: 12px;
	text-decoration: none;
	transition: background 0.2s ease, transform 0.15s ease, box-shadow 0.2s
		ease;
	box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}

.btn-primary:hover {
	background: var(--primary-dark);
	transform: translateY(-1px);
	box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}

.btn-primary:active {
	transform: translateY(0);
}

.btn-primary svg {
	width: 18px;
	height: 18px;
	transition: transform 0.2s ease;
}

.btn-primary:hover svg {
	transform: translateX(3px);
}

.footer-note {
	margin-top: 24px;
	font-size: 12.5px;
	color: #9ca3af;
}
</style>
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

			<div class="footer-note">egovframe-web-sample</div>
		</div>
	</div>

	<script type="text/javascript">
        $(function() {

        });
    </script>
</body>
</html>
