package com.geko.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.geko.myapplication.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val number = listOf("0x00002002", "0x00002000", "0x00000002")
        var iterator = number.iterator()

        //     Promerica Decimal(numberDecimal)     TYPE_NUMBER_FLAG_DECIMAL   TYPE_CLASS_NUMBER

        val filterArray = arrayOf(InputFilter.LengthFilter(4));
        binding.textviewFirst.setFilters(filterArray);

        binding.textviewFirst.addTextChangedListener(
            object : TextWatcher {
                var isFormatting = false
                val regex = Regex("[^0-9]")

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?)    {
                    if (isFormatting) return

                    isFormatting = true


//                    val userInput = p0.toString().filter { it.isDigit() }.take(3)

//                    val userInput = Regex("[0-9]{1,3}").findAll(p0.toString()).joinToString("", transform = MatchResult::value)

                    // IME only displays number but this regex takes into account the edge case where the user paste some non numeric values
                    val userInput = p0.toString().replace(Regex("[^0-9]{1,3}[%]?"), "")

                    if (userInput.isNotEmpty()) {
                        if (userInput.contains("%")) {
                            if (userInput.length == 1) {
                                binding.textviewFirst.setText("")
                            } else {
                                binding.textviewFirst.setText(userInput)
                                binding.textviewFirst.setSelection(userInput.length-1)
                            }
                        } else {
//                            val text = "$userInput%"
                            binding.textviewFirst.setText(text)
                            binding.textviewFirst.setSelection(text.length-1)
                        }
                    } else {
                        binding.textviewFirst.setText(userInput)
                    }

                    isFormatting = false
                }

            }
        )
        binding.buttonFirst.setOnClickListener {
            if (binding.textviewSc.text.isEmpty()) {
                if (iterator.hasNext()) {
                    val code = iterator.next()
                    binding.textviewFirst.inputType = Integer.decode(code)
                    binding.txType.text = code
                } else {
                    iterator = number.iterator()
                }
            } else {
                binding.textviewFirst.inputType = Integer.decode(binding.textviewSc.text.toString())
            }


//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}