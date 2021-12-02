package com.example.mymap.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.example.mymap.Contains.Companion.KEY_CODE_END
import com.example.mymap.Contains.Companion.KEY_CODE_START
import com.example.mymap.Contains.Companion.RESULT
import com.example.mymap.Contains.Companion.SEND_INT_KEY_CODE
import com.example.mymap.R
import com.example.mymap.activities.ChooseLocalActivity
import com.example.mymap.activities.MainActivity
import com.example.mymap.data.models.MyPlace
import com.example.mymap.databinding.BottomSheetFragmentBinding
import com.example.mymap.viewmodels.MapsViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectionsFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var viewmodel: MapsViewModel
    var startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == KEY_CODE_START) {
                val data: Intent? = result.data
                val value = data?.getSerializableExtra(RESULT) as MyPlace
                viewmodel.startPlace.value = value

            } else if (result.resultCode == KEY_CODE_END) {
                val data: Intent? = result.data
                val value = data?.getSerializableExtra(RESULT) as MyPlace
                binding.tvEnd.text = value.snippet
                viewmodel.endPlace.value = value
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewmodel = ViewModelProvider(requireActivity())[MapsViewModel::class.java]
        _binding = BottomSheetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenus()
        setupDirectionButton()
        setupSearchFunc()
        setupReverse()
        setupRadiolistener()
        obseverData()
    }

    private fun setupReverse() {
        binding.btnReverse.setOnClickListener {
            Toast.makeText(requireActivity(), viewmodel.reversePlace(), Toast.LENGTH_LONG).show()
        }
    }

    private fun obseverData() {
        viewmodel.startPlace.observe(viewLifecycleOwner) {
            binding.tvStart.text = it.snippet
        }
        viewmodel.endPlace.observe(viewLifecycleOwner) {
            binding.tvEnd.text = it.snippet
        }
        viewmodel.selectionStartPlaceMethod.observe(viewLifecycleOwner) {
            if (it == 3) {
                binding.edtStart.visibility = View.VISIBLE
                binding.btnStartOk.visibility = View.VISIBLE
            } else {
                binding.edtStart.visibility = View.GONE
                binding.btnStartOk.visibility = View.GONE
            }
        }
        viewmodel.selectionEndPlaceMethod.observe(viewLifecycleOwner) {
            if (it == 3) {
                binding.edtEnd.visibility = View.VISIBLE
                binding.btnEndOk.visibility = View.VISIBLE
            } else {
                binding.edtEnd.visibility = View.GONE
                binding.btnEndOk.visibility = View.GONE
            }
        }
    }

    private fun setupDirectionButton() {
        binding.btnDirect.setOnClickListener {
            if (viewmodel.endPlace.value != null && viewmodel.startPlace.value != null) {
                // add 2 marker and zoom to bounds, call api draw line
                viewmodel.isStartDirect.value = true
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra("start", viewmodel.startPlace.value)
                intent.putExtra("end", viewmodel.endPlace.value)
                intent.putExtra("mode", viewmodel.travelMode.value)
                startActivity(intent)
            } else {
                viewmodel.isStartDirect.value = false
                Toast.makeText(requireActivity(), "Nhập đủ thông tin", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupMenus() {
        binding.btnOptionStartLocal.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.start_current_local -> {
                            getCurrentPlace()?.let {
                                viewmodel.startPlace.value = it
                                viewmodel.selectionStartPlaceMethod.value = 1
                            }
                            true
                        }
                        R.id.start_check_in_map -> {
                            openActivityForResult()
                            true
                        }
                        R.id.start_search -> {
                            viewmodel.selectionStartPlaceMethod.value = 3
                            true
                        }
                        else -> false
                    }
                }
                inflate(R.menu.menu_option_start)
                show()
            }
        }
        binding.btnOptionEndLocal.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.start_current_local -> {
                            getCurrentPlace()?.let {
                                viewmodel.endPlace.value = it
                                viewmodel.selectionEndPlaceMethod.value = 1
                            }
                            true
                        }
                        R.id.start_check_in_map -> {
                            openActivityForResult()
                            true
                        }
                        R.id.start_search -> {
                            viewmodel.selectionEndPlaceMethod.value = 3
                            true
                        }
                        else -> false
                    }
                }
                inflate(R.menu.menu_option_start)
                show()
            }
        }
    }

    private fun setupRadiolistener() {
        binding.groupVehicles.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.rbtn_car -> {
                    viewmodel.travelMode.value = "car"
                }
                R.id.rbtn_circle -> {
                    viewmodel.travelMode.value = "scooter"
                }
                R.id.rbtn_wark -> {
                    viewmodel.travelMode.value = "pedestrian"
                }
            }
        }
    }

    private fun setupSearchFunc() {
        binding.btnStartOk.setOnClickListener {
            val query = binding.edtStart.text.toString()
            if (!query.isEmpty()) {
                getSearchPlace(query)?.let {
                    viewmodel.startPlace.value = it
                }
            }
        }
        binding.btnEndOk.setOnClickListener {
            val query = binding.edtEnd.text.toString()
            if (!query.isEmpty()) {
                getSearchPlace(query)?.let {
                    viewmodel.endPlace.value = it
                }
            }
        }
    }

    private fun getSearchPlace(query: String): MyPlace? {
        val address = viewmodel.getAddressFromName(query)
        //Geocoder(requireActivity()).getFromLocationName(query, 1)
        if (address != null) {
            val lat = address.latitude
            val long = address.longitude
            val snippet = address.getAddressLine(0)
            return MyPlace(lat, long, snippet = snippet)
        }
        return null
    }

    private fun getCurrentPlace(): MyPlace? {
        val local = viewmodel.lastKnownLocation.value
        if (local != null) {
            val address =
                viewmodel.getAdressFromLocation(LatLng(local.latitude, local.longitude))
            //  Geocoder(requireActivity()).getFromLocation(local.latitude, local.longitude, 1)
            if (address != null) {
                val snippet = address.getAddressLine(0)
                val place = MyPlace(local.latitude, local.longitude, snippet = snippet)
                return place
            }
        }
        return null
    }

    private fun openActivityForResult() {
        viewmodel.selectionStartPlaceMethod.value = 2
        val intent = Intent(requireActivity(), ChooseLocalActivity::class.java)
        intent.putExtra(SEND_INT_KEY_CODE, 1)
        viewmodel.lastKnownLocation.value?.let {
            intent.putExtra("lat", it.latitude)
            intent.putExtra("long", it.longitude)
        }
        startForResult.launch(intent)
    }
}