package com.alfred.kitabalhuda.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentRemoteDialogBinding
import com.alfred.kitabalhuda.network.DialogButton
import com.alfred.kitabalhuda.network.DialogDef
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class RemoteDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentRemoteDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoteDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val json = arguments?.getString(ARG_DIALOG_JSON) ?: return
        val dialogDef = try {
            gson.fromJson(json, DialogDef::class.java)
        } catch (e: JsonSyntaxException) {
            Log.w(TAG, "Malformed dialog JSON in arguments", e)
            null
        } ?: return
        bind(dialogDef)
    }

    override fun getTheme(): Int = R.style.Theme_KitabAlHuda_BottomSheet

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bind(dialog: DialogDef) {
        binding.dialogTitle.text = dialog.title
        binding.dialogMessage.text = dialog.message

        val buttonCount = dialog.buttons.size
        if (buttonCount > MAX_BUTTONS) {
            Log.w(TAG, "Dialog '${dialog.id}' has $buttonCount buttons, showing only $MAX_BUTTONS")
        }
        val buttons = dialog.buttons.take(MAX_BUTTONS)
        for (btnDef in buttons) {
            val button = createButton(btnDef)
            binding.buttonContainer.addView(button)
        }
    }

    private fun createButton(btnDef: DialogButton): MaterialButton {
        val styleAttr = when (btnDef.style) {
            "outlined" -> com.google.android.material.R.attr.materialButtonOutlinedStyle
            "text" -> com.google.android.material.R.attr.borderlessButtonStyle
            else -> com.google.android.material.R.attr.materialButtonStyle
        }
        val button = MaterialButton(requireContext(), null, styleAttr)
        button.text = btnDef.text
        button.layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = resources.getDimensionPixelSize(R.dimen.button_margin_top)
        }

        button.setOnClickListener {
            handleAction(btnDef.action.type, btnDef.action.value)
        }

        return button
    }

    private fun handleAction(type: String, value: String?) {
        when (type) {
            "open_url" -> {
                val url = value ?: return
                val uri = Uri.parse(url)
                if (uri.scheme != "https") {
                    Log.w(TAG, "Blocked non-https URL: $url")
                    return
                }
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (_: Exception) {
                }
            }
            "dismiss" -> {
                dismiss()
            }
            else -> {
                Log.w(TAG, "Unknown action type: $type")
                dismiss()
            }
        }
    }

    companion object {
        private const val MAX_BUTTONS = 3
        private const val ARG_DIALOG_JSON = "dialog_json"
        private const val TAG = "RemoteDialogFragment"

        private val gson = Gson()

        fun newInstance(dialog: DialogDef): RemoteDialogFragment {
            val fragment = RemoteDialogFragment()
            val args = Bundle()
            args.putString(ARG_DIALOG_JSON, gson.toJson(dialog))
            fragment.arguments = args
            return fragment
        }
    }
}
