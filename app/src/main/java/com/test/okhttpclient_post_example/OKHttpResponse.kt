package com.test.okhttpclient_post_example

import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by admin on 2017/11/17.
 */
open class OKHttpResponse {
    var statusCode : Int = 0
        private set
    private var responseString : String? = null
    private var responseBytes : ByteArray? = null
    private var headers : MutableMap<String, MutableList<String>>? = null

    @Throws(IOException::class)
    constructor(statusCode: Int, bytes: ByteArray?, headers: MutableMap<String, MutableList<String>>?) {
        this.statusCode = statusCode
        this.responseBytes = bytes
        this.headers = headers
    }

    constructor(string: String, responseCode: Int) {
        responseString = string
        statusCode = responseCode
    }

    fun getResponseString(): String? {
        responseString = responseBytes?.toString(Charset.forName("utf-8"))
        return responseString
    }

    fun getHeadersByName(name: String): MutableList<String>? = headers?.get(name)

    override fun toString(): String {
        return getResponseString() ?: "Response{statusCode=$statusCode, responseString='$responseString'}"
    }
}