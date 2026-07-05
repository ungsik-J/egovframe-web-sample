<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form"   uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>전자정부 AJAX 샘플 - 모던 UI</title>
    
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    
    <style>
        /* Modern CSS Reset & Variables */
        :root {
            --primary-color: #4f46e5;
            --primary-hover: #4338ca;
            --bg-color: #f8fafc;
            --card-bg: #ffffff;
            --text-main: #0f172a;
            --text-muted: #64748b;
            --border-color: #e2e8f0;
            --table-th-bg: #f8fafc;
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
            --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
            --radius: 12px;
            --radius-sm: 6px;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: var(--bg-color);
            color: var(--text-main);
            line-height: 1.5;
            padding: 40px 20px;
            display: flex;
            justify-content: center;
        }

        /* 메인 컨테이너 구조 */
        .container {
            width: 100%;
            max-width: 800px;
            background-color: var(--card-bg);
            border-radius: var(--radius);
            box-shadow: var(--shadow-md);
            padding: 32px;
            border: 1px solid var(--border-color);
        }

        h2 {
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 24px;
            color: var(--text-main);
            letter-spacing: -0.025em;
        }

        /* 상단 검색 바 영역 (Flexbox 활용) */
        .search-container {
            display: flex;
            gap: 8px;
            margin-bottom: 24px;
            background: var(--bg-color);
            padding: 12px;
            border-radius: var(--radius);
            border: 1px solid var(--border-color);
        }

        .search-container select, 
        .search-container input[type="text"] {
            border: 1px solid var(--border-color);
            border-radius: var(--radius-sm);
            padding: 10px 14px;
            font-size: 0.95rem;
            color: var(--text-main);
            background-color: var(--card-bg);
            outline: none;
            transition: all 0.2s ease;
        }

        .search-container select {
            min-width: 100px;
            cursor: pointer;
        }

        .search-container input[type="text"] {
            flex-grow: 1;
        }

        .search-container select:focus,
        .search-container input[type="text"]:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
        }

        /* 세련된 플랫 버튼 스킨 */
        .btn-primary {
            background-color: var(--primary-color);
            color: white;
            border: none;
            border-radius: var(--radius-sm);
            padding: 10px 20px;
            font-size: 0.95rem;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.2s;
        }

        .btn-primary:hover {
            background-color: var(--primary-hover);
        }

        .btn-secondary {
            background-color: #f1f5f9;
            color: #334155;
            border: 1px solid var(--border-color);
            border-radius: var(--radius-sm);
            padding: 10px 20px;
            font-size: 0.95rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
        }

        .btn-secondary:hover {
            background-color: #e2e8f0;
        }

        /* 테이블 스크롤 및 반응형 래퍼 */
        .table-responsive {
            width: 100%;
            overflow-x: auto;
            border-radius: var(--radius-sm);
            border: 1px solid var(--border-color);
            margin-bottom: 24px;
            box-shadow: var(--shadow-sm);
        }

        /* 모던 데이터 테이블 컴포넌트 */
        .custom-table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            font-size: 0.95rem;
        }

        .custom-table th {
            background-color: var(--table-th-bg);
            color: var(--text-muted);
            font-weight: 600;
            padding: 14px 16px;
            border-bottom: 1px solid var(--border-color);
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .custom-table td {
            padding: 14px 16px;
            border-bottom: 1px solid var(--border-color);
            color: var(--text-main);
        }

        .custom-table tbody tr:last-child td {
            border-bottom: none;
        }

        .custom-table tbody tr {
            transition: background-color 0.15s ease;
        }
        .custom-table tbody tr:hover {
            background-color: rgba(241, 245, 249, 0.6);
        }

        .text-center { text-align: center; }
        
        .custom-table a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
        }

        .custom-table a:hover {
            text-decoration: underline;
        }

        /* 컴팩트 페이징 버튼 스타일 UI */
        .pagination-wrap {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 4px;
            margin-top: 8px;
        }

        .pagination-wrap a, 
        .pagination-wrap strong {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 36px;
            height: 36px;
            padding: 0 6px;
            font-size: 0.9rem;
            border-radius: var(--radius-sm);
            text-decoration: none;
            transition: all 0.2s;
        }

        .pagination-wrap a {
            color: var(--text-muted);
            border: 1px solid transparent;
        }

        .pagination-wrap a:hover {
            background-color: var(--border-color);
            color: var(--text-main);
        }

        .pagination-wrap strong {
            background-color: var(--primary-color);
            color: white;
            font-weight: 600;
        }

        .pagination-wrap .nav-btn {
            border: 1px solid var(--border-color);
            background-color: var(--card-bg);
            font-weight: 500;
        }

        /* =================================*********
           ✨ 모던 모달 팝업 CSS 스타일 추가 구역
        ============================================ */
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(15, 23, 42, 0.6); /* 흐린 다크 배경 배경 블러 효과 적용 */
            backdrop-filter: blur(4px);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            opacity: 0;
            visibility: hidden;
            transition: opacity 0.25s ease, visibility 0.25s ease;
        }

        /* 모달이 활성화되었을 때 클래스 */
        .modal-overlay.active {
            opacity: 1;
            visibility: visible;
        }

        .modal-container {
            background-color: var(--card-bg);
            width: 100%;
            max-width: 480px;
            border-radius: var(--radius);
            box-shadow: var(--shadow-lg);
            border: 1px solid var(--border-color);
            overflow: hidden;
            transform: scale(0.95);
            transition: transform 0.25s ease;
            padding: 24px;
        }

        .modal-overlay.active .modal-container {
            transform: scale(1);
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }

        .modal-title {
            font-size: 1.2rem;
            font-weight: 700;
            color: var(--text-main);
        }

        .modal-close-btn {
            background: none;
            border: none;
            font-size: 1.5rem;
            color: var(--text-muted);
            cursor: pointer;
            line-height: 1;
            padding: 4px;
        }
        .modal-close-btn:hover {
            color: var(--text-main);
        }

        .modal-body {
            margin-bottom: 24px;
            color: var(--text-main);
            font-size: 0.95rem;
        }

        /* 데이터 한 줄 표현 레이아웃 */
        .info-row {
            display: flex;
            padding: 10px 0;
            border-bottom: 1px dashed var(--border-color);
        }
        .info-row:last-child {
            border-bottom: none;
        }
        .info-label {
            width: 80px;
            font-weight: 600;
            color: var(--text-muted);
        }
        .info-value {
            flex: 1;
            color: var(--text-main);
        }

        .modal-footer {
            display: flex;
            justify-content: flex-end;
            gap: 8px;
        }
    </style>
    
    <script type="text/javascript">
        $(document).ready(function() {
            fn_select_list();

            /* 모달 바깥 영역(오버레이) 클릭 시 닫기 설정 */
            $("#detailModal").on("click", function(e) {
                if ($(e.target).hasClass("modal-overlay")) {
                    fn_close_modal();
                }
            });

            /* ESC 키 누르면 모달 닫기 */
            $(document).on("keydown", function(e) {
                if (e.key === "Escape" && $("#detailModal").hasClass("active")) {
                    fn_close_modal();
                }
            });
        });

        /* AJAX 목록 조회 함수 */
        function fn_select_list() {
            var formData = $("#searchForm").serialize();
            
            
            //console.log( paginationInfo )
            $.ajax({
                type: "GET",
                url: "<c:url value='/egovSampleListAjax.do'/>",
                data: formData,
                dataType: "json",
                success: function(data) {
                    if(data.result === "SUCCESS") {
                        var list = data.resultList;
                        var pagination = data.paginationInfo;
                        var searchVO = data.searchVO;
                        var html = "";
                        
                        if(list.length === 0) {
                            html += "<tr><td colspan='3' class='text-center' style='color: var(--text-muted); padding: 30px 0;'>조회된 데이터가 없습니다.</td></tr>";
                        } else {
                            $.each(list, function(index, item) {
                                var rowNum = pagination.totalRecordCount + 1 - ((searchVO.pageIndex - 1) * searchVO.pageSize + (index + 1));
                                
                                html += "<tr>";
                                html += "  <td class='text-center' style='width: 15%; color: var(--text-muted);'>" + rowNum + "</td>";
                                html += "  <td class='text-center' style='width: 25%; font-variant-numeric: tabular-nums;'>" + item.id + "</td>";
                                // fn_detail 함수 호출 시 아이디와 함께 이름을 인자로 전달하여 모달에 바로 바인딩 가능하도록 설계했습니다.
                                html += "  <td style='width: 60%;'><a href='javascript:void(0);' onclick='fn_detail(\""+ item.id +"\", \""+ item.name +"\")'>" + item.name + "</a></td>";
                                html += "</tr>";
                            });
                        }
                        
                        $("#listBody").html(html);
                        fn_render_pagination(pagination);
                    }
                },
                error: function(xhr, status, error) {
                    alert("데이터를 불러오는 중 오류가 발생했습니다.");
                }
            });
        }

        function fn_link_page(pageNo) {
            $("#pageIndex").val(pageNo);
            fn_select_list();
        }

        /* AJAX용 페이징 마크업 동적 주입 */
        function fn_render_pagination(pagination) {
            var pagingHtml = "";
            var firstPage = pagination.firstPageNoOnPageList;
            var lastPage = pagination.lastPageNoOnPageList;
            var totalPage = pagination.totalPageCount;
            var currentPage = pagination.currentPageNo;
            
            if(firstPage > 1) {
                pagingHtml += "<a href='#' class='nav-btn' onclick='fn_link_page(" + (firstPage - 1) + "); return false;'>이전</a>";
            }
            
            for(var i = firstPage; i <= lastPage; i++) {
                if(i === currentPage) {
                    pagingHtml += "<strong>" + i + "</strong>";
                } else {
                    pagingHtml += "<a href='#' onclick='fn_link_page(" + i + "); return false;'>" + i + "</a>";
                }
            }
            
            if(lastPage < totalPage) {
                pagingHtml += "<a href='#' class='nav-btn' onclick='fn_link_page(" + (lastPage + 1) + "); return false;'>다음</a>";
            }
            
            $("#pagingArea").html(pagingHtml);
        }
        
        /* 📌 모달 팝업 제어 함수 활성화 */
        function fn_detail(id, name) {
            // 상세 정보 데이터 매핑
            $("#modalId").text(id);
            $("#modalName").text(name);
            
            // 모달 오픈 클래스 토글
            $("#detailModal").addClass("active");
        }

        /* 모달 닫기 함수 */
        function fn_close_modal() {
            $("#detailModal").removeClass("active");
        }
    </script>
</head>
<body>

    <div class="container">
        <h2>전자정부 표준 AJAX 리스트</h2>

        <form id="searchForm" name="searchForm" method="post" onsubmit="fn_select_list(); return false;">
            <input type="hidden" id="pageIndex" name="pageIndex" value="1" />
            <div class="search-container">
                <select name="searchCondition">
                    <option value="0">ID</option>
                    <option value="1">이름</option>
                </select>
                <input type="text" name="searchKeyword" placeholder="검색어를 입력해 주세요..." />
                <button type="button" class="btn-primary" onclick="fn_select_list();">검색</button>
            </div>
        </form>

        <div class="table-responsive">
            <table class="custom-table">
                <thead>
                    <tr>
                        <th class="text-center">No</th>
                        <th class="text-center">ID</th>
                        <th>이름</th>
                    </tr>
                </thead>
                <tbody id="listBody">
                    <tr>
                        <td colspan="3" class="text-center" style="color: var(--text-muted); padding: 30px 0;">데이터를 로딩 중입니다...</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div id="pagingArea" class="pagination-wrap"></div>
    </div>


    <div id="detailModal" class="modal-overlay">
        <div class="modal-container">
            <div class="modal-header">
                <h3 class="modal-title">상세 정보</h3>
                <button type="button" class="modal-close-btn" onclick="fn_close_modal();">&times;</button>
            </div>
            
            <div class="modal-body">
                <div class="info-row">
                    <div class="info-label">ID</div>
                    <div class="info-value" id="modalId">-</div>
                </div>
                <div class="info-row">
                    <div class="info-label">이름</div>
                    <div class="info-value" id="modalName">-</div>
                </div>
            </div>
            
            <div class="modal-footer">
                <button type="button" class="btn-secondary" onclick="fn_close_modal();">닫기</button>
                <button type="button" class="btn-primary" onclick="alert('수정 기능 확장 가능');">수정</button>
            </div>
        </div>
    </div>

</body>
</html>