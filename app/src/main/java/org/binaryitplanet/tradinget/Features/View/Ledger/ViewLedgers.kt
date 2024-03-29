package org.binaryitplanet.tradinget.Features.View.Ledger

import org.binaryitplanet.tradinget.Utils.LedgerUtils
import org.binaryitplanet.tradinget.Utils.SoldPacketUtils


interface ViewLedgers {
    fun onPacketDeleteListener(position: Int){}
    fun onLedgerInsertListener(status: Boolean){}
    fun onLedgerDeleteListener(status: Boolean){}
    fun onLedgerUpdateListener(status: Boolean){}

    fun onFetchLedger(ledger: LedgerUtils){}

    fun onFetchLedgerListListener(ledgerList: List<LedgerUtils>){}

    fun onFetchSoldPacketListListener(soldPacketList: List<SoldPacketUtils>){}

    fun onFetchLedgerIdListListener(ledgerList: List<String>){}
}