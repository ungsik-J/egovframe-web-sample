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

<style>
/* ===================== Design tokens ===================== */
:root{
    --bg:#F3F5F9;
    --surface:#FFFFFF;
    --ink:#1B2534;
    --ink-soft:#45526B;
    --muted:#8792A2;
    --border:#E4E8EE;

    --primary:#1D3557;
    --primary-600:#16283F;
    --primary-soft:#E8EDF5;

    --accent:#0F9D8C;
    --accent-soft:#E1F5F2;
    --accent-600:#0C8477;

    --warn:#E8873A;
    --warn-soft:#FDEEDF;

    --danger:#D6473F;
    --danger-soft:#FBEAE9;

    --radius-sm:8px;
    --radius-md:14px;
    --radius-lg:22px;

    --shadow-sm:0 1px 2px rgba(27,37,52,.06);
    --shadow-md:0 10px 28px rgba(27,37,52,.09);
    --shadow-lg:0 24px 64px rgba(20,28,42,.28);

    --ease:cubic-bezier(.22,1,.36,1);
}

*{ box-sizing:border-box; }
html,body{
    margin:0; padding:0;
    background:var(--bg);
    color:var(--ink);
    font-family:'Pretendard', -apple-system, sans-serif;
    -webkit-font-smoothing:antialiased;
}
.mono{ font-family:'JetBrains Mono', monospace; font-variant-numeric:tabular-nums; }

a{ color:inherit; text-decoration:none; }
button{ font-family:inherit; }
:focus-visible{ outline:2px solid var(--accent); outline-offset:2px; border-radius:6px; }

/* ===================== Shell ===================== */
.app-shell{
    max-width:1040px;
    margin:0 auto;
    padding:40px 24px 80px;
}

.app-header{
    display:flex;
    align-items:center;
    gap:14px;
    margin-bottom:28px;
    cursor:pointer;
}
.app-header .mark{
    width:38px; height:38px;
    border-radius:11px;
    background:linear-gradient(155deg, var(--primary), var(--primary-600));
    display:flex; align-items:center; justify-content:center;
    color:#fff; font-weight:700; font-size:15px;
    box-shadow:var(--shadow-sm);
    flex-shrink:0;
}
.app-header .titles h1{
    margin:0; font-size:19px; font-weight:700; letter-spacing:-.01em; color:var(--ink);
}
.app-header .titles p{
    margin:2px 0 0; font-size:12.5px; color:var(--muted);
}

/* ===================== Filter capsule ===================== */
.filter-bar{
    display:flex;
    align-items:stretch;
    gap:0;
    background:var(--surface);
    border:1px solid var(--border);
    border-radius:999px;
    padding:6px;
    box-shadow:var(--shadow-sm);
    margin-bottom:18px;
    flex-wrap:wrap;
}
.filter-bar select{
    border:none;
    background:transparent;
    font-size:13.5px;
    font-weight:600;
    color:var(--ink-soft);
    padding:0 14px;
    border-radius:999px;
    cursor:pointer;
}
.filter-bar select:hover{ background:var(--bg); }
.filter-bar .divider{
    width:1px; background:var(--border); margin:8px 2px;
}
.filter-bar input[type="text"]{
    flex:1;
    min-width:160px;
    border:none;
    background:transparent;
    font-size:14px;
    padding:10px 14px;
    color:var(--ink);
}
.filter-bar input[type="text"]::placeholder{ color:var(--muted); }
.filter-bar input[type="text"]:focus-visible{ outline:none; }

.btn{
    display:inline-flex; align-items:center; justify-content:center; gap:6px;
    font-size:13.5px; font-weight:600;
    padding:10px 18px;
    border-radius:999px;
    border:none;
    cursor:pointer;
    transition:transform .15s var(--ease), background .15s var(--ease), box-shadow .15s var(--ease);
    white-space:nowrap;
}
.btn:active{ transform:scale(.97); }
.btn-primary{ background:var(--primary); color:#fff; }
.btn-primary:hover{ background:var(--primary-600); }
.btn-accent{ background:var(--accent); color:#fff; }
.btn-accent:hover{ background:var(--accent-600); }
.btn-ghost{ background:transparent; color:var(--ink-soft); border:1px solid var(--border); }
.btn-ghost:hover{ background:var(--bg); }
.btn-danger-ghost{ background:transparent; color:var(--danger); border:1px solid var(--danger-soft); }
.btn-danger-ghost:hover{ background:var(--danger-soft); }

.toggle-chip{
    display:flex; align-items:center; gap:8px;
    padding:8px 14px;
    border-radius:999px;
    color:var(--ink-soft);
    font-size:13px; font-weight:600;
    cursor:pointer;
    user-select:none;
}
.toggle-chip input{ display:none; }
.toggle-chip .switch{
    width:32px; height:18px; border-radius:999px;
    background:var(--border);
    position:relative;
    transition:background .18s var(--ease);
    flex-shrink:0;
}
.toggle-chip .switch::after{
    content:"";
    position:absolute; top:2px; left:2px;
    width:14px; height:14px; border-radius:50%;
    background:#fff; box-shadow:var(--shadow-sm);
    transition:transform .18s var(--ease);
}
.toggle-chip input:checked + .switch{ background:var(--accent); }
.toggle-chip input:checked + .switch::after{ transform:translateX(14px); }

/* ===================== Table card ===================== */
.table-card{
    background:var(--surface);
    border:1px solid var(--border);
    border-radius:var(--radius-lg);
    box-shadow:var(--shadow-sm);
    overflow:hidden;
}
table.data-table{
    width:100%;
    border-collapse:collapse;
}
.data-table thead th{
    text-align:left;
    font-size:11.5px;
    font-weight:700;
    letter-spacing:.06em;
    text-transform:uppercase;
    color:var(--muted);
    padding:16px 20px;
    border-bottom:1px solid var(--border);
    background:#FAFBFD;
}
.data-table tbody td{
    padding:15px 20px;
    font-size:14px;
    border-bottom:1px solid var(--border);
    color:var(--ink);
}
.data-table tbody tr{ transition:background .12s var(--ease); }
.data-table tbody tr:hover{ background:#FAFBFD; }
.data-table tbody tr:last-child td{ border-bottom:none; }
.data-table .col-no{ width:15%; color:var(--muted); }
.data-table .col-id{ width:25%; }
.data-table .row-link{
    color:var(--primary);
    font-weight:600;
}
.data-table .row-link:hover{ color:var(--accent-600); text-decoration:underline; }
.empty-state{
    padding:56px 20px;
    text-align:center;
    color:var(--muted);
    font-size:13.5px;
}
.empty-state .icon{ font-size:26px; display:block; margin-bottom:10px; }

/* ===================== Pagination ===================== */
.pagination-wrap{
    display:flex; align-items:center; justify-content:center; gap:4px;
    margin-top:22px;
    flex-wrap:wrap;
}
.pagination-wrap a, .pagination-wrap strong{
    min-width:32px; height:32px;
    display:inline-flex; align-items:center; justify-content:center;
    border-radius:999px;
    font-size:13px; font-weight:600;
    color:var(--ink-soft);
    padding:0 8px;
}
.pagination-wrap a:hover{ background:var(--primary-soft); color:var(--primary); }
.pagination-wrap strong{ background:var(--primary); color:#fff; }
.pagination-wrap a.nav-btn{ color:var(--muted); font-weight:700; }

/* ===================== Drawer (detail/edit) ===================== */
.drawer-overlay{
    position:fixed; inset:0;
    background:rgba(20,28,42,.42);
    backdrop-filter:blur(2px);
    opacity:0; pointer-events:none;
    transition:opacity .22s var(--ease);
    z-index:100;
    display:flex; justify-content:flex-end;
}
.drawer-overlay.active{ opacity:1; pointer-events:auto; }

.drawer-panel{
    width:min(420px, 92vw);
    height:100%;
    background:var(--surface);
    box-shadow:var(--shadow-lg);
    display:flex; flex-direction:column;
    transform:translateX(100%);
    transition:transform .28s var(--ease);
}
.drawer-overlay.active .drawer-panel{ transform:translateX(0); }

.drawer-header{
    display:flex; align-items:center; justify-content:space-between;
    padding:22px 24px;
    border-bottom:1px solid var(--border);
    flex-shrink:0;
}
.drawer-header h3{ margin:0; font-size:16.5px; font-weight:700; }
.drawer-close{
    width:30px; height:30px; border-radius:8px;
    border:none; background:transparent;
    font-size:20px; line-height:1; color:var(--muted);
    cursor:pointer;
}
.drawer-close:hover{ background:var(--bg); color:var(--ink); }

.drawer-body{
    padding:22px 24px;
    overflow-y:auto;
    flex:1;
    display:flex; flex-direction:column; gap:18px;
}
.field{ display:flex; flex-direction:column; gap:7px; }
.field label{ font-size:12.5px; font-weight:700; color:var(--muted); letter-spacing:.02em; }
.field input[type="text"], .field textarea{
    border:1px solid var(--border);
    border-radius:var(--radius-sm);
    padding:10px 13px;
    font-size:14px;
    font-family:inherit;
    color:var(--ink);
    background:var(--surface);
    transition:border-color .15s var(--ease);
}
.field input[readonly]{ background:#FAFBFD; color:var(--ink-soft); }
.field input:focus-visible, .field textarea:focus-visible{
    outline:none; border-color:var(--accent);
    box-shadow:0 0 0 3px var(--accent-soft);
}
.field textarea{ resize:vertical; min-height:90px; line-height:1.55; }
.field .id-value{ font-family:'JetBrains Mono', monospace; font-size:13px; }

.file-drop{
    border:1.5px dashed var(--border);
    border-radius:var(--radius-sm);
    padding:16px;
    display:flex; align-items:center; gap:10px;
    font-size:13px; color:var(--muted);
    transition:border-color .15s var(--ease), background .15s var(--ease);
}
.file-drop:hover{ border-color:var(--accent); background:var(--accent-soft); }
.file-drop input[type="file"]{ font-size:12.5px; width:100%; color:var(--ink-soft); }

.status-row{
    display:flex; align-items:center; justify-content:space-between;
    padding:12px 14px;
    background:var(--bg);
    border-radius:var(--radius-sm);
}
.status-row .label{ font-size:13px; font-weight:600; color:var(--ink-soft); }

.radio-segment{
    display:inline-flex;
    background:var(--surface);
    border:1px solid var(--border);
    border-radius:999px;
    padding:3px;
    gap:2px;
}
.radio-segment-option{
    position:relative;
    cursor:pointer;
}
.radio-segment-option input{
    position:absolute;
    opacity:0;
    width:100%; height:100%;
    margin:0;
    cursor:pointer;
}
.radio-segment-option span{
    display:flex; align-items:center; justify-content:center;
    min-width:64px;
    padding:7px 14px;
    border-radius:999px;
    font-size:13px; font-weight:600;
    color:var(--muted);
    transition:background .15s var(--ease), color .15s var(--ease);
}
.radio-segment-option input:checked + span{
    background:var(--accent);
    color:#fff;
}
.radio-segment-option input:checked[value="N"] + span{
    background:var(--danger);
}
.radio-segment-option input:focus-visible + span{
    outline:2px solid var(--accent);
    outline-offset:2px;
}
.radio-segment-option:hover input:not(:checked) + span{
    background:var(--bg);
    color:var(--ink-soft);
}

.drawer-footer{
    display:flex; gap:10px; justify-content:flex-end;
    padding:18px 24px;
    border-top:1px solid var(--border);
    flex-shrink:0;
}

/* ===================== Confirm dialog ===================== */
.confirm-overlay{
    position:fixed; inset:0;
    background:rgba(20,28,42,.5);
    display:flex; align-items:center; justify-content:center;
    z-index:200;
    opacity:0; pointer-events:none;
    transition:opacity .18s var(--ease);
}
.confirm-overlay.active{ opacity:1; pointer-events:auto; }
.confirm-box{
    width:min(340px, 88vw);
    background:var(--surface);
    border-radius:var(--radius-md);
    box-shadow:var(--shadow-lg);
    padding:24px;
    transform:scale(.94) translateY(6px);
    transition:transform .18s var(--ease);
}
.confirm-overlay.active .confirm-box{ transform:scale(1) translateY(0); }
.confirm-box p{ margin:0 0 18px; font-size:14.5px; color:var(--ink); line-height:1.5; }
.confirm-actions{ display:flex; gap:8px; justify-content:flex-end; }

/* ===================== Toast ===================== */
#toastStack{
    position:fixed; top:20px; right:20px; z-index:300;
    display:flex; flex-direction:column; gap:10px;
}
.toast{
    background:var(--ink);
    color:#fff;
    font-size:13.5px; font-weight:500;
    padding:13px 18px;
    border-radius:var(--radius-sm);
    box-shadow:var(--shadow-md);
    display:flex; align-items:center; gap:10px;
    transform:translateX(120%);
    transition:transform .28s var(--ease);
    max-width:320px;
}
.toast.show{ transform:translateX(0); }
.toast.success{ background:var(--accent-600); }
.toast.error{ background:var(--danger); }
.toast .dot{ width:6px; height:6px; border-radius:50%; background:currentColor; flex-shrink:0; opacity:.8; }

@media (max-width: 600px){
    .app-shell{ padding:24px 16px 60px; }
    .filter-bar{ border-radius:var(--radius-md); }
    .filter-bar input[type="text"]{ order:3; flex-basis:100%; }
}

@media (prefers-reduced-motion: reduce){
    *{ transition-duration:.01ms !important; animation-duration:.01ms !important; }
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
                파일 포함 다운로드
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
                </tr>
            </thead>
            <tbody id="listBody">
                <tr>
                    <td colspan="3">
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

<script type="text/javascript">
$(document).ready(function () {
    fn_select_list();

    /* 드로어 바깥(오버레이) 클릭 시 닫기 */
    $("#detailModal").on("click", function (e) {
        if (e.target.id === "detailModal") {
            fn_close_modal();
        }
    });

    /* ESC 키로 드로어 닫기 */
    $(document).on("keydown", function (e) {
        if (e.key === "Escape" && $("#detailModal").hasClass("active")) {
            fn_close_modal();
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
                success: function (data) {
                	console.log( data )
                    showToast("다운로드 준비가 완료되었습니다.", "success");
                },
                error: function (xhr, status, error) {
                    console.error(xhr, status, error);
                    showToast("다운로드 처리 중 오류가 발생했습니다.", "error");
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
            if (data.result === "SUCCESS") {
                var list = data.resultList;
                var pagination = data.paginationInfo;
                var searchVO = data.searchVO;
                var html = "";

                if (list.length === 0) {
                    html += "<tr><td colspan='3'><div class='empty-state'><span class='icon'>&#128269;</span>조회된 데이터가 없습니다.</div></td></tr>";
                } else {
                    $.each(list, function (index, item) {
                        var rowNum = pagination.totalRecordCount + 1 - ((searchVO.pageIndex - 1) * searchVO.pageSize + (index + 1));
					
                        let file = (item.fileName) ? item.fileName.split("/").pop() : ""; //(item.fileName) ? "Y" : "N";
						//file += item.fileName.split("/").pop();
                        html += "<tr>";
                        html += "  <td class='col-no mono'>" + rowNum + "</td>";
                        html += "  <td class='col-id mono'>" + item.id + "</td>";
                        html += "  <td><a href='javascript:void(0);' class='row-link' onclick='fn_detail(" + JSON.stringify(item) + ")'>" + item.name + "</a></td>";
                        html += "  <td class='col-id mono'>" + file + "</td>";
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
            url: "<c:url value='/fileUploadAjax.do'/>", // ⚠️ 전자정부 서버 Controller 맵핑 URL에 맞게 수정하세요.
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
