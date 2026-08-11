package com.example.senior_on.ui.common.homebutton

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeButtonActionsPolicyTest {
    @Test
    fun manufacturerDefaultAppLabelsAreRecognizedWithoutPackageHardcoding() {
        assertTrue(matchesDefaultAppLabel("CALCULATOR", "계산기"))
        assertTrue(matchesDefaultAppLabel("MEMO", "Samsung Notes"))
        assertTrue(matchesDefaultAppLabel("MEMO", "Keep 메모"))
        assertTrue(matchesDefaultAppLabel("TIMER", "시계"))
        assertTrue(matchesDefaultAppLabel("INTERNET", "삼성 인터넷"))
    }

    @Test
    fun ordinaryDeepLinkAppsAreNotMistakenForInternetAppsByLabel() {
        assertFalse(matchesDefaultAppLabel("INTERNET", "네이버"))
        assertFalse(matchesDefaultAppLabel("INTERNET", "넷플릭스"))
    }

    @Test
    fun legacyActionAliasesUseTheSameLabelPolicy() {
        assertTrue(matchesDefaultAppLabel("NOTE", "메모"))
        assertTrue(matchesDefaultAppLabel("BROWSER", "Chrome"))
        assertTrue(matchesDefaultAppLabel("RECORDER", "음성 녹음"))
    }
}
