package com.example.carepick.ui.location

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.adapter.LocationAdapter
import com.example.carepick.databinding.FragmentLocationSettingBinding
import com.example.carepick.network.RetrofitClient
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.activityViewModels
import com.example.carepick.network.KakaoRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KEY_SELECTED_ADDRESS = "key_selected_address"
private const val ARG_ADDRESS = "address"


class LocationSettingFragment : Fragment(R.layout.fragment_location_setting) {

    private var _binding: FragmentLocationSettingBinding? = null
    private val binding get() = _binding!!

    private var selectedSido: String? = null
    private var selectedSgg:  String? = null
    private var selectedUmd:  String? = null

    private enum class Mode { SEARCH, ADMIN, GPS }

    private val locationVM: LocationSharedViewModel by activityViewModels {
        LocationVMFactory(requireContext().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationSettingBinding.bind(view)

        // 뒤로가기 버튼
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 🔘 모드 토글 적용 (초기값: 검색)
        setupModeToggle(initial = Mode.SEARCH)


        // === 주소 검색 ===
        // 검색 리스트 & 페이징
        binding.recycler.adapter = adapter
        if (binding.recycler.layoutManager == null) {
            binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        }
        // 👇 중복 리스너 하나만 남기기
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= adapter.itemCount - 4) viewModel.loadMore()
            }
        })

        // IME 액션(검색)
        binding.searchEdit.setOnEditorActionListener { v, actionId, event ->
            val isImeSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterDown = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (isImeSearch || isEnterDown) {
                val q = v.text?.toString().orEmpty()
                viewModel.search(q, resetPage = true)
                true
            } else false
        }

        // 스크롤 페이징
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= adapter.itemCount - 4) {
                    Log.d("LocationSetting", "loadMore() triggered at last=$last")
                    viewModel.loadMore()
                }
            }
        })

        // 상태 수집
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.ui.collect { state ->
                Log.d("LocationSetting", "state loading=${state.loading} items=${state.items.size} error=${state.error}")
                adapter.submit(state.items)
            }
        }



        // === 행정구역 선택 ===
        // 행정구역 그리드 초기화
        binding.rvSidoGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = sidoAdapter
            setHasFixedSize(true)
        }
        binding.rvSggGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = sggAdapter
            setHasFixedSize(true)
        }
        binding.rvUmdGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = umdAdapter
            setHasFixedSize(true)
        }


        // 확인/취소
        binding.btnConfirmSelection.setOnClickListener {
            val addr = buildAddress()
            viewLifecycleOwner.lifecycleScope.launch {
                val pair = geocodeAddress(addr)
                if (pair != null) {
                    val (lat, lng) = pair
                    locationVM.setLocation(lat, lng, addr)

                    // 기존 result flow로 텍스트도 전달
                    val fm = requireActivity().supportFragmentManager
                    fm.setFragmentResult(KEY_SELECTED_ADDRESS, Bundle().apply {
                        putString(ARG_ADDRESS, addr)
                    })
                    fm.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "주소 좌표 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnCancelSelection.setOnClickListener {
            selectedUmd = null
            binding.confirmBar.show(false)
            binding.rvUmdGrid.show(false)
            binding.rvSggGrid.show(false)
            binding.rvSidoGrid.show(true)
        }


        // ✅ 행정구역 헤더 버튼: 루트로 돌아가기(시/도부터)
        binding.btnAdminRegion.setOnClickListener {
            binding.confirmBar.show(false)
            binding.rvUmdGrid.show(false)
            binding.rvSggGrid.show(false)
            binding.rvSidoGrid.show(true)
            if (sidoAdapter.itemCount == 0) fetchSidos()
        }




        // === GPS 사용 ===
        // GPS 섹션 버튼
        binding.btnRequestGps.setOnClickListener {
            when {
                isFineLocationGranted() -> {
                    // 이미 허용됨
                    fetchAndSetLocation()
                }
                shouldShowLocationRationale() -> {
                    // 이전에 거부했지만 "다시 묻지 않음"은 아님 — 설명 후 재요청
                    showRationaleAndRequest()
                }
                else -> {
                    // 최초 요청 or "다시 묻지 않음"을 선택했을 수도 있음 → 우선 요청
                    locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
        }

        // 상태 구독 (오타 수정 + viewLifecycleOwner 사용)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.ui.collect { state ->
//                binding.progress.isVisible = state.loading
//                binding.txtError.isVisible = state.error != null
//                binding.txtError.text = state.error
                adapter.submit(state.items)
//                binding.emptyView.isVisible = !state.loading && state.items.isEmpty() && state.error == null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun geocodeAddress(fullAddress: String): Pair<Double, Double>? = withContext(
        Dispatchers.IO) {
        runCatching {
            // Kakao REST API 예시(RetrofitClient에 address search가 있다고 가정)
            val res = KakaoRetrofitClient.kakaoService.searchAddress(fullAddress, page = 1, size = 1)
            val first = res.documents.firstOrNull()
            val lat = first?.y?.toDoubleOrNull()
            val lng = first?.x?.toDoubleOrNull()
            if (lat != null && lng != null) lat to lng else null
        }.getOrNull()
    }


    private fun View.show(show: Boolean) {
        visibility = if (show) View.VISIBLE else View.GONE
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            fetchAndSetLocation()
        } else {
            // 여기서 ‘다시 묻지 않음’ 여부 판단
            if (!shouldShowLocationRationale()) {
                // 사용자가 "다시 묻지 않음"을 체크했거나 정책상 rationale 표시 불가
                showGoToSettingsDialog()
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val sidoAdapter by lazy {
        SidoAdapter { sido ->
            selectedSido = sido.name
            Log.d(TAG, "Sido clicked: ${sido.name} (${sido.type})")
            fetchSggs(sido.name)
        }
    }



    private val sggAdapter by lazy {
        SggAdapter(
            currentSido = { selectedSido ?: "시/도" },
            onBackClick = {
                // SGG → SIDO
                binding.rvSggGrid.visibility = View.GONE
                binding.rvSidoGrid.visibility = View.VISIBLE
            },
            onItemClick = { sgg ->
                selectedSgg = sgg.name
                fetchUmds(sgg.name)
            }
        )
    }

    private val umdAdapter by lazy {
        UmdAdapter(
            currentSgg = { selectedSgg ?: "시/군/구" },
            onBackClick = {
                // UMD → SGG
                binding.rvUmdGrid.visibility = View.GONE
                binding.rvSggGrid.visibility = View.VISIBLE
            },
            onItemClick = { umd ->
                selectedUmd = umd.name
                showConfirmation()
            }
        )
    }




    private val viewModel by lazy { LocationViewModel(restKey = "4bde7a7235a24839479023ff8eb22347") }
    private val adapter = LocationAdapter { doc ->
        // 클릭 시 좌표/주소 사용 예시
        val lat = doc.y?.toDoubleOrNull()
        val lon = doc.x?.toDoubleOrNull()
        val road = doc.road_address?.address_name
        val jibun = doc.address?.address_name ?: doc.address_name
        // TODO: 선택 결과 처리 (예: 상세 페이지 이동, 지도 표시, 폼에 채우기 등)
    }

    private fun isFineLocationGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowLocationRationale(): Boolean =
        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun openAppSettings() {
        val intent = Intent (
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        startActivity(intent)
    }

    private fun showGoToSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("위치 권한이 필요해요")
            .setMessage("현재 위치로 자동 설정하려면 위치 권한을 허용해 주세요.\n설정 > 앱 > Carepick > 권한에서 ‘위치’를 허용으로 변경하세요.")
            .setPositiveButton("설정으로 이동") { _, _ -> openAppSettings() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showRationaleAndRequest() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("위치 권한 요청")
            .setMessage("현재 위치를 사용하여 주소를 자동으로 설정합니다. 권한을 허용해 주세요.")
            .setPositiveButton("허용") { _, _ ->
                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupModeToggle(initial: Mode) = with(binding) {
        // 토글 버튼 체크(초기)
        when (initial) {
            Mode.SEARCH -> toggleModes.check(R.id.btnModeSearch)
            Mode.ADMIN  -> toggleModes.check(R.id.btnModeAdmin)
            Mode.GPS    -> toggleModes.check(R.id.btnModeGps)
        }
        applyMode(initial)

        toggleModes.addOnButtonCheckedListener(
            MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@OnButtonCheckedListener
                val mode = when (checkedId) {
                    R.id.btnModeSearch -> Mode.SEARCH
                    R.id.btnModeAdmin  -> Mode.ADMIN
                    R.id.btnModeGps    -> Mode.GPS
                    else               -> Mode.SEARCH
                }
                applyMode(mode)
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndSetLocation() {
        val client = LocationServices.getFusedLocationProviderClient(requireContext())
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }

                // 카카오 역지오코딩으로 행정구역 얻기
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        // x = lon(경도), y = lat(위도) !!!
                        Log.d("KakaoAPI", "coord2region x(lon)=${loc.longitude}, y(lat)=${loc.latitude}")

                        var lon = loc.longitude
                        var lat = loc.latitude

                        if (!isInKorea(lat, lon)) {
                            lon = 127.0276
                            lat = 37.4979
                        }

                        KakaoRetrofitClient.kakaoService.getRegionByCoord(
                            lon = lon,
                            lat = lat
                        )
                    }.onSuccess { res ->
                        val best =
                            res.documents.firstOrNull { it.regionType == "B" } // 법정동 우선
                                ?: res.documents.firstOrNull()                  // 없으면 아무거나

                        if (best != null) {
                            val sido = best.region1DepthName
                            val sgg  = best.region2DepthName
                            val umd  = when (best.regionType) {
                                "B"  -> best.region3DepthName
                                "H"  -> best.region3DepthHName ?: best.region3DepthName
                                else -> best.region3DepthName ?: best.region3DepthHName
                            } ?: ""

                            // UI 반영
                            val full = listOfNotNull(sido, sgg, umd.ifBlank { null }).joinToString(" ")
                            binding.tvSelectedAddress.text = full
                            binding.confirmBar.show(true)

                            // 필요하면 ‘행정구역 모드’로 전환 + 선택 완료 UX
                            selectedSido = sido
                            selectedSgg = sgg
                            selectedUmd = umd
//                            binding.toggleModes.check(R.id.btnModeAdmin)
//                            showConfirmation()
                            binding.tvSelectedAddress.text = full
                            binding.confirmBar.show(true)
                        } else {
                            // 실패 시 좌표 문자열 표시
                            binding.tvSelectedAddress.text =
                                "위도 ${"%.5f".format(loc.latitude)}, 경도 ${"%.5f".format(loc.longitude)}"
                            binding.confirmBar.show(true)
                        }
                    }.onFailure { e ->
                        if (e is retrofit2.HttpException) {
                            val body = e.response()?.errorBody()?.string()
                            Log.e("KakaoAPI", "HTTP ${e.code()} body=$body")
                        } else {
                            Log.e("KakaoAPI", "call failed", e)
                        }
                        Log.e("LocationSetting", "Reverse geocoding 실패: ${e.message}", e)
                        binding.tvSelectedAddress.text =
                            "위도 ${"%.5f".format(loc.latitude)}, 경도 ${"%.5f".format(loc.longitude)}"
                        binding.confirmBar.show(true)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "위치 조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyMode(mode: Mode) = with(binding) {
        val isSearch = mode == Mode.SEARCH
        val isAdmin  = mode == Mode.ADMIN
        val isGps    = mode == Mode.GPS

        // 검색 섹션
        searchContainer.show(isSearch)

        // 행정구역 섹션 (Group로 한 번에)
        adminGroup.visibility = if (isAdmin) View.VISIBLE else View.GONE
        if (isAdmin) {
            // 루트 상태로 초기화
            confirmBar.show(false)
            rvUmdGrid.show(false)
            rvSggGrid.show(false)
            rvSidoGrid.show(true)
            if (sidoAdapter.itemCount == 0) fetchSidos()
        }

        // GPS 섹션
        gpsContainer.show(isGps)
    }

    private fun fetchSidos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = RetrofitClient.adminRegionService.getSidos(0, 30)
                sidoAdapter.submit(res.content)
            } catch (e: Exception) {
                Log.e("RetrofitTest", "요청 실패: ${e.message}", e)
            }
        }
    }

    private fun fetchSggs(sidoName: String) {
        binding.rvSidoGrid.show(false)
        binding.rvUmdGrid.show(false)
        binding.confirmBar.show(false)
        binding.rvSggGrid.show(true)
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { RetrofitClient.adminRegionService.getSggsBySido(sidoName, 0, 100) }
                .onSuccess { res -> sggAdapter.submit(res.content) }
                .onFailure {
                    binding.rvSggGrid.show(false)
                    binding.rvSidoGrid.show(true)
                }
        }
    }

    private fun fetchUmds(sggName: String) {
        binding.rvSggGrid.show(false)
        binding.confirmBar.show(false)
        binding.rvUmdGrid.show(true)
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { RetrofitClient.adminRegionService.getUmdsBySgg(sggName, 0, 100) }
                .onSuccess { res -> umdAdapter.submit(res.content) }
                .onFailure {
                    binding.rvUmdGrid.show(false)
                    binding.rvSggGrid.show(true)
                }
        }
    }


    private fun showConfirmation() {
        binding.rvSidoGrid.show(false)
        binding.rvSggGrid.show(false)
        binding.rvUmdGrid.show(false)
        binding.tvSelectedAddress.text = buildAddress()
        binding.confirmBar.show(true)
    }

    private fun buildAddress(): String {
        val a = listOfNotNull(selectedSido, selectedSgg, selectedUmd)
        return a.joinToString(" ")
    }

    private fun createSelectableButton(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(32, 20, 32, 20)
        textView.setBackgroundResource(R.drawable.chip_unselected)
        textView.setTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(12, 12, 12, 12)
        textView.layoutParams = params

        return textView
    }

    private fun isInKorea(lat: Double, lon: Double): Boolean {
        // 대략적인 한반도 경계 (느슨하게 잡음)
        val latOk = lat in 33.0..39.5
        val lonOk = lon in 124.0..132.0
        return latOk && lonOk
    }
}