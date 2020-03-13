package com.newx.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 输入限制
        edt_progress.addTextChangedListener(object : TextWatcher {
            var l = 0 // 记录字符串被删除字符之前，字符串的长度
            var location = 0 // 记录光标的位置

            override fun afterTextChanged(s: Editable?) {
                val p = Pattern.compile("^(100|[1-9]\\d|\\d)$")
                val m = p.matcher(s.toString())
                if (m.find() || "" == s.toString()) {
                    Log.i(TAG, "当前输入：${s.toString()}")
                } else {
                    val length = edt_progress.text.length
                    val text = edt_progress.text.delete(
                        length - 1,
                        length
                    )
                    Log.i(TAG, "修正输入：$text")

                    edt_progress.text = text
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                l = s?.length ?: 0
                location = edt_progress.selectionStart
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        btn_update.setOnClickListener {
            var progress = 0
            progress = if (TextUtils.isEmpty(edt_progress.text.toString()))
                view_circle.progress + 5
            else
                edt_progress.text.toString().toInt()
            view_circle.setProgressAndText(progress, "当前进度$progress")
        }
    }
}
