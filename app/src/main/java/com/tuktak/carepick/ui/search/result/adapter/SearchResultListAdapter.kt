package com.tuktak.carepick.ui.search.result.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.R
import com.tuktak.carepick.data.model.DoctorDetailsResponse
import com.tuktak.carepick.data.model.HospitalDetailsResponse
import com.tuktak.carepick.data.model.LoadingItem
import com.tuktak.carepick.databinding.SearchListBinding
import com.tuktak.carepick.ui.search.result.doctor.DoctorSearchListViewHolder
import com.tuktak.carepick.ui.search.result.hospital.HospitalListViewHolder
import com.tuktak.carepick.data.model.SearchResultItem
import com.tuktak.carepick.ui.location.repository.UserLocation

// 검색 결과 화면에서 병원과 의사 정보에서 데이터를 읽어서 레이아웃에 바인딩하는 코드
class SearchResultListAdapter(
    // 병원과 의사 정보를 모두 담을 수 있는 SearchResultItem 객체 형태로 받을 것임을 선언
    private val activity: FragmentActivity,
    private val onItemClicked: (SearchResultItem) -> Unit // 람다 콜백 추가
) : ListAdapter<SearchResultItem, RecyclerView.ViewHolder>(SearchResultDiffCallback()) {

    // ✅ 1. 어댑터 내부에 사용자 위치를 저장할 변수를 만듭니다.
    private var userLocation: UserLocation? = null

    // 특정 데이터가 병원인지, 의사 데이터인지 구분할 수 있도록 타입 설정
    companion object {
        private const val TYPE_HOSPITAL = 0
        private const val TYPE_DOCTOR = 1
        private const val TYPE_LOADING = 2
    }

    override fun getItemViewType(position: Int): Int {
        // ✅ 리스트에 있는 아이템의 타입에 따라 뷰 타입을 결정 (훨씬 간단해짐)
        return when (getItem(position)) {
            is HospitalDetailsResponse -> TYPE_HOSPITAL
            is DoctorDetailsResponse -> TYPE_DOCTOR
            is LoadingItem -> TYPE_LOADING
        }
    }

    // ✅ 5. onCreateViewHolder 수정: 로딩 타입에 맞는 ViewHolder를 생성합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HOSPITAL -> {
                val binding = SearchListBinding.inflate(inflater, parent, false)
                HospitalListViewHolder(binding)
            }
            TYPE_DOCTOR -> {
                val binding = SearchListBinding.inflate(inflater, parent, false)
                DoctorSearchListViewHolder(binding)
            }
            TYPE_LOADING -> {
                val view = inflater.inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // ✅ 6. onBindViewHolder 수정: 로딩 ViewHolder는 아무것도 바인딩할 필요가 없습니다.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // ✅ getItemViewType과 마찬가지로 아이템 타입에 따라 분기
        when (val item = getItem(position)) {
            is HospitalDetailsResponse -> {
                // ✅ 2. 바인딩 시, 생성자가 아닌 내부 변수 userLocation을 전달합니다.
                (holder as HospitalListViewHolder).bind(item, userLocation) { onItemClicked(item) }
            }
            is DoctorDetailsResponse -> {
                (holder as DoctorSearchListViewHolder).bind(item, userLocation) { onItemClicked(item) }
            }
            is LoadingItem -> {
                // 로딩 아이템의 경우 아무것도 할 필요가 없습니다.
            }
        }
    }

    // ✅ 8. 로딩 ViewHolder 클래스 추가
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ✅ DiffUtil.ItemCallback 구현 클래스 (리스트 변경을 효율적으로 처리)
    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResultItem>() {
        override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
            return when {
                oldItem is LoadingItem && newItem is LoadingItem -> true // 로딩 아이템은 항상 같다고 취급
                oldItem is HospitalDetailsResponse && newItem is HospitalDetailsResponse -> oldItem.id == newItem.id
                oldItem is DoctorDetailsResponse && newItem is DoctorDetailsResponse -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
            return oldItem == newItem
        }
    }

    // ✅ 3. 외부(Fragment)에서 사용자 위치를 업데이트할 수 있는 함수를 추가합니다.
    fun updateUserLocation(newUserLocation: UserLocation?) {
        this.userLocation = newUserLocation
        // 위치가 갱신되었으므로, 현재 화면에 보이는 아이템들을 새로 그리도록 알려줍니다.
        notifyDataSetChanged() // 간단하지만 전체를 새로고침
        // 또는 notifyItemRangeChanged(0, itemCount) 로 더 효율적으로 처리 가능
    }
}