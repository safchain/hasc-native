package safchain.hasc.ui.settings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import safchain.hasc.databinding.FragmentSettingsBinding


class SettingsFragment: Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return root
        val endpoint = sharedPref.getString("endpoint", "")
        val endpointInputText = binding.endpointTextInput
        endpointInputText.setText(endpoint)

        val saveBtn = binding.saveButton
        saveBtn.setOnClickListener {
            with (sharedPref.edit()) {
                putString("endpoint", endpointInputText.text.toString())
                apply()

                Toast.makeText(context, "saved!", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }
}