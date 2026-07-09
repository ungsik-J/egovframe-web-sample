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
    <link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/modern.css'/>"/>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
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

            $.ajax({
                type: "GET",
                url: "<c:url value='/egovSampleListAjax.do'/>",
                data: formData,
                dataType: "json",
                cache: false,
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
                                html += "  <td style='width: 60%;'><a href='javascript:void(0);' onclick='fn_detail("+JSON.stringify(item)+")'>" + item.name + "</a></td>";
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
        
        /* 모달 팝업 제어 함수 활성화 */
        function fn_detail(data) {
            // 폼 리셋 (이전 업로드 선택 파일 제거 목적)
            $("#detailForm")[0].reset();

            // 상세 정보 데이터 매핑 (input 태그의 val() 활용)
            $("#modalId").val(data.id);
            $("#modalName").val(data.name);
            $("#description").val(data.description || ""); 
            
            // 모달 오픈 클래스 토글
            $("#detailModal").addClass("active");
        }

        /* 모달 닫기 함수 */
        function fn_close_modal() {
            $("#detailModal").removeClass("active");
        }

        /* ✨ 멀티파트 데이터 전송 함수 (수정 버튼 클릭시 호출) */
        function fn_update_sample() {
            // HTML5 FormData 객체 생성 (Form 태그 자체를 인자로 전달)
            var form = $('#detailForm')[0];
            var formData = new FormData(form);

            // 데이터 유효성 검사 예시
            if(!$("#description").val().trim()) {
                alert("설명을 입력해 주세요.");
                $("#description").focus();
                return false;
            }

            if(confirm("수정하시겠습니까?")) {
                $.ajax({
                    type: "POST",
                    enctype: 'multipart/form-data', // 멀티파트 인코딩 설정
                    url: "<c:url value='/fileUploadAjax.do'/>", // ⚠️ 전자정부 서버 Controller 맵핑 URL에 맞게 수정하세요.
                    data: formData,
                    processData: false, // jQuery가 데이터를 Query String으로 변환하는 것을 방지
                    contentType: false, // jQuery가 content-type을 자동 설정하도록 방지 (Boundary 자동 생성)
                    cache: false,
                    success: function(data) {
                        // 서버 리턴 처리 예시 (일반적으로 JSON 성공 유무 반환)
                        alert("정상적으로 수정 및 파일 업로드가 완료되었습니다.");
                        fn_close_modal();
                        fn_select_list(); // 목록 새로고침
                    },
                    error: function(xhr, status, error) {
                        alert("업로드 및 수정 중 오류가 발생했습니다.");
                    }
                });
            }
        }
    </script>
</head>
<body>

    <div class="container">
        <h2 onclick="window.location.reload();"> 전자정부 표준 AJAX 리스트</h2>

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
                <h3 class="modal-title">상세 정보 및 수정</h3>
                <button type="button" class="modal-close-btn" onclick="fn_close_modal();">&times;</button>
            </div>
            
            <form id="detailForm" name="detailForm" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="info-row">
                        <div class="info-label">ID</div>
                        <div class="info-value">
                            <input type="text" id="modalId" name="id" readonly />
                        </div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">이름</div>
                        <div class="info-value">
                            <input type="text" id="modalName" name="name" readonly />
                        </div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">설명</div>
                        <div class="info-value">
                            <textarea id="description" name="description" class="modal-textarea" placeholder="설명을 입력해주세요."></textarea>
                        </div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">첨부파일</div>
                        <div class="info-value">
                            <div class="file-input-wrapper">
                                <input type="file" id="uploadFile" name="uploadFile" class="modal-file-input" />
                            </div>
                        </div>
                    </div>
                </div>
            </form>
            
            <div class="modal-footer">
                <button type="button" class="btn-secondary" onclick="fn_close_modal();">닫기</button>
                <button type="button" class="btn-primary" onclick="fn_update_sample();">수정</button>
            </div>
        </div>
    </div>

</body>
</html>