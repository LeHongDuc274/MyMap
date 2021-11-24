package com.example.mymap.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.example.mymap.Contains.Companion.KEY_CODE_END
import com.example.mymap.Contains.Companion.KEY_CODE_START
import com.example.mymap.Contains.Companion.RESULT
import com.example.mymap.Contains.Companion.SEND_INT_KEY_CODE
import com.example.mymap.R
import com.example.mymap.activities.ChooseLocalActivity
import com.example.mymap.data.models.MyPlace
import com.example.mymap.databinding.BottomSheetFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectionsFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetFragmentBinding? = null
    private val binding get() = _binding!!
    var startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == KEY_CODE_START) {
                // There are no request codes
                val data: Intent? = result.data
                val value = data?.getSerializableExtra(RESULT) as MyPlace
              //  Log.e("data1", value.snippet)
                binding.tvStart.text = value.snippet
            } else if (result.resultCode == KEY_CODE_END) {
                val data: Intent? = result.data
                val value = data?.getSerializableExtra(RESULT) as MyPlace
              //  Log.e("data2", value.snippet)
                binding.tvEnd.text = value.snippet
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_option_start_local).setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.start_search -> {

                            true
                        }
                        R.id.start_current_local -> {

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
        view.findViewById<Button>(R.id.btn_option_end_local).setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.start_search -> {

                            true
                        }
                        R.id.start_current_local -> {

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
//
//    fun openActivityForResult() {
//
//    }


    companion object {
        const val REQUEST_START_LOCAL = 1
        const val REQUEST_END_LOCAL = 2
    }
}