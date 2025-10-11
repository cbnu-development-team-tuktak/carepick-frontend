package com.example.carepick.ui.search.result.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.data.model.DoctorDetailsResponse
import com.example.carepick.data.model.HospitalDetailsResponse
import com.example.carepick.databinding.SearchListBinding
import com.example.carepick.ui.search.result.DoctorSearchListViewHolder
import com.example.carepick.ui.search.result.HospitalSearchListViewHolder
import com.example.carepick.data.model.SearchResultItem

// 검색 결과 화면에서 병원과 의사 정보에서 데이터를 읽어서 레이아웃에 바인딩하는 코드
class SearchResultListAdapter(
    // 병원과 의사 정보를 모두 담을 수 있는 SearchResultItem 객체 형태로 받을 것임을 선언
    private val activity: FragmentActivity,
    private val onItemClicked: (SearchResultItem) -> Unit // 람다 콜백 추가
) : ListAdapter<SearchResultItem, RecyclerView.ViewHolder>(SearchResultDiffCallback()) {

    // 특정 데이터가 병원인지, 의사 데이터인지 구분할 수 있도록 타입 설정
    companion object {
        private const val TYPE_HOSPITAL = 0
        private const val TYPE_DOCTOR = 1
        private const val TYPE_LOADING = 2
    }

    // ✅ 2. 로딩 상태를 저장할 내부 변수 추가
    private var isLoadingNextPage = false

    // ✅ 3. getItemCount 수정: 로딩 중일 때는 아이템 개수를 1 늘려줍니다.
    override fun getItemCount(): Int {
        return super.getItemCount() + if (isLoadingNextPage) 1 else 0
    }

    // ✅ 4. getItemViewType 수정: 마지막 위치이고 로딩 중이면 로딩 타입을 반환합니다.
    override fun getItemViewType(position: Int): Int {
        return if (isLoadingNextPage && position == super.getItemCount()) {
            TYPE_LOADING
        } else {
            when (getItem(position)) {
                is HospitalDetailsResponse -> TYPE_HOSPITAL
                is DoctorDetailsResponse -> TYPE_DOCTOR
                else -> throw IllegalArgumentException("Unknown type at position $position")
            }
        }
    }

    // ✅ 5. onCreateViewHolder 수정: 로딩 타입에 맞는 ViewHolder를 생성합니다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HOSPITAL -> {
                val binding = SearchListBinding.inflate(inflater, parent, false)
                HospitalSearchListViewHolder(binding)
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
        if (holder is LoadingViewHolder) {
            // 로딩 뷰홀더인 경우 아무 작업도 하지 않음
        } else {
            val item = getItem(position)
            when (item) {
                is HospitalDetailsResponse -> (holder as HospitalSearchListViewHolder).bind(item) { onItemClicked(item) }
                is DoctorDetailsResponse -> (holder as DoctorSearchListViewHolder).bind(item) { onItemClicked(item) }
            }
        }
    }

    // ✅ 7. 로딩 상태를 외부에서 제어할 수 있는 함수 추가
    fun setLoading(isLoading: Boolean) {
        val previousState = isLoadingNextPage
        isLoadingNextPage = isLoading
        if (previousState && !isLoading) {
            // 로딩이 끝났으면 로딩 아이템을 제거하기 위해 UI 갱신
            notifyItemRemoved(super.getItemCount())
        } else if (!previousState && isLoading) {
            // 로딩이 시작됐으면 로딩 아이템을 추가하기 위해 UI 갱신
            notifyItemInserted(super.getItemCount())
        }
    }

    // ✅ 8. 로딩 ViewHolder 클래스 추가
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ✅ DiffUtil.ItemCallback 구현 클래스 (리스트 변경을 효율적으로 처리)
    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResultItem>() {
        override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
            val oldId = if (oldItem is HospitalDetailsResponse) oldItem.id else (oldItem as? DoctorDetailsResponse)?.id
            val newId = if (newItem is HospitalDetailsResponse) newItem.id else (newItem as? DoctorDetailsResponse)?.id
            return oldId != null && oldId == newId
        }

        override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
            return oldItem == newItem
        }
    }
}