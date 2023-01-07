package com.example.myapplication

import android.content.Context
import android.content.res.Resources

class Settings {
    companion object {
        var REST_ADDRESS = "http://192.168.2.43:3000/"

        fun IconIdByName(resIdName: String, context: Context): Int {
            resIdName?.let {
                return context.resources.getIdentifier(it, "drawable", context.packageName)
            }
            throw Resources.NotFoundException()
        }

    }
}