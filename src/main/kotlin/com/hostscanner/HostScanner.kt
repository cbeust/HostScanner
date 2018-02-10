package com.beust.hostScanner

import com.hostscanner.LocalProperties
import com.squareup.okhttp.Credentials
import com.squareup.okhttp.Interceptor
import com.squareup.okhttp.OkHttpClient
import retrofit.Call
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.http.GET
import retrofit.http.Query
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class HostScanner {
    fun connect(hostName: String, portNumber: Int): Result {
        val result =
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(hostName, portNumber), 1000)
//                val b = ByteArray(256)
//                val ins = socket.getInputStream()
//                ins.read(b)
                true
            } catch(ex: Exception) {
                false
            }
        return Result(hostName, portNumber, result)
    }

    data class Result(val host: String, val port: Int, val success: Boolean)
    fun connect2(hostName: String, portNumber: Int): Result {
        return Result(hostName, portNumber, hostName.endsWith("17"))
    }

    fun scan() {
        val subNet = "47.208"
//        val subNet = "47.208.180"
        val portNumber = 4444

        val executor = Executors.newFixedThreadPool(100)
        val pool = ExecutorCompletionService<Result>(executor)
        val futures = arrayListOf< Future<Result>>()
        var count = 0
        (195..197).forEach { j ->
            (0..255).forEach { i ->
                futures.add(pool.submit { connect2("$subNet.$j.$i", portNumber) })
            }
        }

        println("Running")
        val successes = arrayListOf<Result>()
        futures.forEach {
            count++
            if ((count % 100) == 0) println("Processed $count ports\r")
            val r = it.get()
            if (r.success) {
                println("Success: ${r.host}:${r.port}")
                successes.add(r)
            }
        }
        println("Done running, successes: " + successes.joinToString("\n"))
    }

    fun updateIp() {
        val url = "http://cedric@beust.com:cedric@dynupdate.no-ip.com/nic/update?hostname=tmpced.ddns.net&myip=4.3.2.1"
    }

    class Response(val string: String)

    interface Api {
        @GET("/nic/update")
        fun update(@Query("hostname") hostname: String,
                @Query("myip") ip: String): Call<Void>
    }

    fun updateNoIp(hostName: String, ip: String) {
        val lp = LocalProperties()
        val user = lp.get("user")
        val password = lp.get("password")
        val credentials = Credentials.basic(user, password)
        val client = OkHttpClient()
        client.interceptors().add(object: Interceptor {
            override fun intercept(chain: Interceptor.Chain): com.squareup.okhttp.Response {
                val request = chain.request()
                val authenticatedRequest = request.newBuilder()
                        .header("Authorization", credentials).build()
                return chain.proceed(authenticatedRequest)
            }

        })
        val service = Retrofit.Builder()
                .client(client)
                .baseUrl("https://dynupdate.no-ip.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api::class.java)

        val r = service.update(hostName, ip)
        val result = r.execute()
        if (result.isSuccess) {
            println("Successfully updated host $hostName to IP $ip")
        } else {
            println("Error: " + result.code() + " " + result.errorBody())
        }
    }
}

fun main(args: Array<String>) {
//    HostScanner().connect("lunacabin.ddns.net", 4444)
    HostScanner().updateNoIp("tmpced.ddns.net", "1.2.3.4")
//    HostScanner().scan()
}