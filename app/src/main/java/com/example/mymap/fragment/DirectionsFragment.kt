package com.example.mymap.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.example.mymap.Contains.Companion.KEY_CODE_END
import com.example.mymap.Contains.Companion.KEY_CODE_START
import com.example.mymap.Contains.Companion.RESULT
import com.example.mymap.Contains.Companion.SEND_INT_KEY_CODE
import com.example.mymap.R
import com.example.mymap.activities.ChooseLocalActivity
import com.example.mymap.data.models.MyPlace
import com.example.mymap.databinding.BottomSheetFragmentBinding
import com.example.mymap.viewmodels.MapsViewModel
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.IOException

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
        obseverData()
    }

    private fun obseverData() {
        viewmodel.startPlace.observe(viewLifecycleOwner) {
            binding.tvStart.text = it.snippet
        }
        viewmodel.endPlace.observe(viewLifecycleOwner) {
            binding.tvEnd.text = it.snippet
        }
    }

    private fun setupDirectionButton() {
        binding.btnDirect.setOnClickListener {
            if (viewmodel.endPlace.value != null && viewmodel.startPlace.value != null) {
                // add 2 marker and zoom to bounds, call api draw line
                viewmodel.isStartDirect.value = true
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
                        R.id.start_search -> {
                            true
                        }
                        R.id.start_current_local -> {
                            getCurrentPlace()?.let {
                                viewmodel.startPlace.value = it
                            }
                            true
                        }
                        R.id.start_check_in_map -> {
                            val intent = Intent(requireActivity(), ChooseLocalActivity::class.java)
                            intent.putExtra(SEND_INT_KEY_CODE, 1)
                            startForResult.launch(intent)
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
                        R.id.start_search -> {

                            true
                        }
                        R.id.start_current_local -> {
                            getCurrentPlace()?.let {
                                viewmodel.endPlace.value = it
                            }
                            true
                        }
                        R.id.start_check_in_map -> {
                            val intent = Intent(requireActivity(), ChooseLocalActivity::class.java)
                            intent.putExtra(SEND_INT_KEY_CODE, 2)
                            startForResult.launch(intent)
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

    private fun getCurrentPlace(): MyPlace? {
        val local = viewmodel.lastKnownLocation.value
        if (local != null) {
            try {
                val address =
                    Geocoder(requireActivity()).getFromLocation(local.latitude, local.longitude, 1)
                if (address.isNotEmpty()) {
                    val snippet = address[0].getAddressLine(0)
                  val place = MyPlace(local.latitude,local.longitude,snippet = snippet)
                    return place
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}