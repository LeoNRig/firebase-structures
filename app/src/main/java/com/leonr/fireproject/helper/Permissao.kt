package com.leonr.fireproject.helper

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissao {

    companion object {

        fun requisitarPermissoes(activity: Activity, permissoes: List<String>, requestCode: Int){

            val permissoesNegadas = mutableListOf<String>()

            permissoes.forEach { permissao ->
                val temPermissão = ContextCompat.checkSelfPermission(
                    activity, permissao
                ) == PackageManager.PERMISSION_GRANTED
                if (!temPermissão)
                    permissoesNegadas.add(permissao)
            }

            if(permissoesNegadas.isNotEmpty()){
                
                ActivityCompat.requestPermissions(
                    activity, permissoes.toTypedArray(), requestCode
                )

            }

        }
    }

}