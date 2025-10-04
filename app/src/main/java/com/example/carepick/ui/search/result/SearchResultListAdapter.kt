package com.example.carepick.ui.search.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.model.HospitalDetailsResponse

// 검색 결과 화면에서 병원과 의사 정보에서 데이터를 읽어서 레이아웃에 바인딩하는 코드
class SearchResultListAdapter(
    // 병원과 의사 정보를 모두 담을 수 있는 SearchResultItem 객체 형태로 받을 것임을 선언
    private val items: List<SearchResultItem>,
    private val activity: FragmentActivity,
    private val onItemClicked: (SearchResultItem) -> Unit // 람다 콜백 추가
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 특정 데이터가 병원인지, 의사 데이터인지 구분할 수 있도록 타입 설정
    companion object {
        private const val TYPE_HOSPITAL = 0
        private const val TYPE_DOCTOR = 1
    }

    override fun getItemCount(): Int = items.size

    // items의 각각의 데이터에 대해 병원 데이터인지, 의사 데이터인지를 보고 타입을 정해준다
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {

            // HospitalDetailResponse 형태로 넘어왔다면 병원 타입이다
            is HospitalDetailsResponse -> TYPE_HOSPITAL
            // DoctorDetailResponse 형태로 넘어왔다면 의사 타입나다
            is DoctorDetailsResponse -> TYPE_DOCTOR

            // 그 외에는 예외처리
            else -> throw IllegalArgumentException("Unknown type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // 뷰 타입에 따라 다른 동작 수행
        return when (viewType) {
            // 뷰 타입이 병원인 경우
            TYPE_HOSPITAL -> {
                // search_list.xml 레이아웃을 바인딩한다
                val binding = SearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                // 병원 뷰홀더를 호출한다
                HospitalSearchListViewHolder(binding)
            }
            // 뷰 타입이 의사인 경우
            TYPE_DOCTOR -> {
                // search_list.xml 레이아웃을 바인딩한다
                val binding = SearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                // 의사 뷰홀더를 호출한다
                DoctorSearchListViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        // ViewHolder에 클릭 리스너와 데이터를 함께 전달
        when (item) {
            is HospitalDetailsResponse -> (holder as HospitalSearchListViewHolder).bind(item, onItemClicked)
            is DoctorDetailsResponse -> (holder as DoctorSearchListViewHolder).bind(item, onItemClicked)
        }
    }
}