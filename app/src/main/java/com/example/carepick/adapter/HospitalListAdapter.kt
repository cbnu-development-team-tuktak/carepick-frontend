package com.example.carepick.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.carepick.R
import com.example.carepick.databinding.HospitalCardBinding
import com.example.carepick.model.HospitalListData
import com.example.carepick.ui.HospitalDetailFragment
import com.example.carepick.viewHolder.HospitalListViewHolder

// 카드뷰 어디에 어떤 정보가 들어갈지를 지정한다
// 또한 상세 페이지에 어떤 정보를 넘길지를 다룬다
class HospitalListAdapter(
    private val datas: MutableList<HospitalListData>,
    private val activity: FragmentActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        HospitalListViewHolder(HospitalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HospitalListViewHolder).binding

        // 병원 정보들을 한번에 받는 객체
        val hospitalData = datas[position]

        // 동적으로 추가할 진료과 정보들을 받는다
        val specialties = hospitalData.specialties ?: emptyList()

        // 병원 이름을 깔끔하게 다듬는다
        val cleanedName = hospitalData.name
            .replace(Regex("""^["'(【\[].*?["')】\]]\s*"""), "") // 앞 괄호/따옴표 제거

        // 카드에 데이터를 지정해서 넣는다
        binding.hospitalCardName.text = cleanedName
        binding.hospitalCardAddress.text = hospitalData.address
        // 카드에 이미지를 url을 통해서 집어넣는다
        Glide.with(binding.root)
            .load(hospitalData.imageUrl)
            .placeholder(R.drawable.sand_clock)
            .error(R.drawable.warning)
            .into(binding.hospitalPicture)

        // 진료과 목록에 따라 동적으로 진료과를 카드에 추가한다
        val specialtyAdapter = SpecialtyAdapter(specialties)
        binding.hospitalCardRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.hospitalCardRecyclerView.adapter = specialtyAdapter

        // 카드를 선택했을 때 다음의 동작들을 수행한다
        binding.root.setOnClickListener {
            navigateToDetail(hospitalData, cleanedName)
        }
    }


    // 카드를 선택했을 때 수행할 동작들을 담은 메소드
    private fun navigateToDetail(hospitalData: HospitalListData, cleanedName: String) {
        // 카드뷰를 클릭했을 때 넘어갈 Fragment를 객체에 저장
        val hospitalDetailFragment = HospitalDetailFragment()

        // 병원 상세 페이지에 전달할 데이터를 지정한다
        val bundle = Bundle().apply {
            putString("name", cleanedName)
            putString("phoneNumber", hospitalData.phoneNumber)
            putString("homepage", hospitalData.homepage)
            putString("address", hospitalData.address)
            putString("imageUrl", hospitalData.imageUrl)
            putString("operatingHours", hospitalData.operatingHours)

            hospitalData.latitude?.let { putDouble("latitude", it) }
            hospitalData.longitude?.let { putDouble("longitude", it) }

            hospitalData.specialties?.let {
                putStringArrayList("specialties", ArrayList(it))
            }

            hospitalData.doctors?.let {
                putStringArrayList("doctors", ArrayList(it))
            }

            hospitalData.additionalInfo?.let {
                putParcelable("additionalInfo", it)
            }
        }

        hospitalDetailFragment.arguments = bundle

        // 병원 상세 페이지 화면으로 넘어간다
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hospitalDetailFragment)
            .addToBackStack(null)
            .commit()
    }

}