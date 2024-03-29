package org.binaryitplanet.tradinget.Features.View.Inventory

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import org.binaryitplanet.tradinget.Features.Adapter.PacketAdapter
import org.binaryitplanet.tradinget.Features.Adapter.ShowSoldPacketAdapter
import org.binaryitplanet.tradinget.Features.Adapter.SoldPacketAdapter
import org.binaryitplanet.tradinget.Features.Prsenter.LedgerPresenterIml
import org.binaryitplanet.tradinget.Features.Prsenter.PacketPresenterIml
import org.binaryitplanet.tradinget.Features.View.Ledger.ViewLedgers
import org.binaryitplanet.tradinget.R
import org.binaryitplanet.tradinget.Utils.Config
import org.binaryitplanet.tradinget.Utils.PacketUtils
import org.binaryitplanet.tradinget.Utils.SoldPacketUtils
import org.binaryitplanet.tradinget.databinding.FragmentPacketsBinding
import org.binaryitplanet.tradinget.databinding.FragmentSoldPacketsBinding

class SoldPackets : Fragment(), InventoryView, ViewLedgers {


    private val TAG = "SoldPackets"
    private lateinit var binding: FragmentSoldPacketsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSoldPacketsBinding.inflate(
            inflater,
            container,
            false
        )

        binding.sortName.setOnClickListener {
            val presenter = LedgerPresenterIml(context!!, this)
            presenter.fetchSoldPacketList(true)
        }

        binding.sortDate.setOnClickListener {
            val presenter = LedgerPresenterIml(context!!, this)
            presenter.fetchSoldPacketList(false)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val presenter = LedgerPresenterIml(context!!, this)
        presenter.fetchSoldPacketList(true)
    }

    override fun onFetchSoldPacketListListener(soldPacketList: List<SoldPacketUtils>) {
        super.onFetchSoldPacketListListener(soldPacketList)
        val adapter = ShowSoldPacketAdapter(
            context!!, soldPacketList as ArrayList<SoldPacketUtils>)

        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(context!!)
        binding.list.setItemViewCacheSize(Config.LIST_CACHED_SIZE)
    }
//
//    override fun onFetchPacketListListener(PacketList: List<PacketUtils>) {
//        super.onFetchPacketListListener(PacketList)
//        val adapter = PacketAdapter(
//            context!!, PacketList as ArrayList<PacketUtils>)
//
//        binding.list.adapter = adapter
//        binding.list.layoutManager = LinearLayoutManager(context!!)
//        binding.list.setItemViewCacheSize(Config.LIST_CACHED_SIZE)
//    }


}