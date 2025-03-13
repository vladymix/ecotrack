package com.vladymix.ecotrack.ui.devices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.altamirano.fabricio.core.tools.inflate
import com.vladymix.ecotrack.R
import com.vladymix.ecotrack.databinding.ItemDeviceBleBinding


@SuppressLint("MissingPermission")
class DevicesAdapter(private val listener: (BluetoothDevice) -> Unit) :
    RecyclerView.Adapter<DevicesAdapter.DevicesHolder>() {
    private val source = ArrayList<BluetoothDevice>()

    fun addItem(device:BluetoothDevice){
        if(device.name.isNullOrEmpty())
            return

        val exist = source.firstOrNull { it.address == device.address }
        if(exist == null){
            source.add(device)
            notifyItemInserted(source.size)
        }
    }

    inner class DevicesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = ItemDeviceBleBinding.bind(itemView)

        fun bindData(item: BluetoothDevice) {
            binding.tvName.text = item.name
            binding.tvMac.text = item.address
            binding.root.setOnClickListener {
                listener.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesHolder {
        return DevicesHolder(parent.inflate(R.layout.item_device_ble))
    }

    override fun getItemCount(): Int {
        return source.size
    }

    override fun onBindViewHolder(holder: DevicesHolder, position: Int) {
        holder.bindData(source[position])
    }
}