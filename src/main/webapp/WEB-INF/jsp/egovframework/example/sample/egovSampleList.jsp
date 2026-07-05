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

        /* 테이블 내부 요소 제어 클래스 */
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
    </style>
    
    <script type="text/javascript">
        $(document).ready(function() {
            fn_select_list();
        });

        /* AJAX 목록 조회 함수 */
        function fn_select_list() {
            var formData = $("#searchForm").serialize();
            
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
                                html += "  <td style='width: 60%;'><a href='javascript:void(0);' onclick='fn_detail(\""+ item.id +"\")'>" + item.name + "</a></td>";
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

        /* AJAX용 페이징 마크업 동적 주입 (UI 클래스 반영 고도화) */
        function fn_render_pagination(pagination) {
            var pagingHtml = "";
            var firstPage = pagination.firstPageNoOnPageList;
            var lastPage = pagination.lastPageNoOnPageList;
            var totalPage = pagination.totalPageCount;
            var currentPage = pagination.currentPageNo;
            
            // [이전] 버튼 클래스화 (`nav-btn`)
            if(firstPage > 1) {
                pagingHtml += "<a href='#' class='nav-btn' onclick='fn_link_page(" + (firstPage - 1) + "); return false;'>이전</a>";
            }
            
            // 페이지 넘버 서식 최신화
            for(var i = firstPage; i <= lastPage; i++) {
                if(i === currentPage) {
                    pagingHtml += "<strong>" + i + "</strong>";
                } else {
                    pagingHtml += "<a href='#' onclick='fn_link_page(" + i + "); return false;'>" + i + "</a>";
                }
            }
            
            // [다음] 버튼 클래스화 (`nav-btn`)
            if(lastPage < totalPage) {
                pagingHtml += "<a href='#' class='nav-btn' onclick='fn_link_page(" + (lastPage + 1) + "); return false;'>다음</a>";
            }
            
            $("#pagingArea").html(pagingHtml);
        }
        
        function fn_detail(id) {
            alert("선택한 ID: " + id);
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

</body>
</html>