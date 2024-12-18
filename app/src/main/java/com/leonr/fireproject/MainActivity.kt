package com.leonr.fireproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.leonr.fireproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }
    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.button.setOnClickListener {
//            listarDados()
//            atualizarRemoverDados()
//            salvarDados()
//            cadastroUsuario()
//            logarUsuario()
            pesquisarDados()
        }

    }

    private fun pesquisarDados() {

        val refUsuario = bancoDados
            .collection("usuarios")
//            .whereEqualTo("nome","Leonardo")
//            .whereNotEqualTo("nome","Leonardo")
//            .whereIn("nome", listOf("Leonardo","Rosf"))
//            .whereNotIn("nome", listOf("Leonardo","Rosf"))
//            .whereArrayContains("conhecimentos", "kotlin")
//            .whereGreaterThan("idade", "30")
//            .whereGreaterThanOrEqualTo("idade","40")
//            .whereLessThan("idade","40")
//            .whereLessThanOrEqualTo("idade","40")
//            .whereGreaterThanOrEqualTo("idade", "36")
//            .whereLessThanOrEqualTo("idade", "46")
//            .orderBy("idade", Query.Direction.ASCENDING)
            .orderBy("idade", Query.Direction.DESCENDING)


        refUsuario.addSnapshotListener{ querySnapshot, eroo ->

            val listaDocuments = querySnapshot?.documents
            var listaResultado = ""
            listaDocuments?.forEach{documentSnapshot ->
                val dados = documentSnapshot?.data
                if(dados != null){
                    val nome = dados["nome"]
                    val idade = dados["idade"]


                    listaResultado += "nome $nome,idade $idade"

                }
                binding.textView.text = listaResultado
            }
        }

    }

    private fun saldarUsuario(
         nome: String, idade: String
    ){
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null){

            val dados = mapOf(
                "nome" to nome,
                "idade" to idade
            )

            bancoDados
                .collection("usuarios")
                .document(idUsuarioLogado)
                .set(dados)
        }
    }

    private fun listarDados() {

//        saldarUsuario("Jorel","22")
        val idUsuarioLogado = autenticacao.currentUser?.uid

        if(idUsuarioLogado != null) {
            val refUsuario = bancoDados
                .collection("usuarios")
//                .document(idUsuarioLogado)

            refUsuario.addSnapshotListener{ querySnapshot, eroo ->

                val listaDocuments = querySnapshot?.documents
                var listaResultado = ""
                listaDocuments?.forEach{documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if(dados != null){
                        val nome = dados["nome"]
                        val idade = dados["idade"]


                        listaResultado += "nome $nome,idade $idade"

                    }
                    binding.textView.text = listaResultado
                }
                /*val dados = documentSnapshot?.data
                if(dados != null){
                    val nome = dados["nome"]
                    val idade = dados["idade"]
                    val texto = "nome $nome,idade $idade "
                    binding.textView.text = texto
                }*/
            }

            /*refUsuario
                .get()
                .addOnSuccessListener {documentSnapshot ->
                    val dados = documentSnapshot.data
                    if(dados != null){
                        val nome = dados["nome"]
                        val idade = dados["idade"]
                        val texto = "nome $nome,idade $idade "
                        binding.textView.text = texto
                    }
                }*/

        }


    }

    private fun atualizarRemoverDados() {

        val dados = mapOf(
            "nome" to "leonardo",
            "idade" to "24"
        )
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null){
            val refUsuario = bancoDados
                .collection("usuarios")
//                .document("1")
//        refUsuario.set(dados)
            refUsuario
//            .update("nome", "Leonard")
//            .delete()
                .add(dados)
                .addOnSuccessListener {
                    exibirMensagem("Usuario atualizado com sucesso")
                }.addOnFailureListener{
                    exibirMensagem("Erro ao atualizar usuário")
                }
        }


    }

    private fun salvarDados() {

        val dados = mapOf(
            "nome" to "leonardo",
            "idade" to "22"
        )

        bancoDados
            .collection("usuarios")
            .document("1")
            .set(dados)
            .addOnSuccessListener {
                exibirMensagem("Usuario salvo com sucesso")
            }.addOnFailureListener{
                exibirMensagem("Erro ao Salvar")
            }

    }

    override fun onStart() {
        super.onStart()
        logarUsuario()
//        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {

//        autenticacao.signOut()
        val usuario = autenticacao.currentUser
        val id = usuario?.uid

        if (usuario != null){
            exibirMensagem("Usuario está logado com id: $id")
            startActivity(
                Intent(this, UploadImageActivity::class.java)
            )
        }else{
            exibirMensagem("Não tem usuário logado")
        }
    }

    private fun logarUsuario() {

        val email = "leonardo@gmail.com"
        val senha = "!@#1212"

        autenticacao.signInWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener { authResult ->
            binding.textView.text = "Sucesso ao logar o usuário"
            startActivity(
                Intent(this, PrincipalActivity::class.java)
            )
        }.addOnFailureListener {exception ->
            binding.textView.text = "Falha ao logar usuário ${exception.message }"

        }

    }

    private fun cadastroUsuario() {

        val email = "leonardo2@gmail.com"
        val senha = "!@#1212"
        val nome = "leonard"
        val idade = "22"

        autenticacao.createUserWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener { authResult ->

            val email = authResult.user?.email
            val id = authResult.user?.uid
            //Salvar mais dados do Usuario
                saldarUsuario(nome,idade)
//            exibirMensagem("Sucesso ao cadastrar o usuario: $id - $email")
            binding.textView.text = "Sucesso: $id - $email"
        }.addOnFailureListener{exception ->
            val erro = exception.message
            binding.textView.text = "Erro: $erro"
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, "texto", Toast.LENGTH_SHORT).show()
    }
}