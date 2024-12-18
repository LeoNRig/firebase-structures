package com.leonr.fireproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.leonr.fireproject.databinding.ActivityUploadImageBinding
import com.leonr.fireproject.helper.Permissao
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class UploadImageActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityUploadImageBinding.inflate(layoutInflater)
    }

    private val armazenamento by lazy {
        FirebaseStorage.getInstance()
    }
    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private var uriImagemSelecionada: Uri? = null
    private var bitmapImagemSelecionada: Bitmap? = null

    private val abrirGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri ->
        if(uri != null){
            binding.imageSelecionada.setImageURI(uri)
            uriImagemSelecionada = uri
            Toast.makeText(this, "Imagem Selecionada", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Nenhuma imagem Selecionada", Toast.LENGTH_SHORT).show()
        }
    }

    private val abrirCamera = registerForActivityResult(
//        ActivityResultContracts.GetContent()
        ActivityResultContracts.StartActivityForResult()
    ) { resultadoActivity ->
//            if(resultadoActivity.resultCode == RESULT_OK){ }else{}
        bitmapImagemSelecionada = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resultadoActivity.data?.extras?.getParcelable("data", Bitmap::class.java)
        }else{
            resultadoActivity.data?.extras?.getParcelable("data",)

        }
        binding.imageSelecionada.setImageBitmap( bitmapImagemSelecionada )

    }

    private val permissoes = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("permissao_app","requestCode: $requestCode")

        permissions.forEachIndexed{indice, valor ->
            Log.i("permissao_app","permission:$indice $valor")
        }
        grantResults.forEachIndexed{indice, valor ->
            Log.i("permissao_app","concedida:$indice $valor")
        }

    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        solicitarPermissoes()

        /*Permissao.requisitarPermissoes(
            this, permissoes,100
        )*/

        binding.btnGaleria.setOnClickListener {
//            abrirGaleria.launch("image/*") //Mime Type
            if(temPermissaoGaleria){
                abrirGaleria.launch("image/*")
            }else{
                Toast.makeText(this, "Você precisa permitir acesso a galeria", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAdicionar.setOnClickListener {
            if(temPermissaoGaleria){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                abrirCamera.launch(intent)
            }else{
                Toast.makeText(this, "Você precisa permitir acesso a camera", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnUpload.setOnClickListener {
//            uploadGaleria()
            uploadCamera()
        }

        binding.btnRecuperar.setOnClickListener {
            recuperarImagemFirebase()
        }

    }

    private fun solicitarPermissoes() {

        val permissoesNegadas = mutableListOf<String>()
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (!temPermissaoCamera)
            permissoesNegadas.add(Manifest.permission.CAMERA)
        if (!temPermissaoGaleria)
            permissoesNegadas.add(Manifest.permission.READ_EXTERNAL_STORAGE)


        if(permissoesNegadas.isNotEmpty()){
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){ permissoes: Map<String, Boolean> ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA] ?: temPermissaoCamera
                temPermissaoGaleria = permissoes[Manifest.permission.READ_EXTERNAL_STORAGE] ?: temPermissaoGaleria

            }
            gerenciadorPermissoes.launch(permissoesNegadas.toTypedArray())
        }
    }

    private fun recuperarImagemFirebase(){

        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null){
            armazenamento
                .getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .downloadUrl
                .addOnSuccessListener { urlFirebase ->
//                    binding.imageRecuperada.setImageURI(urlFirebase)
                    Picasso.get()
                        .load(urlFirebase)
                        .into(binding.imageRecuperada)
                }
        }

    }

    private fun uploadCamera() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
//        val nomeImagem = UUID.randomUUID().toString()

        val outputStream = ByteArrayOutputStream()
        bitmapImagemSelecionada?.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            outputStream
        )

        if( bitmapImagemSelecionada != null && idUsuarioLogado != null){
            armazenamento
                .getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .putBytes( outputStream.toByteArray())
                .addOnSuccessListener { task ->
                    Toast.makeText(this, "Sucesso ao fazer upload", Toast.LENGTH_SHORT).show()
                    task.metadata?.reference?.downloadUrl?.addOnSuccessListener { urlFirebase ->
                        Toast.makeText(this, urlFirebase.toString() , Toast.LENGTH_SHORT).show()


                    }
                }.addOnFailureListener{erro->
                    Toast.makeText(this, "Erro ao fazer upload", Toast.LENGTH_SHORT).show()

                }
        }

    }
}