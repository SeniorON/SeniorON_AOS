package com.example.senior_on.data.repository

interface HospitalSpecialtyRepository {
    suspend fun getSpecialties(): List<String>
}

object MockHospitalSpecialtyRepository : HospitalSpecialtyRepository {
    val specialties = listOf(
        "직접 작성",
        "피부과",
        "내과",
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
        "비뇨기과"
    )

    override suspend fun getSpecialties(): List<String> = specialties
}
