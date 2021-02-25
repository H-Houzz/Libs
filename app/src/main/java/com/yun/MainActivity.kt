package com.yun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.yun.baselibrary.utils.MMKVUtils

class MainActivity : AppCompatActivity() {
    lateinit var save: TextView
    lateinit var read: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        save = findViewById(R.id.tvSave)
        read = findViewById(R.id.tvRead)

        save.setOnClickListener {
            mmkvSave()
        }
        read.setOnClickListener {
            mmkvRead()
        }
    }

    private fun mmkvRead() {
        val isAk1=MMKVUtils.getInstance().getBoolean("ak1")
        val isAk2=MMKVUtils.getInstance().getInt("ak2")
        val isAk3=MMKVUtils.getInstance().getString("ak3")
        LogUtils.i("ak1=$isAk1,ak2=$isAk2,ak3=$isAk3")
    }

    private fun mmkvSave() {

        MMKVUtils.getInstance().putBoolean("ak1",true)
        MMKVUtils.getInstance().putInt("ak2",110)
        MMKVUtils.getInstance().putString("ak3","每当随着年纪的增加，总会有一些危机感。总是思索一个问题，人生该怎么渡过？没有李白的\"人生得意须尽欢，莫使金樽空对月\"，\n" +
                "        没有老庄的看破红尘，也没有奥斯特洛夫斯基的\"当回首往事，不因虚度年华而悔恨，不因碌碌无为而羞耻\"。自己只能做个平凡的人，\n" +
                "        掌握一门技术，练就一身能力，带着脑子，平凡过日子。")
        ToastUtils.showLong("保存成功")
    }
}