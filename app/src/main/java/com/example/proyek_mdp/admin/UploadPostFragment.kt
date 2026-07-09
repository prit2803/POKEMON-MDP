package com.example.proyek_mdp.admin

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.R
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.viewmodel.UploadPostViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class UploadPostFragment : Fragment() {

    private val viewModel: UploadPostViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private val categories = listOf("Kartu Pokemon", "Makanan", "Lainnya")
    private var pickedImagePath: String? = null

    private lateinit var ivPreview: ImageView

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { copyImageToInternalStorage(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_post, container, false)

        ivPreview = view.findViewById(R.id.ivPreview)
        val btnPickImage = view.findViewById<Button>(R.id.btnPickImage)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val etStock = view.findViewById<EditText>(R.id.etStock)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, categories
        )

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val priceText = etPrice.text.toString().trim()
            val stockText = etStock.text.toString().trim()

            if (title.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                Toast.makeText(requireContext(), "Judul, harga, dan stok wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull()
            if (price == null) {
                Toast.makeText(requireContext(), "Harga tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stock = stockText.toIntOrNull()
            if (stock == null || stock < 0) {
                Toast.makeText(requireContext(), "Stok tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val post = Post(
                title = title,
                description = description,
                price = price,
                category = spinnerCategory.selectedItem as String,
                imagePath = pickedImagePath,
                stock = stock
            )

            viewModel.savePost(post)
        }
        
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Post berhasil disimpan", Toast.LENGTH_SHORT).show()
                (activity as? AdminActivity)?.loadFragment(ManagePostsFragment())
            }
        }
        
        viewModel.saveError.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), "Gagal menyimpan: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun copyImageToInternalStorage(sourceUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(sourceUri)
            val fileName = "post_${System.currentTimeMillis()}.jpg"
            val outputFile = File(requireContext().filesDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            pickedImagePath = outputFile.absolutePath
            ivPreview.setImageURI(Uri.fromFile(outputFile))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}