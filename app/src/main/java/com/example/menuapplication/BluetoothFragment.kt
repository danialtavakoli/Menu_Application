package com.example.menuapplication

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.menuapplication.databinding.FragmentBluetoothBinding

class BluetoothFragment : Fragment() {
    private lateinit var binding: FragmentBluetoothBinding
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val devicesList = arrayListOf<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                // All requested permissions are granted
                startBluetoothDiscovery()
            } else {
                // Permissions denied, handle accordingly
                Toast.makeText(
                    context,
                    "Permissions required for Bluetooth scan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // New Activity Result API
    private val requestBluetoothEnable =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                checkPermissionsAndStartDiscovery()
            } else {
                Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false)

        setupBluetoothListView()
        startBluetoothDiscovery()

        return binding.root
    }

    private fun setupBluetoothListView() {
        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, devicesList)
        binding.listBluetoothDevices.adapter = arrayAdapter
    }

    private fun startBluetoothDiscovery() {
        val localBluetoothAdapter = bluetoothAdapter
        if (localBluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!localBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothEnable.launch(enableBtIntent)
        } else {
            checkPermissionsAndStartDiscovery()
        }
    }

    private fun checkPermissionsAndStartDiscovery() {
        val requiredPermissions = mutableListOf<String>()

        // Handling for Android S (API level 31) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            // Handling for below Android S (API level 31)
            // Typically requires location permissions for Bluetooth scanning
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            // You can also check and request ACCESS_COARSE_LOCATION if necessary
        }

        if (requiredPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            // Permissions are already granted; proceed with the Bluetooth scan
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            requireContext().registerReceiver(receiver, filter)
            Log.d("BluetoothFragment", "Starting Bluetooth discovery")
            bluetoothAdapter?.startDiscovery()
            binding.progressBar.visibility = View.VISIBLE
            binding.listBluetoothDevices.visibility = View.GONE
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    Log.d("BluetoothFragment", "Device found: ${device?.name}")

                    if (device != null && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val deviceName = device.name ?: "Unknown Device"
                        devicesList.add("$deviceName\n${device.address}")
                        arrayAdapter.notifyDataSetChanged()
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("BluetoothFragment", "Discovery finished")
                    binding.progressBar.visibility = View.GONE
                    binding.listBluetoothDevices.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(receiver)
    }

}
