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
        setupSearchFunc()
        obseverData()
    }

    private fun obseverData() {
        viewmodel.startPlace.observe(viewLifecycleOwner) {
            binding.tvStart.text = it.snippet
        }
        viewmodel.endPlace.observe(viewLifecycleOwner) {
            binding.tvEnd.text = it.snippet
        }
        viewmodel.selectionStartPlaceMethod.observe(viewLifecycleOwner){
            if(it == 3){
                binding.edtStart.visibility = View.VISIBLE
                binding.btnStartOk.visibility = View.VISIBLE
            } else{
                binding.edtStart.visibility = View.GONE
                binding.btnStartOk.visibility = View.GONE
            }
        }
        viewmodel.selectionEndPlaceMethod.observe(viewLifecycleOwner){
            if(it == 3){
                binding.edtEnd.visibility = View.VISIBLE
                binding.btnEndOk.visibility = View.VISIBLE
            } else{
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
                            viewmodel.selectionStartPlaceMethod.value = 2
                            val intent = Intent(requireActivity(), ChooseLocalActivity::class.java)
                            intent.putExtra(SEND_INT_KEY_CODE, 1)
                            startForResult.launch(intent)
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
                            viewmodel.selectionEndPlaceMethod.value = 2
                            val intent = Intent(requireActivity(), ChooseLocalActivity::class.java)
                            intent.putExtra(SEND_INT_KEY_CODE, 2)
                            startForResult.launch(intent)
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
   private fun setupSearchFunc(){
        binding.btnStartOk.setOnClickListener {
            val query = binding.edtStart.text.toString()
            if(!query.isEmpty()){
                getSearchPlace(query)?.let {
                    viewmodel.startPlace.value = it
                }
            }
        }
       binding.btnEndOk.setOnClickListener {
           val query = binding.edtEnd.text.toString()
           if(!query.isEmpty()){
               getSearchPlace(query)?.let {
                   viewmodel.endPlace.value = it
               }
           }
       }
    }
    private fun getSearchPlace(query : String):MyPlace?{
       try {
            val address = Geocoder(requireActivity()).getFromLocationName(query, 1)
            if (address.isNotEmpty()) {
                val lat = address[0].latitude
                val long = address[0].longitude
                val snippet = address[0].getAddressLine(0)
                return MyPlace(lat,long,snippet= snippet)
            }
        } catch (e:IOException){
            e.printStackTrace()
        }
        return null
    }
    private fun getCurrentPlace(): MyPlace? {
        val local = viewmodel.lastKnownLocation.value
        if (local != null) {
            try {
                val address =
                    Geocoder(requireActivity()).getFromLocation(local.latitude, local.longitude, 1)
                if (address.isNotEmpty()) {
                    val snippet = address[0].getAddressLine(0)
                    val place = MyPlace(local.latitude, local.longitude, snippet = snippet)
                    return place
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}