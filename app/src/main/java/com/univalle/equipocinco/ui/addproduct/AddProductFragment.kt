package com.univalle.equipocinco.ui.addproduct

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
import com.univalle.equipocinco.databinding.FragmentAddProductBinding
import com.univalle.equipocinco.ui.home.ProductViewModel
import com.univalle.equipocinco.ui.home.ProductViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupInputFilters()
        setupTextWatchers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_addProductFragment_to_homeFragment)
        }
    }


    private fun setupInputFilters() {
        // Código producto: máximo 4 dígitos
        binding.etProductCode.filters = arrayOf(
            InputFilter.LengthFilter(4),
            InputFilter { source, start, end, dest, dstart, dend ->
                // Solo permite números
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )

        // Nombre artículo: máximo 40 caracteres
        binding.etProductName.filters = arrayOf(InputFilter.LengthFilter(40))

        // Precio: máximo 20 dígitos
        binding.etPrice.filters = arrayOf(
            InputFilter.LengthFilter(20),
            InputFilter { source, start, end, dest, dstart, dend ->
                // Solo permite números
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )

        // Cantidad: solo números
        binding.etQuantity.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                // Solo permite números
                for (i in start until end) {
                    if (!Character.isDigit(source[i])) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )
    }

    private fun setupTextWatchers() {
        // Limpiar error cuando el usuario empiece a escribir
        binding.etProductCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilProductCode.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etProductName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilProductName.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPrice.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilQuantity.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (validateFields()) {
                saveProduct()
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Validar código producto
        val code = binding.etProductCode.text.toString().trim()
        if (code.isEmpty()) {
            binding.tilProductCode.error = "El código es requerido"
            isValid = false
        } else if (code.length > 4) {
            binding.tilProductCode.error = "Máximo 4 dígitos"
            isValid = false
        }

        // Validar nombre artículo
        val name = binding.etProductName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilProductName.error = "El nombre es requerido"
            isValid = false
        } else if (name.length > 40) {
            binding.tilProductName.error = "Máximo 40 caracteres"
            isValid = false
        }

        // Validar precio
        val price = binding.etPrice.text.toString().trim()
        if (price.isEmpty()) {
            binding.tilPrice.error = "El precio es requerido"
            isValid = false
        } else if (price.length > 20) {
            binding.tilPrice.error = "Máximo 20 dígitos"
            isValid = false
        } else {
            try {
                val priceValue = price.toDouble()
                if (priceValue <= 0) {
                    binding.tilPrice.error = "El precio debe ser mayor a 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.tilPrice.error = "Precio inválido"
                isValid = false
            }
        }

        // Validar cantidad
        val quantity = binding.etQuantity.text.toString().trim()
        if (quantity.isEmpty()) {
            binding.tilQuantity.error = "La cantidad es requerida"
            isValid = false
        } else {
            try {
                val quantityValue = quantity.toInt()
                if (quantityValue <= 0) {
                    binding.tilQuantity.error = "La cantidad debe ser mayor a 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.tilQuantity.error = "Cantidad inválida"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveProduct() {
        binding.btnSave.isEnabled = false

        try {
            val code = binding.etProductCode.text.toString().trim().toInt()
            val name = binding.etProductName.text.toString().trim()
            val price = binding.etPrice.text.toString().trim().toDouble()
            val quantity = binding.etQuantity.text.toString().trim().toInt()

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val product = viewModel.getProductById(code).firstOrNull()

                    if (product != null) {
                        binding.tilProductCode.error = "Este código ya existe"
                        binding.btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "El producto con código $code ya existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val newProduct = Product(code, name, price, quantity)
                        viewModel.addProduct(newProduct)

                        Toast.makeText(
                            requireContext(),
                            "Producto guardado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        findNavController().navigate(R.id.action_addProductFragment_to_homeFragment)
                    }

                } catch (e: Exception) {
                    if (_binding != null) {
                        binding.btnSave.isEnabled = true
                        Toast.makeText(
                            requireContext(),
                            "Error al guardar el producto: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        } catch (e: NumberFormatException) {
            binding.btnSave.isEnabled = true
            Toast.makeText(
                requireContext(),
                "Error: Verifica los valores numéricos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun clearFields() {
        binding.etProductCode.text?.clear()
        binding.etProductName.text?.clear()
        binding.etPrice.text?.clear()
        binding.etQuantity.text?.clear()

        binding.tilProductCode.error = null
        binding.tilProductName.error = null
        binding.tilPrice.error = null
        binding.tilQuantity.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}