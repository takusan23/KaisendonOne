package io.github.kaisendonone.Util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * モバイルデータ接続かかくにんする
 * */
internal fun checkMobileData(context: Context): Boolean {
    //今の接続状態を取得
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    //ろりぽっぷとましゅまろ以上で分岐
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    } else {
        return connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
    }
}
