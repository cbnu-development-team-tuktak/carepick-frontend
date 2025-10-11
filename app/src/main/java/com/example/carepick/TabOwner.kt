package com.example.carepick

interface TabOwner {
    /**
     * 이 프래그먼트가 속한 하단 네비게이션 탭의 ID를 반환합니다.
     * 예: R.id.nav_search
     */
    fun getNavId(): Int
}