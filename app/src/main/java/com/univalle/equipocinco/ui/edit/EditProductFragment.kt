package com.univalle.equipocinco.ui.editproduct

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.univalle.equipocinco.R
import com.univalle.equipocinco.data.local.entity.Product
import com.univalle.equipocinco.databinding.FragmentEditProductBinding
import com.univalle.equipocinco.ui.home.ProductViewModel
import com.univalle.equipocinco.ui.home.ProductViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(requireContext())
    }

    private var productId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getInt("id") ?: -1
        setupToolbar()
        setupInputFilters()
        setupTextWatchers()
        loadProduct()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbarEdit.title = "Editar producto"
        binding.toolbarEdit.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbarEdit.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupInputFilters() {
        binding.etProductName.filters = arrayOf(InputFilter.LengthFilter(40))

        binding.etPrice.filters = arrayOf(
            InputFilter.LengthFilter(20),
            InputFilter { source, start, end, _, _, _ ->
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) return@InputFilter ""
                }
                null
            }
        )

        binding.etQuantity.filters = arrayOf(
            InputFilter.LengthFilter(4),
            InputFilter { source, start, end, _, _, _ ->
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) return@InputFilter ""
                }
                null
            }
        )
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateFields()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etProductName.addTextChangedListener(watcher)
        binding.etPrice.addTextChangedListener(watcher)
        binding.etQuantity.addTextChangedListener(watcher)
    }

    private fun validateFields() {
        val nameOk = binding.etProductName.text.toString().trim().isNotEmpty()
        val priceOk = binding.etPrice.text.toString().trim().isNotEmpty()
        val qtyOk = binding.etQuantity.text.toString().trim().isNotEmpty()

        binding.btnEdit.isEnabled = nameOk && priceOk && qtyOk
    }

    private fun loadProduct() {
        lifecycleScope.launch {
            viewModel.getProductById(productId).collect { product ->
                if (product != null) {
                    binding.etId.setText(product.id.toString())
                    binding.etProductName.setText(product.name)
                    binding.etPrice.setText(product.price.toString())
                    binding.etQuantity.setText(product.quantity.toString())
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            if (validateEdition()) updateProduct()
        }
    }

    private fun validateEdition(): Boolean {
        var valid = true

        val name = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim()

        if (name.isEmpty() || name.length > 40) valid = false
        if (price.isEmpty() || price.length > 20) valid = false
        if (quantity.isEmpty() || quantity.length > 4) valid = false

        return valid
    }

    private fun updateProduct() {
        binding.btnEdit.isEnabled = false

        try {
            val updated = Product(
                id = productId,
                name = binding.etProductName.text.toString().trim(),
                price = binding.etPrice.text.toString().trim().toDouble(),
                quantity = binding.etQuantity.text.toString().trim().toInt()
            )

            lifecycleScope.launch {
                viewModel.updateProduct(updated)

                Toast.makeText(
                    requireContext(),
                    "Producto actualizado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(R.id.action_editProductFragment_to_homeFragment)
            }

        } catch (e: Exception) {
            binding.btnEdit.isEnabled = true
            Toast.makeText(
                requireContext(),
                "Error al actualizar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
