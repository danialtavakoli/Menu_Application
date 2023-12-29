package com.example.menuapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.menuapplication.databinding.FragmentMemoryBinding

class MemoryFragment : Fragment() {
    private lateinit var binding: FragmentMemoryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemoryBinding.inflate(layoutInflater)
        return binding.root
    }
}
