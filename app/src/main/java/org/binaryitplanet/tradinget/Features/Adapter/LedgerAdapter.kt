package org.binaryitplanet.tradinget.Features.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_ledger_list_item.view.*
import kotlinx.android.synthetic.main.view_list_item_packet.view.*
import org.binaryitplanet.tradinget.Features.View.Buyer.ViewBuyer
import org.binaryitplanet.tradinget.Features.View.Inventory.ViewPacket
import org.binaryitplanet.tradinget.Features.View.Ledger.ViewLedger
import org.binaryitplanet.tradinget.R
import org.binaryitplanet.tradinget.Utils.Config
import org.binaryitplanet.tradinget.Utils.LedgerUtils
import org.binaryitplanet.tradinget.Utils.PacketUtils
import org.binaryitplanet.tradinget.Utils.StakeholderUtils
import android.util.Pair as UtilPair

class LedgerAdapter(
    val context: Context,
    val ledgerList: ArrayList<LedgerUtils>,
    val isBroker: Boolean
): RecyclerView.Adapter<LedgerAdapter.ViewHolder>() {
    private val TAG = "PacketAdapter"
    // Holding the view

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(context)
                .inflate(
                    R.layout.view_ledger_list_item,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int = ledgerList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var view = holder.itemView

        view.ledgerId.text = "Packet name: " + ledgerList[position].firstPacketName

        view.date.text = "Date: " + ledgerList[position].date
        view.dueDate.text = "Due date: " + ledgerList[position].dueDate

        view.totalWeight.text = ledgerList[position].totalWeight.toString() + " " + Config.CTS
        view.totalAmount.text = Config.RUPEE_SIGN + " " + ledgerList[position].totalAmount.toString()



        view.setOnClickListener {
            Log.d(TAG, "PacketClicked: $position")

            var intent: Intent? = Intent(context, ViewLedger::class.java)


            // Passing selected item data
            intent!!.putExtra(Config.LEDGER, ledgerList[position])
            intent!!.putExtra(Config.BROKER_FLAG, isBroker)
            context.startActivity(intent)
        }
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {}
}