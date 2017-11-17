package com.test.okhttpclient_post_example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * Created by admin on 2017/11/17.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout() {
            padding = dip(5)
            button("Post data") {
                textSize = 14f
                width = matchParent
                height = wrapContent
                onClick {
                    doAsync {
                        var response: OKHttpResponse? = OKHttpHelper.instance.post("https://www.google.com", null, null)
                        val result: String? = response?.getResponseString()

                        uiThread {
                            //Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                            println(result)
                        }
                    }
                }
            }
        }
    }


}