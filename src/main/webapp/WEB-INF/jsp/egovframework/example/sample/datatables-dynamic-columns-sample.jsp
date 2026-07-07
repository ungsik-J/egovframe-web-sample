<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>DataTables 동적 컬럼 생성 샘플</title>

<!-- jQuery -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>

<!-- DataTables CSS -->
<link rel="stylesheet" href="https://cdn.datatables.net/1.13.8/css/jquery.dataTables.min.css">

<!-- DataTables JS -->
<script src="https://cdn.datatables.net/1.13.8/js/jquery.dataTables.min.js"></script>

<style>
  body { font-family: Arial, sans-serif; margin: 40px; }
  h2 { margin-bottom: 10px; }
  .toolbar { margin-bottom: 15px; }
  .toolbar button { padding: 6px 14px; margin-right: 8px; cursor: pointer; }
  table.dataTable { width: 100% !important; }
  code { background: #f4f4f4; padding: 2px 6px; border-radius: 4px; }
</style>
</head>
<body>

<h2>동적 컬럼 생성 예제</h2>
<p>버튼을 눌러 <b>컬럼 구조 자체가 다른 데이터셋</b>으로 테이블을 다시 그립니다. (하드코딩된 컬럼 없음)</p>

<div class="toolbar">
  <button id="btnDataset1">데이터셋 1 (직원 정보)</button>
  <button id="btnDataset2">데이터셋 2 (상품 정보 - 컬럼 다름)</button>
  <button id="btnDataset3">데이터셋 3 (주문 정보 - 컬럼 다름)</button>
</div>

<!-- thead도 비워둠: 컬럼 자체를 JS가 만들어서 채움 -->
<table id="dynamicTable" class="display" style="width:100%">
  <thead></thead>
  <tbody></tbody>
</table>

<script>
$(document).ready(function () {

  // ------------------------------------------------------
  // 서로 다른 구조의 데이터셋 3종 (컬럼 개수/이름이 전부 다름)
  // ------------------------------------------------------
  const dataset1 = [
    { id: 1, name: '김민준', dept: '개발팀', position: '과장', salary: 65000000 },
    { id: 2, name: '이서연', dept: '마케팅팀', position: '대리', salary: 48000000 },
    { id: 3, name: '박도윤', dept: '영업팀', position: '부장', salary: 85000000 }
  ];

  const dataset2 = [
    { productId: 'P-001', productName: '무선 이어폰', category: '전자제품', price: 89000, stock: 120 },
    { productId: 'P-002', productName: '텀블러', category: '생활용품', price: 15000, stock: 300 },
    { productId: 'P-003', productName: '모니터', category: '전자제품', price: 259000, stock: 45 }
  ];

  const dataset3 = [
    { orderNo: 'ORD-2024-001', customer: '최지우', items: 3, totalAmount: 128000, status: '배송중', orderDate: '2024-05-01' },
    { orderNo: 'ORD-2024-002', customer: '정하은', items: 1, totalAmount: 45000, status: '결제완료', orderDate: '2024-05-03' }
  ];

  let table = null;

  // ------------------------------------------------------
  // 핵심 함수: 데이터 배열만 넘기면
  // 1) 첫 번째 객체의 key들을 뽑아서
  // 2) columns 배열을 자동 생성하고
  // 3) 기존 테이블을 완전히 파괴 후 재생성
  // ------------------------------------------------------
  function renderDynamicTable(data) {
    // 1) 데이터의 key 목록 추출 -> 컬럼 정의 자동 생성
    const keys = Object.keys(data[0]);

    const columns = keys.map(function (key) {
      return {
        data: key,
        title: formatTitle(key),   // 헤더명은 보기 좋게 변환
        render: function (value) {
          // 숫자이고 금액/가격/합계 관련 필드면 콤마 포맷
          if (typeof value === 'number' && /price|salary|amount/i.test(key)) {
            return value.toLocaleString('ko-KR');
          }
          return value;
        }
      };
    });

    // 2) 기존 DataTable 인스턴스가 있으면 완전히 제거
    if (table) {
      table.destroy();
      $('#dynamicTable').empty(); // thead/tbody 내용까지 초기화
      $('#dynamicTable').append('<thead></thead><tbody></tbody>');
    }

    // 3) columns + data 옵션으로 재생성
    table = $('#dynamicTable').DataTable({
      data: data,
      columns: columns,
      language: {
        search: "검색:",
        lengthMenu: "_MENU_ 개씩 보기",
        info: "총 _TOTAL_ 건 중 _START_ - _END_ 표시",
        infoEmpty: "데이터가 없습니다",
        paginate: { first: "처음", last: "마지막", next: "다음", previous: "이전" },
        zeroRecords: "일치하는 데이터가 없습니다",
        emptyTable: "테이블에 데이터가 없습니다"
      },
      pageLength: 5
    });
  }

  // camelCase 키를 사람이 읽기 좋은 한글/영문 헤더로 변환하는 매핑
  // (매핑에 없는 키는 그대로 camelCase를 보여줌 -> 완전 범용)
  const titleMap = {
    id: 'ID', name: '이름', dept: '부서', position: '직급', salary: '연봉',
    productId: '상품코드', productName: '상품명', category: '카테고리', price: '가격', stock: '재고',
    orderNo: '주문번호', customer: '고객명', items: '수량', totalAmount: '총액', status: '상태', orderDate: '주문일'
  };

  function formatTitle(key) {
    return titleMap[key] || key;
  }

  // ------------------------------------------------------
  // 버튼 이벤트: 데이터셋 교체 -> 컬럼도 함께 자동 교체
  // ------------------------------------------------------
  $('#btnDataset1').on('click', function () { renderDynamicTable(dataset1); });
  $('#btnDataset2').on('click', function () { renderDynamicTable(dataset2); });
  $('#btnDataset3').on('click', function () { renderDynamicTable(dataset3); });

  // 초기 로딩
  renderDynamicTable(dataset1);

});
</script>

</body>
</html>

