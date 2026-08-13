package com.example.senior_on.data.source.health

/**
 * 병원 일정 입력 화면에서 사용하는 고정 진료과 카탈로그입니다.
 *
 * 진료과 목록은 서버에서 변경되는 사용자 데이터가 아니며 현재 API 명세에도
 * 별도의 진료과 조회 API가 없어 앱 리소스 성격의 정적 목록으로 관리합니다.
 */
object HospitalSpecialtyCatalogDataSource : HospitalSpecialtyDataSource {
    val specialties = listOf(
        "안과",
        "외과",
        "정형외과",
        "한의원",
        "신경과",
        "요양병원",
        "결핵과",
        "치과",
        "한방병원",
        "이비인후과",
        "재활의학과",
        "일반의원",
        "영상의학과",
        "산부인과",
        "종합병원",
        "신경과",
        "보건소",
        "정신건강의학과",
        "흉부외과",
        "성형외과",
        "비뇨기과",
        "피부과",
        "내과",
        "직접 작성"
    )

    override suspend fun getSpecialties(): List<String> = specialties
}
