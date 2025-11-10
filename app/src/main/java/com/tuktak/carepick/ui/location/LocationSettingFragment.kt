package com.tuktak.carepick.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.R
import com.tuktak.carepick.ui.location.adapter.AdminRegionAdapter
import com.tuktak.carepick.ui.location.adapter.LocationAdapter
import com.tuktak.carepick.databinding.FragmentLocationSettingBinding
import com.tuktak.carepick.ui.location.model.AddressDoc
import com.tuktak.carepick.ui.location.model.Sgg
import com.tuktak.carepick.ui.location.model.Sido
import com.tuktak.carepick.ui.location.model.Umd
import com.tuktak.carepick.ui.location.repository.UserLocation
import com.tuktak.carepick.ui.location.viewModel.LocationSettingViewModel
import com.tuktak.carepick.ui.location.viewModel.Mode
import com.tuktak.carepick.ui.location.viewModel.UserLocationViewModel
import com.tuktak.carepick.ui.location.viewModelFactory.UserLocationViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationSettingFragment : Fragment(R.layout.fragment_location_setting) {

    private var _binding: FragmentLocationSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationSettingViewModel by viewModels()

    // ✨ ViewModel 이름 및 타입 수정: LocationSharedViewModel -> UserLocationViewModel
    private val userLocationVM: UserLocationViewModel by activityViewModels {
        UserLocationViewModelFactory(requireContext().applicationContext)
    }

    // --- Adapters ---
    private val searchAdapter by lazy {
        LocationAdapter { doc -> onAddressDocClicked(doc) }
    }
    private val sidoAdapter by lazy {
        // AdminRegionAdapter가 Sido 타입만 다루도록 임시 어댑터를 만들거나,
        // AdminRegionAdapter를 수정하여 onBackClick을 nullable로 만들 수 있습니다.
        // 여기서는 간단하게 onBackClick에 아무것도 하지 않는 람다를 전달합니다.
        AdminRegionAdapter(
            onRegionClick = { region -> viewModel.selectSido(region as Sido) },
            onBackClick = { /* Sido 목록에서는 뒤로가기 없음 */ }
        )
    }
    private val sggAdapter by lazy {
        AdminRegionAdapter(
            onRegionClick = { region -> viewModel.selectSgg(region as Sgg) },
            onBackClick = { viewModel.goBackToSidoSelection() } // ✨ 뒤로가기 클릭 이벤트 연결
        )
    }

    private val umdAdapter by lazy {
        AdminRegionAdapter(
            onRegionClick = { region -> viewModel.selectUmd(region as Umd) },
            onBackClick = { viewModel.goBackToSggSelection() } // ✨ 뒤로가기 클릭 이벤트 연결
        )
    }

    // --- Permission Launcher ---
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fetchAndSetLocation()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showGoToSettingsDialog()
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationSettingBinding.bind(view)

        setupAdapters()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        binding.recycler.adapter = searchAdapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                if (last >= searchAdapter.itemCount - 4) {
                    viewModel.loadMoreSearch()
                }
            }
        })

        binding.rvSidoGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSidoGrid.adapter = sidoAdapter

        binding.rvSggGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSggGrid.adapter = sggAdapter

        binding.rvUmdGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvUmdGrid.adapter = umdAdapter
    }

    private fun setupClickListeners() = with(binding) {
        btnClose.setOnClickListener { parentFragmentManager.popBackStack() }

        toggleModes.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnModeSearch -> Mode.SEARCH
                R.id.btnModeAdmin -> Mode.ADMIN
                R.id.btnModeGps -> Mode.GPS
                else -> Mode.SEARCH
            }
            viewModel.setMode(mode)
        }

        searchEdit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                viewModel.searchAddress(v.text.toString())
                hideKeyboard()
                true
            } else false
        }

        btnAdminRegion.setOnClickListener { viewModel.clearAdminSelection() }
        btnCancelSelection.setOnClickListener { viewModel.clearAdminSelection() }
        btnConfirmSelection.setOnClickListener { viewModel.confirmAdminSelection() }

        btnRequestGps.setOnClickListener {
            when {
                isFineLocationGranted() -> fetchAndSetLocation()
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> showRationaleAndRequest()
                else -> locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progress.isVisible = state.isLoading
                state.errorMessage?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
                binding.searchContainer.isVisible = state.currentMode == Mode.SEARCH
                binding.adminGroup.isVisible = state.currentMode == Mode.ADMIN
                binding.gpsContainer.isVisible = state.currentMode == Mode.GPS
                searchAdapter.submit(state.searchResults)
                sidoAdapter.submitList(state.sidos)
                sggAdapter.submitList(state.sggs)
                umdAdapter.submitList(state.umds)
                binding.rvSidoGrid.isVisible = state.currentMode == Mode.ADMIN && state.selectedSido == null
                binding.rvSggGrid.isVisible = state.currentMode == Mode.ADMIN && state.selectedSido != null && state.selectedSgg == null
                binding.rvUmdGrid.isVisible = state.currentMode == Mode.ADMIN && state.selectedSgg != null && state.selectedUmd == null
                binding.confirmBar.isVisible = state.showConfirmBar
                if (state.showConfirmBar) {
                    val address = listOfNotNull(state.selectedSido, state.selectedSgg, state.selectedUmd).joinToString(" ")
                    binding.tvSelectedAddress.text = address
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.locationResultEvent.collect { location ->
                userLocationVM.setLocation(location)
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun onAddressDocClicked(doc: AddressDoc) {
        val lat = doc.y?.toDoubleOrNull()
        val lng = doc.x?.toDoubleOrNull()
        val road = doc.road_address?.address_name
        val jibun = doc.address?.address_name ?: doc.address_name

        if (lat != null && lng != null && (road != null || jibun != null)) {
            val finalAddress = road ?: jibun!!
            // ✨ 데이터 모델 통일: GeoLocation -> UserLocation
            userLocationVM.setLocation(UserLocation(address = finalAddress, lat = lat, lng = lng))
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "선택한 주소의 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndSetLocation() {
        val client = LocationServices.getFusedLocationProviderClient(requireContext())
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                viewModel.processGpsLocation(loc.latitude, loc.longitude)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "위치 조회 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- ✨ 생략되었던 권한 관련 헬퍼 함수들 전체 코드 ---
    private fun isFineLocationGranted(): Boolean = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowLocationRationale(): Boolean =
        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)

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

    private fun showGoToSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("위치 권한이 필요해요")
            .setMessage("현재 위치로 자동 설정하려면 위치 권한을 허용해 주세요.\n'설정'으로 이동하여 앱 권한에서 '위치'를 허용으로 변경해 주세요.")
            .setPositiveButton("설정으로 이동") { _, _ -> openAppSettings() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}