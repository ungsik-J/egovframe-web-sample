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
    <link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/modern.css'/>"/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
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