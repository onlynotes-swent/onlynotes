package com.github.onlynotesswent.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
  /**
   * Check if the device has an active internet connection.
   *
   * @param context The context to use to check for an active internet connection.
   * @return True if the device has an active internet connection, false otherwise.
   */
  fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    println("connectivityManager: $connectivityManager")
    val network = connectivityManager.activeNetwork ?: return false
    println("network: $network")
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    println("capabilities: $capabilities")
    println(
        "capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET): " +
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    println(
        "capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED): " +
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }
}
