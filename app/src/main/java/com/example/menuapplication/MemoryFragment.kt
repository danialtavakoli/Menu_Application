package com.example.menuapplication

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.menuapplication.databinding.FragmentMemoryBinding
import java.text.DecimalFormat

class MemoryFragment : Fragment() {
    private lateinit var binding: FragmentMemoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemoryBinding.inflate(inflater, container, false)
        displayMemoryInfo()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun displayMemoryInfo() {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemory = memoryInfo.totalMem / (1024 * 1024)  // Convert to Megabytes
        val freeMemory = memoryInfo.availMem / (1024 * 1024)  // Convert to Megabytes
        val usedMemory = totalMemory - freeMemory
        val df = DecimalFormat("#,### MB")

        binding.textViewMemoryInfo.text = "Total Memory: ${df.format(totalMemory)}\n" +
                "Free Memory: ${df.format(freeMemory)}\n" +
                "Used Memory: ${df.format(usedMemory)}"
    }
}
