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
import androidx.annotation.RequiresApi
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

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
                // Permission denied
            }
        }

    private val requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Bluetooth Admin permission granted
                // Proceed with Bluetooth operations
                checkPermissionsAndStartDiscovery()
            } else {
                Toast.makeText(context, "Bluetooth Admin permission denied", Toast.LENGTH_SHORT)
                    .show()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                // Prior to Android 12
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
            }
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
            Log.d("BluetoothFragment", "Starting Bluetooth discovery startDiscovery")
            bluetoothAdapter?.startDiscovery()
            binding.progressBar.visibility = View.VISIBLE
            binding.listBluetoothDevices.visibility = View.GONE
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("BluetoothFragment", "onReceive")
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
                    Log.d("BluetoothFragment", (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED).toString())
                    if (device != null) {
                        val deviceName = device.name ?: "Unknown Device"
                        Log.d("BluetoothFragment2", deviceName)
                        devicesList.add("$deviceName\n${device.address}")
                        Log.d("BluetoothFragment2", devicesList.toString())
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
        if (isReceiverRegistered) {
            context?.unregisterReceiver(receiver)
        }
    }

    private val isReceiverRegistered: Boolean
        get() {
            // Check if the receiver is registered before unregistering it
            return try {
                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context?.registerReceiver(receiver, filter)
                true // Receiver was registered
            } catch (e: IllegalArgumentException) {
                false // Receiver was not registered
            }
        }

}