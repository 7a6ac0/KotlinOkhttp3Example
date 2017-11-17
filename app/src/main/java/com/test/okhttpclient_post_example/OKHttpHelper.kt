package com.test.okhttpclient_post_example

import android.util.Log
import okhttp3.*
import okhttp3.internal.tls.OkHostnameVerifier
import java.io.File
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

/**
 * Created by admin on 2017/11/17.
 */
open class OKHttpHelper private constructor() {
    private val TAG = "OKHttpHelper"

    private val mOkHttpCilent: OkHttpClient

    private object Holder { val INSTANCE = OKHttpHelper() }

    companion object { //Singleton
        private var CONNECTION_TIME_OUT = 30 * 1000
        private var READ_TIME_OUT = 30 * 1000
        private var WRITE_TIME_OUT = 30 * 1000
        val instance: OKHttpHelper by lazy { Holder.INSTANCE }
    }

    init {
        mOkHttpCilent = OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .hostnameVerifier(myHostnameVerifier())
                .build()
    }

    /**
     * GET
     */
    @Throws(Exception::class)
    operator fun get(url: String, requestParams: List<Pair<String, String>>,
                     requestHeaders: List<Pair<String, String>>): OKHttpResponse? {
        return execute(Type.GET, url, requestParams, requestHeaders)
    }
    /**
     * POST
     */
    @Throws(Exception::class)
    fun post(url: String, requestParams: List<Pair<String, String>>?,
             requestHeaders: List<Pair<String, String>>?): OKHttpResponse? {
        return execute(Type.POST, url, requestParams, requestHeaders)
    }
    /**
     * Upload File
     */
    @Throws(Exception::class)
    fun uploadFile(url: String, requestParams: List<Pair<String, String>>,
                   requestHeaders: List<Pair<String, String>>, filePath: String): OKHttpResponse? {
        return execute(url, requestParams, requestHeaders, filePath)
    }
    /**
     * Download File
     */
    @Throws(Exception::class)
    fun downloadFile(url: String, requestParams: List<Pair<String, String>>,
                     requestHeaders: List<Pair<String, String>>): OKHttpResponse? {
        return execute(Type.POST, url, requestParams, requestHeaders)
    }
    /**
     * execute request
     */
    @Throws(Exception::class)
    private fun execute(type: Type, url: String, requestParams: List<Pair<String, String>>?,
                        requestHeaders: List<Pair<String, String>>?): OKHttpResponse? {
        var result: OKHttpResponse? = null
        try {
            val okResponse = performRequest(OKRequest(type, url, requestParams, requestHeaders))//进行参数封装，发起网络请求
            if (okResponse != null) {
                result = OKHttpResponse(okResponse.responseCode, okResponse.body, okResponse.headers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return result
    }

    @Throws(Exception::class)
    private fun execute(url: String, requestParams: List<Pair<String, String>>,
                        requestHeaders: List<Pair<String, String>>, filePath: String): OKHttpResponse? {
        var result: OKHttpResponse? = null
        try {
            val okResponse = performRequest(OKRequest(Type.POST, url, requestParams, requestHeaders, filePath))
            if (okResponse != null) {
                result = OKHttpResponse(okResponse.responseCode, okResponse.body, okResponse.headers)
            }
        } catch (e: Exception) {
            throw e
        }
        return result
    }


    internal enum class Type {
        GET,
        POST
    }

    private inner class OKRequest {
        private var mType: Type? = null
        var mUrl: String? = null
            private set
        private var mRequestParams: List<Pair<String, String>>? = null
        private var mRequestHeaders: List<Pair<String, String>>? = null
        private var mFilePath: String? = null
        constructor(type: Type, url: String,
                    requestParams: List<Pair<String, String>>?,
                    requestHeaders: List<Pair<String, String>>?) {
            mType = type
            mUrl = url
            mRequestParams = requestParams
            mRequestHeaders = requestHeaders
        }
        constructor(type: Type, url: String,
                    requestParams: List<Pair<String, String>>,
                    requestHeaders: List<Pair<String, String>>, filePath: String) {
            mType = type
            mUrl = url
            mRequestParams = requestParams
            mRequestHeaders = requestHeaders
            mFilePath = filePath
        }
        fun getDataRequest(): Request{
            val request: Request
            val builder = Request.Builder()
            builder.url(mUrl!!)
            builder.header("Connection", "Close")
            if (mRequestHeaders?.isNotEmpty() ?: false) {
                for (header in mRequestHeaders!!) {
                    builder.addHeader(header.first, header.second)
                }
            }
            if (mType == Type.GET) {
                builder.get()
            } else {
                if (mFilePath?.isNotEmpty() ?: false) { //Set up file upload
                    val bodyBuilder = MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                    val file = File(mFilePath)
                    if (file.exists()) {
                        bodyBuilder.addFormDataPart("file", file.name, RequestBody.create(null, file))
                    }
                    mRequestParams?.let {
                        for (param in it) {
                            bodyBuilder.addFormDataPart(param.first, param.second)
                        }
                    }
                    builder.post(bodyBuilder.build())
                } else {// setting headers
                    mRequestParams?.let {
                        val formBuilder = FormBody.Builder()
                        for (param in it) {
                            formBuilder.add(param.first, param.second)
                        }
                        builder.post(formBuilder.build())
                    }
                }
            }
            request = builder.build()
            return request
        }
    }

    private inner class OKResponse(private val response: Response?) {
        val responseCode: Int
            get() = response!!.code()

        val body: ByteArray?
            get() {
                var result: ByteArray? = null
                if (response != null && response.body() != null) {
                    try {
                        result = response.body()!!.bytes()
                    } catch (e: IOException) {
                        Log.e(TAG, e.message)
                    }
                    headers
                }
                return result
            }

        val headers: MutableMap<String, MutableList<String>>?
            get() {
                var headers: MutableMap<String, MutableList<String>>? = null
                if (response != null && response.headers() != null) {
                    try {
                        headers = response.headers().toMultimap()
                    } catch (e: Exception) {
                    }
                }
                return headers
            }
    }

    @Throws(IOException::class)
    private fun performRequest(request: OKRequest): OKResponse? {
        val call = mOkHttpCilent.newCall(request.getDataRequest())
        return OKResponse(call.execute())
    }

    private inner class myHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            var result = false
            try {
                val certs = session.peerCertificates as Array<X509Certificate>
                if (certs.isNotEmpty()) {
                    for (i in certs.indices) {
                        result = OkHostnameVerifier.INSTANCE.verify(hostname, certs[i])
                        if (result) {
                            break
                        }
                    }
                } else {
                    result = true
                }
            } catch (e: SSLPeerUnverifiedException) {
                e.printStackTrace()
            }
            return result
        }
    }
}