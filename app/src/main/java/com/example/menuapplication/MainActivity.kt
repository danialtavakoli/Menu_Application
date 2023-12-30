package com.example.menuapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.menuapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val menuItems = resources.getStringArray(R.array.menu_items)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, menuItems)
        val listView = binding.listView
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> navigateToFragment(BluetoothFragment())
                1 -> navigateToFragment(CalculatorFragment())
                2 -> navigateToFragment(MemoryFragment())
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager

        // Clear the back stack
        val backStackCount = fragmentManager.backStackEntryCount
        for (i in 0 until backStackCount) {
            fragmentManager.popBackStackImmediate()
        }

        // Replace with the new fragment
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentContainer.id, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}