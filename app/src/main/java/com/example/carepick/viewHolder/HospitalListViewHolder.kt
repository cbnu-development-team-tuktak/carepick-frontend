package com.example.carepick.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.HospitalCardBinding

// layout 폴더의 hospital_card.xml 레이아웃을 객체처럼 접근하여 속성(텍스트 등등)을 수정하기 위해 binding 시키는 부분
// HospitalListViewHolder는 병원 카드를 바인딩한 객체를 받고 리사이클러뷰를 반환한다
class HospitalListViewHolder(val binding: HospitalCardBinding) : RecyclerView.ViewHolder(binding.root)