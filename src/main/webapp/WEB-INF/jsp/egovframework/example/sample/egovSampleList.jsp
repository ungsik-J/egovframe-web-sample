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

    <link rel="stylesheet" as="style" crossorigin
          href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.css" />
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
	<link href="css/egovframework/indexUI.css" rel="stylesheet" />
	<style>
	/* 화면 전체를 흐리게 덮는 레이어 */
#loadingBar {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: 9999; /* 최상단에 보이도록 설정 */
    display: flex;
    justify-content: center;
    align-items: center;
}

/* 동그라미 스피너 애니메이션 */
.spinner {
    width: 50px;
    height: 50px;
    border: 5px solid #f3f3f3;
    border-top: 5px solid #3498db;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
	</style>
</head>
<body>
<div id="toastStack"></div>
<div class="app-shell">

    <div class="app-header" onclick="window.location.reload();">
        <div class="mark">전</div>
        <div class="titles">
            <h1>전자정부 표준 샘플 목록</h1>
            <p>Runtime Environment · Sample Data Console</p>
        </div>
    </div>

    <form id="searchForm" name="searchForm" method="post" onsubmit="fn_select_list(); return false;">
        <input type="hidden" id="pageIndex" name="pageIndex" value="1" />
        <div class="filter-bar">
            <select name="searchCondition">
                <option value="0">ID</option>
                <option value="1">이름</option>
            </select>
            <div class="divider"></div>
            <input type="text" name="searchKeyword" placeholder="검색어를 입력해 주세요..." />
            <label class="toggle-chip">
                <input type="checkbox" id="checked_download" name="checked_download" />
                <span class="switch"></span>
                대용량 파일 생성
            </label>
            <button type="button" class="btn btn-primary" onclick="fn_select_list();">검색</button>
        </div>
    </form>

    <div class="table-card">
        <table class="data-table">
            <thead>
                <tr>
                    <th class="col-no">No</th>
                    <th class="col-id">ID</th>
                    <th>이름</th>
                    <th>파일</th>
                    <th>사용</th>
                </tr>
            </thead>
            <tbody id="listBody">
                <tr>
                    <td colspan="4">
                        <div class="empty-state">데이터를 불러오는 중입니다…</div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div id="pagingArea" class="pagination-wrap"></div>
</div>


<!-- ================= Detail / Edit Drawer ================= -->
<div id="detailModal" class="drawer-overlay">
    <div class="drawer-panel">
        <div class="drawer-header">
            <h3>상세 정보 및 수정</h3>
            <button type="button" class="drawer-close" onclick="fn_close_modal();">&times;</button>
        </div>
	
        <form id="detailForm" name="detailForm" enctype="multipart/form-data" style="display:contents;">
            <div class="drawer-body">
                <div class="field">
                    <label for="modalId">ID</label>
                    <input type="text" id="modalId" name="id" class="id-value" readonly />
                </div>
                <div class="field">
                    <label for="modalName">이름</label>
                    <input type="text" id="modalName" name="name" readonly />
                </div>
                <div class="field">
                    <label for="description">설명</label>
                    <textarea id="description" name="description" placeholder="설명을 입력해주세요."></textarea>
                </div>
                <div class="field">
                    <label for="uploadFile">첨부파일</label>
                    <div class="file-drop">
                        <input type="file" id="uploadFile" name="uploadFile" />
                    </div>
                </div>
                <div class="status-row">
                    <span class="label">사용 여부</span>
                    <div class="radio-segment" role="radiogroup" aria-label="사용 여부">
                        <label class="radio-segment-option">
                            <input type="radio" id="useYnY" name="useYn" value="Y" checked />
                            <span>사용</span>
                        </label>
                        <label class="radio-segment-option">
                            <input type="radio" id="useYnN" name="useYn" value="N" />
                            <span>미사용</span>
                        </label>
                    </div>
                </div>
            </div>
        </form>

        <div class="drawer-footer">
            <button type="button" class="btn btn-ghost" onclick="fn_close_modal();">닫기</button>
            <button type="button" class="btn btn-accent" onclick="fn_update_sample();">수정 저장</button>
        </div>
    </div>
</div>

<!-- ================= Custom confirm dialog ================= -->
<div id="confirmOverlay" class="confirm-overlay">
    <div class="confirm-box">
        <p id="confirmMessage">진행하시겠습니까?</p>
        <div class="confirm-actions">
            <button type="button" class="btn btn-ghost" id="confirmCancelBtn">취소</button>
            <button type="button" class="btn btn-primary" id="confirmOkBtn">확인</button>
        </div>
    </div>
</div>

<!-- ================= [NEW] 최신 트렌드 이미지 팝업 모달 마크업 ================= -->
<div id="imageLightboxModal" class="img-modal-overlay">
    <div class="img-modal-wrapper">
        <button type="button" class="img-modal-close" onclick="fn_close_img_modal();">&times;</button>
        <img id="lightboxImg" src="" alt="미리보기 이미지">
        <div id="lightboxCaption" class="img-modal-caption"></div>
    </div>
</div>

<!-- ================= loadingBar ================= -->
<div id="loadingBar" style="display: none;">
    <div class="spinner"></div>
</div>

<script type="text/javascript">
$(document).ready(function () {
    fn_select_list();

    /* 드로어 바깥(오버레이) 클릭 시 닫기 */
    $("#detailModal").on("click", function (e) {
        if (e.target.id === "detailModal") {
            fn_close_modal();
        }
    });

    /* 이미지 모달 바깥(오버레이) 클릭 시 닫기 */
    $("#imageLightboxModal").on("click", function (e) {
        if (e.target.id === "imageLightboxModal") {
            fn_close_img_modal();
        }
    });

    /* ESC 키로 모든 모달/드로어 통합 닫기 */
    $(document).on("keydown", function (e) {
        if (e.key === "Escape") {
            if ($("#imageLightboxModal").hasClass("active")) {
                fn_close_img_modal();
            } else if ($("#detailModal").hasClass("active")) {
                fn_close_modal();
            }
        }
    });

    $("#checked_download").on("change", function (e) {
        if (e.target.checked) {
        	$.ajax({
        	    type: "GET",
        	    url: "<c:url value='/egovSampleListAjaxDownload.do'/>",
        	    data: null,
        	    dataType: "json",
        	    cache: false,
        	    beforeSend: function() {
        	        // [원리] AJAX 요청이 시작되기 직전에 실행
        	        // 로딩바 표시 (예: 로딩 DIV를 보임)
        	        $('#loadingBar').show(); 
        	    },
        	    success: function (data) {
        	        console.log(data);
        	        showToast("다운로드 준비가 완료되었습니다.", "success");
        	        // 1. 브라우저 메모리에 가상 URL 생성
        	        const blob = new Blob([data.fileWriter]);
        	        const downloadUrl = window.URL.createObjectURL(blob);
        	        
        	        // 2. 가상의 <a> 태그 생성 및 클릭 이벤트 발생
        	        const a = document.createElement("a");
        	        a.href = downloadUrl;
        	        
        	        // 3. 파일명 지정 (서버에서 헤더로 주는 이름을 쓰거나, 직접 지정)
        	        a.download = data.createFileName //"sample_list.xlsx"; // 원하는 파일명과 확장자 입력
        	        
        	        document.body.appendChild(a);
        	        a.click(); // 다운로드 시작
        	        
        	        // 4. 다운로드 후 가상 링크 및 메모리 해제
        	        document.body.removeChild(a);
        	        window.URL.revokeObjectURL(downloadUrl);
        	        
        	    },
        	    error: function (xhr, status, error) {
        	        console.error(xhr, status, error);
        	        showToast("다운로드 처리 중 오류가 발생했습니다.", "error");
        	    },
        	    complete: function() {
        	        // [원리] success든 error든 요청이 끝나면 무조건 실행
        	        // 로딩바 숨김
        	    	setTimeout(() => {
        	    	    console.log("setTimeout-->>>>>");
        	    	    $('#loadingBar').hide();
        	    	}, 5000);
        	    }
        	});
        }
    });
});

/* ===================== Toast ===================== */
function showToast(message, type) {
    var $toast = $('<div class="toast"><span class="dot"></span><span></span></div>');
    if (type) { $toast.addClass(type); }
    $toast.find("span").eq(1).text(message);
    $("#toastStack").append($toast);
    requestAnimationFrame(function () { $toast.addClass("show"); });
    setTimeout(function () {
        $toast.removeClass("show");
        setTimeout(function () { $toast.remove(); }, 250);
    }, 3200);
}

/* ===================== Custom confirm (Promise 기반) ===================== */
function showConfirmDialog(message) {
    return new Promise(function (resolve) {
        $("#confirmMessage").text(message);
        var $overlay = $("#confirmOverlay").addClass("active");

        function cleanup(result) {
            $overlay.removeClass("active");
            $("#confirmOkBtn").off("click");
            $("#confirmCancelBtn").off("click");
            resolve(result);
        }
        $("#confirmOkBtn").on("click", function () { cleanup(true); });
        $("#confirmCancelBtn").on("click", function () { cleanup(false); });
    });
}

/* ===================== [NEW] 이미지 모달 제어 함수 ===================== */
function fn_close_img_modal() {
    $("#imageLightboxModal").removeClass("active");
    setTimeout(function() {
        $("#lightboxImg").attr("src", ""); // 리소스 초기화로 흔들림 방지
    }, 300);
}

function fn_fileClick(el, fileName) {
    console.log('클릭한 파일:', fileName);
    if(fileName){
        $.ajax({
            type: "GET",
            url: "<c:url value='/egovSampleImageView.do'/>",
            data: { fileName: fileName },
            dataType: "json",
            cache: false,
            success: function (data) {
                console.log(data);
                
                // ✅ Controller가 보내주는 데이터 필드명(예: imageBase64, fileType)에 따라 맞춰 처리합니다.
                if (data.imageBase64) {
                    var imgSrc = 'data:image/' + (data.fileType || 'png') + ';base64,' + data.imageBase64;
                    
                    $("#lightboxImg").attr("src", imgSrc);
                    $("#lightboxCaption").text(fileName);
                    
                    // 모달 활성화 및 바디 스크롤 차단(UX 향상)
                    $("#imageLightboxModal").addClass("active");
                    // $("body").css("overflow", "hidden");
                } else {
                    showToast("이미지 데이터가 올바르지 않습니다.", "error");
                }
            },
            error: function (xhr, status, error) {
                showToast("데이터를 불러오는 중 오류가 발생했습니다.", "error");
            }
        });
    }
}

/* ===================== AJAX 목록 조회 ===================== */
function fn_select_list() {
    var formData = $("#searchForm").serialize();

    $.ajax({
        type: "GET",
        url: "<c:url value='/egovSampleListAjax.do'/>",
        data: formData,
        dataType: "json",
        cache: false,
        success: function (data) {
        	console.log(data)
            if (data.result === "SUCCESS") {
                var list = data.resultList;
                var pagination = data.paginationInfo;
                var searchVO = data.searchVO;
                var html = "";

                if (list.length === 0) {
                    html += "<tr><td colspan='4'><div class='empty-state'><span class='icon'>&#128269;</span>조회된 데이터가 없습니다.</div></td></tr>";
                } else {
                    $.each(list, function (index, item) {
                        var rowNum = pagination.totalRecordCount + 1 - ((searchVO.pageIndex - 1) * searchVO.pageSize + (index + 1));

                        let file = (item.fileName) ? item.fileName.split("/").pop() : "";

                        /* 파일명 셀: 돋보기 아이콘 + 파일명 chip (클릭 시 미려한 팝업 모달 오픈) */
                        var fileCell;
                        if (file) {
                            fileCell =
                                "<span class='file-chip' onclick=\"fn_fileClick(this,'" + file + "')\" >" +
                                    "<svg class='icon-search' width='15' height='15' viewBox='0 0 24 24' fill='none' " +
                                        "stroke='currentColor' stroke-width='2.2' stroke-linecap='round' stroke-linejoin='round'>" +
                                        "<circle cx='11' cy='11' r='7'></circle>" +
                                        "<line x1='21' y1='21' x2='16.65' y2='16.65'></line>" +
                                    "</svg>" +
                                    "<span class='file-name-text mono'>" + file + "</span>" +
                                "</span>";
                        } else {
                            fileCell = "<span class='no-file-dash'>—</span>";
                        }

                        html += "<tr>";
                        html += "  <td class='col-no mono'>" + rowNum + "</td>";
                        html += "  <td class='col-id mono'>" + item.id + "</td>";
                        html += "  <td><a href='javascript:void(0);' class='row-link' onclick='fn_detail(" + JSON.stringify(item) + ")'>" + item.name + "</a></td>";
                        html += "  <td class='col-id' name='fileName'>" + fileCell + "</td>"
                        html += "  <td class='col-id' name='useYn'>" + item.useYn + "</td>";
                        html += "</tr>";
                    });
                }

                $("#listBody").html(html);
                fn_render_pagination(pagination);
            }
        },
        error: function (xhr, status, error) {
            showToast("데이터를 불러오는 중 오류가 발생했습니다.", "error");
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

    if (firstPage > 1) {
        pagingHtml += "<a href='#' class='nav-btn' onclick='fn_link_page(" + (firstPage - 1) + "); return false;'>이전</a>";
    }

    for (var i = firstPage; i <= lastPage; i++) {
        if (i === currentPage) {
            pagingHtml += "<strong>" + i + "</strong>";
        } else {
            pagingHtml += "<a href='#' onclick='fn_link_page(" + i + "); return false;'>" + i + "</a>";
        }
    }

    if (lastPage < totalPage) {
        pagingHtml += "<a href='#' class='nav-btn' onclick='fn_link_page(" + (lastPage + 1) + "); return false;'>다음</a>";
    }

    $("#pagingArea").html(pagingHtml);
}

/* 드로어 열기 (상세 정보 매핑) */
function fn_detail(data) {
    $("#detailForm")[0].reset();

    $("#modalId").val(data.id);
    $("#modalName").val(data.name);
    $("#description").val(data.description || "");

    var useYn = data.useYn === "N" ? "N" : "Y"; // 값이 없으면 기본 '사용'
    $("input[name='useYn'][value='" + useYn + "']").prop("checked", true);

    $("#detailModal").addClass("active");
}

/* 드로어 닫기 */
function fn_close_modal() {
    $("#detailModal").removeClass("active");
}

/* 멀티파트 데이터 전송 (수정 저장 버튼 클릭 시 호출) */
function fn_update_sample() {
    var form = $('#detailForm')[0];
    var formData = new FormData(form);

    if (!$("#description").val().trim()) {
        showToast("설명을 입력해 주세요.", "error");
        $("#description").focus();
        return false;
    }

    showConfirmDialog("수정하시겠습니까?").then(function (confirmed) {
        if (!confirmed) { return; }

        $.ajax({
            type: "POST",
            enctype: 'multipart/form-data',
            url: "<c:url value='/fileUploadAjax.do'/>", 
            data: formData,
            processData: false,
            contentType: false,
            cache: false,
            success: function (data) {
                if (data.resultCode == '0000') {
                    showToast("정상적으로 수정 및 파일 업로드가 완료되었습니다.", "success");
                    fn_close_modal();
                    fn_select_list();
                } else {
                    showToast(data.resultMsg || "처리 중 문제가 발생했습니다.", "error");
                }
            },
            error: function (xhr, status, error) {
                showToast("업로드 및 수정 중 오류가 발생했습니다.", "error");
            }
        });
    });
}
</script>

</body>
</html>