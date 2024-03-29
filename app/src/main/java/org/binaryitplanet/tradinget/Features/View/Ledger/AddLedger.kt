package org.binaryitplanet.tradinget.Features.View.Ledger

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.binaryitplanet.tradinget.Features.Adapter.BrokerDropDownAdapter
import org.binaryitplanet.tradinget.Features.Adapter.LedgerPacketAdapter
import org.binaryitplanet.tradinget.Features.Adapter.PacketDropDownAdapter
import org.binaryitplanet.tradinget.Features.Adapter.SubPacketDropDownAdapter
import org.binaryitplanet.tradinget.Features.Common.StakeholderView
import org.binaryitplanet.tradinget.Features.Prsenter.LedgerPresenterIml
import org.binaryitplanet.tradinget.Features.Prsenter.PacketDetailsPresenterIml
import org.binaryitplanet.tradinget.Features.Prsenter.PacketPresenterIml
import org.binaryitplanet.tradinget.Features.Prsenter.StakeholderPresenterIml
import org.binaryitplanet.tradinget.Features.View.Inventory.InventoryView
import org.binaryitplanet.tradinget.Features.View.Inventory.ViewPacketDetails
import org.binaryitplanet.tradinget.R
import org.binaryitplanet.tradinget.Utils.*
import org.binaryitplanet.tradinget.databinding.ActivityAddLedgerBinding
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class AddLedger : AppCompatActivity(), InventoryView, ViewPacketDetails, StakeholderView, ViewLedgers {


    private val TAG = "AddLedger"
    private lateinit var binding: ActivityAddLedgerBinding
    private lateinit var packetList: ArrayList<PacketUtils>
    private var subPacketList = arrayListOf<PacketDetailsUtils>()
    private lateinit var allSubPacketList: ArrayList<PacketDetailsUtils>
    private lateinit var brokerList: ArrayList<StakeholderUtils>
    private lateinit var stakeholder: StakeholderUtils
    private var soldPacketList = arrayListOf<SoldPacketUtils>()
    private var packetPosition: Int = -1
    private var subPacketPosition: Int = -1
    private var brokerPosition: Int = -1
    private var day: Int = 0
    private var month: Int = 0
    private var year: Int = 0
    private var dueDay: Int = 0
    private var dueMonth: Int = 0
    private var dueYear: Int = 0
    private lateinit var weight: String
    private lateinit var rate: String

    private lateinit var dateString: String
    private lateinit var dueDateString: String

    private var totalWeight: Double = 0.0
    private var totalRate: Double = 0.0
    private var totalPrice: Double = 0.0

    private var imageURL: String? = null
    private var ledgerId: String? = null

    private var brokerPercentage: Double = 0.0
    private var brokerAmount: Double = 0.0
    private var discountPercentage: Double = 0.0
    private var discountAmount: Double = 0.0

    private lateinit var paymentType: String
    private lateinit var remark: String
    private lateinit var progressDialog: ProgressDialog

    private var operationFlag = true

    private lateinit var ledger: LedgerUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ledger)

        ledgerId = Calendar.getInstance().timeInMillis.toString()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_ledger)

        progressDialog = ProgressDialog(this)

        setUpToolbar()
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.done && checkValidity()) {
                Log.d(TAG, "Size: ${subPacketList.size}")
                if (operationFlag) {
                    saveData()
                } else {
                    updateData()
                }
            }
            return@setOnMenuItemClickListener super.onOptionsItemSelected(it)
        }

        binding.addPacket.setOnClickListener {
            if (isPacketValid()) {
                var subPacketNumber = ""
                var sieve = ""
                if (subPacketPosition != -1) {
                    subPacketNumber = subPacketList[subPacketPosition].packetDetailsNumber
                    sieve = subPacketList[subPacketPosition].sieve
                }
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val dateMilli = calendar.timeInMillis
                soldPacketList.add(
                    SoldPacketUtils(
                        null,
                        ledgerId,
                        stakeholder.name,
                        dateString,
                        dateMilli,
                        packetList[packetPosition].packetNumber,
                        packetList[packetPosition].packetName,
                        subPacketNumber,
                        sieve,
                        weight.toDouble(),
                        rate.toDouble(),
                        packetList[packetPosition].code,
                        packetPosition,
                        subPacketPosition
                    )
                )
                totalWeight += weight.toDouble()
                totalRate += rate.toDouble()
                totalPrice += (weight.toDouble() * rate.toDouble())
                Log.d(TAG, "Price: $totalPrice $totalWeight $totalRate")
                setPacketList()
            }
        }

        binding.addImage.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            val intent = Intent.createChooser(galleryIntent, Config.PICK_IMAGE)
            startActivityForResult(intent, Config.PICK_IMAGE_REQUEST_CODE)
        }
    }

    private fun updateData() {

        if (brokerPosition != -1){
            ledger.brokerId = brokerList[brokerPosition].id!!
            ledger.brokerName = brokerList[brokerPosition].name
        }

        ledger.paymentType = paymentType
        ledger.brokerPercentage = brokerPercentage
        ledger.brokerAmount = brokerAmount
        ledger.brokerageAmount = totalPrice + brokerAmount
        ledger.discountPercentage = discountPercentage
        ledger.discountAmount = discountAmount
        ledger.remark = remark
        ledger.date = dateString
        ledger.dueDate = dueDateString
        ledger.imageUrl = imageURL
        ledger.brokerAmountRemaining = brokerAmount - ledger.brokerAmountPaid
        ledger.totalAmount = totalPrice - discountAmount
        ledger.totalWeight = totalWeight
        ledger.totalPackets = soldPacketList.size
        ledger.firstPacketName = soldPacketList[0].packetName

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateMilli = calendar.timeInMillis
        calendar.set(dueYear, dueMonth, dueDay)
        val dueDateMilli = calendar.timeInMillis

        ledger.dateInMilli = dateMilli
        ledger.dueDateInMilli = dueDateMilli


        val presenter = LedgerPresenterIml(this, this)
        presenter.updateLedger(ledger, soldPacketList)



        Log.d(TAG, "Ledger: $ledger")

    }

    override fun onLedgerUpdateListener(status: Boolean) {
        super.onLedgerUpdateListener(status)
        if (status) {
            Toast.makeText(
                this,
                Config.SUCCESS_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
            onBackPressed()
        } else {
            Toast.makeText(
                this,
                Config.FAILED_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Config.PICK_IMAGE_REQUEST_CODE
            && data != null
            && data.data != null
        ) {
            val imageUri = data.data
            imageURL = imageUri.toString()
            Log.d(TAG, "ImagePath: $imageURL")
            binding.addImage.setImageURI(imageUri)

        }
    }

    private fun setPacketList() {
        val adapter = LedgerPacketAdapter(
            this,
            soldPacketList,
            this
        )

        binding.packetList.adapter = adapter
        binding.packetList.layoutManager = LinearLayoutManager(this)
        binding.packetList.setItemViewCacheSize(Config.LIST_CACHED_SIZE)

        binding.packet.setText("")
        binding.subPacket.setText("")
        binding.weight.setText("")
        binding.rate.setText("")

        packetPosition = -1
        subPacketPosition = -1

        subPacketList.clear()

        setSubPacketDropDownAdapter()
    }

    private fun isPacketValid(): Boolean {
        weight = binding.weight.text.toString()
        rate = binding.rate.text.toString()

        if (packetPosition == -1) {
            binding.packet.error = Config.REQUIRED_FIELD
            binding.packet.requestFocus()
            return false
        }

        if (weight.isNullOrEmpty()) {
            binding.weight.error = Config.REQUIRED_FIELD
            binding.weight.requestFocus()
            return false
        }

        if (rate.isNullOrEmpty()) {
            binding.rate.error = Config.REQUIRED_FIELD
            binding.rate.requestFocus()
            return false
        }

        return true
    }

    override fun onResume() {
        super.onResume()

        stakeholder = intent?.getSerializableExtra(Config.STAKEHOLDER) as StakeholderUtils
        operationFlag = intent?.getBooleanExtra(Config.OPERATION_FLAG, true)!!

        val  presenter = PacketPresenterIml(this, this)
        presenter.fetchPacketList()

        val brokerPresenter = StakeholderPresenterIml(this, this)
        brokerPresenter.fetchStakeholder(Config.TYPE_ID_BROKER)


        val subPacketPresenter = PacketDetailsPresenterIml(this, this)
        subPacketPresenter.fetchAllPacketDetailsList()

        setupDate()

        val paymentTypes = resources.getStringArray(R.array.paymentType)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, paymentTypes)
        binding.paymentType.setText(paymentTypes[0])
        binding.paymentType.setAdapter(adapter)


        if (!operationFlag) {
            ledger = intent?.getSerializableExtra(Config.LEDGER) as LedgerUtils
            setViews()
        }
    }

    private fun setViews() {
        binding.brokerPercentage.setText(ledger.brokerPercentage.toString())
        binding.brokerAmount.setText(ledger.brokerAmount.toString())
        binding.discountPercentage.setText(ledger.discountPercentage.toString())
        binding.discountAmount.setText(ledger.discountAmount.toString())
        binding.remark.setText(ledger.remark)
        imageURL = ledger.imageUrl
        if (!imageURL.isNullOrEmpty())
            binding.addImage.setImageURI(Uri.parse(imageURL))
        binding.date.text = ledger.date
        binding.dueDate.text = ledger.dueDate
        dateString = ledger.date
        dueDateString = ledger.dueDate

        val dates = ledger.date.split("/")
        day = dates[0].toInt()
        month = dates[1].toInt()
        year = dates[2].toInt()

        val dueDates = ledger.dueDate.split("/")
        dueDay = dueDates[0].toInt()
        dueMonth = dueDates[1].toInt()
        dueYear = dueDates[2].toInt()


        ledgerId = ledger.ledgerId

        val presenter = LedgerPresenterIml(this, this)
        presenter.fetchSoldPacketListByLedgerId(ledgerId!!)

        binding.packetList.visibility = View.GONE
        binding.weightLayout.visibility = View.GONE
        binding.packetLayout.visibility = View.GONE
        binding.subPacketLayout.visibility = View.GONE
        binding.rateLayout.visibility = View.GONE
        binding.addPacket.visibility = View.GONE


    }

    override fun onFetchSoldPacketListListener(soldPacketList: List<SoldPacketUtils>) {
        super.onFetchSoldPacketListListener(soldPacketList)
        this.soldPacketList = soldPacketList as ArrayList<SoldPacketUtils>
        setPacketList()
        soldPacketList.forEach {
            totalWeight += it.weight
            totalRate += it.rate
            totalPrice += (it.weight * it.rate)
        }

        Log.d(TAG, "$totalWeight $totalRate $totalPrice")
    }

    override fun onFetchAllPacketDetailsListListener(subPacketList: List<PacketDetailsUtils>) {
        super.onFetchAllPacketDetailsListListener(subPacketList)
        allSubPacketList = subPacketList as ArrayList<PacketDetailsUtils>
    }

    private fun setupDate() {
        var calendar = Calendar.getInstance()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        dueDay = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        dueMonth = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        dueYear = calendar.get(Calendar.YEAR)
        dateString = getFormattedDate(day, month, year).toString()
        binding.date.text = dateString
        dueDateString = getFormattedDate(dueDay, dueMonth, dueYear).toString()
        binding.dueDate.text = dueDateString

        binding.date.setOnClickListener {
            var datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                day = dayOfMonth
                this.month = month
                this.year = year
                Log.d(TAG, "Date: $dayOfMonth/$month/$year")
                dateString = getFormattedDate(day, month, year).toString()
                binding.date.text = dateString
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val dateMilli = calendar.timeInMillis
                soldPacketList.forEach {
                    it.dateInMilli = dateMilli
                }
            }, year, month, day)
            datePickerDialog.show()
        }
        binding.dueDate.setOnClickListener {

            Log.d(TAG, "Date: $dueDay/$dueMonth/$dueYear")
            var datePickerDialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    dueDay = dayOfMonth
                    dueMonth = month
                    dueYear = year
                    dueDateString = getFormattedDate(dueDay, dueMonth, dueYear).toString()
                    binding.dueDate.text = dueDateString
                }, dueYear, dueMonth, dueDay)
            datePickerDialog.show()
        }
    }

    private fun getFormattedDate(day: Int, month: Int, year: Int): CharSequence? {
        return "%02d/%02d/%04d".format(
            day,
            month+1,
            year
        )
    }

    override fun onFetchPacketListListener(PacketList: List<PacketUtils>) {
        super.onFetchPacketListListener(PacketList)
        packetList = PacketList as ArrayList<PacketUtils>
        Log.d(TAG, "PacketList: $packetList")
        setPacketDropDownAdapter()
        binding.packet.setOnItemClickListener { parent, view, position, id ->
            binding.packet.setText(packetList[position].packetName)
            binding.subPacket.text = null
            packetPosition = position
            subPacketPosition = -1
            val presenter = PacketDetailsPresenterIml(this, this)
            presenter.fetchPacketDetailsList(packetList[position].packetNumber)
        }
    }

    private fun setPacketDropDownAdapter() {
        val adapter = PacketDropDownAdapter(this, packetList)
        binding.packet.setAdapter(adapter)
    }

    override fun onFetchPacketDetailsListListener(packetDetailsList: List<PacketDetailsUtils>) {
        super.onFetchPacketDetailsListListener(packetDetailsList)
        subPacketList = packetDetailsList as ArrayList<PacketDetailsUtils>
        Log.d(TAG, "SubPacketList: ${subPacketList.size}")
        setSubPacketDropDownAdapter()
        binding.subPacket.setOnItemClickListener { parent, view, position, id ->
            subPacketPosition = allSubPacketList.indexOf(subPacketList[position])
            binding.subPacket.setText(subPacketList[position].packetDetailsNumber)
        }
    }

    private fun setSubPacketDropDownAdapter() {
        val adapter = SubPacketDropDownAdapter(this, subPacketList)
        binding.subPacket.setAdapter(adapter)
    }

    override fun onFetchStakeholderListListener(stakeholderList: List<StakeholderUtils>) {
        super.onFetchStakeholderListListener(stakeholderList)
        brokerList = stakeholderList as ArrayList<StakeholderUtils>
        val adapter = BrokerDropDownAdapter(this, brokerList)
        binding.broker.setAdapter(adapter)
        binding.broker.setOnItemClickListener { parent, view, position, id ->
            brokerPosition = position
            binding.broker.setText(brokerList[position].name)
        }
    }

    override fun onPacketDeleteListener(position: Int) {
        super.onPacketDeleteListener(position)
        totalWeight -= soldPacketList[position].weight
        totalRate -= soldPacketList[position].rate
        totalPrice -= (soldPacketList[position].weight * soldPacketList[position].rate)
        Log.d(TAG, "Price: $totalPrice $totalWeight $totalRate")
        soldPacketList.removeAt(position)
        setPacketList()
    }

    private fun saveData() {
        Log.d(TAG, "Saving...")

        progressDialog.setTitle(Config.CREATING_LEDGER_TITLE)
        progressDialog.setMessage(Config.CREATING_LEDGER_MESSAGE)
        progressDialog.show()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateMilli = calendar.timeInMillis
        calendar.set(dueYear, dueMonth, dueDay)
        val dueDateMilli = calendar.timeInMillis
        var brokerId: Long? = null
        var brokerName: String? = null

        if (brokerPosition != -1){
            brokerId = brokerList[brokerPosition].id!!
            brokerName = brokerList[brokerPosition].name
        }

        val ledgerUtils = LedgerUtils(
            null,
            ledgerId,
            stakeholder.id!!,
            stakeholder.name,
            stakeholder.mobileNumber,
            brokerId,
            brokerName,
            brokerPercentage,
            brokerAmount,
            0.0,
            brokerAmount,
            totalPrice + brokerAmount,
            discountPercentage,
            discountAmount,
            totalWeight,
            totalPrice - discountAmount,
            0.0,
            soldPacketList.size,
            soldPacketList[0].packetName,
            Calendar.getInstance().get(Calendar.MONTH),
            dateString,
            dateMilli,
            dueDateString,
            dueDateMilli,
            paymentType,
            remark,
            imageURL,
            null
        )

        val presenter = LedgerPresenterIml(this, this)
        presenter.insertLedger(
            ledgerUtils,
            soldPacketList,
            packetList,
            allSubPacketList
        )
    }

    override fun onLedgerInsertListener(status: Boolean) {
        super.onLedgerInsertListener(status)
        if (status) {
            Toast.makeText(
                this,
                Config.SUCCESS_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
            onBackPressed()
        } else {
            Toast.makeText(
                this,
                Config.FAILED_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
        }
        progressDialog.dismiss()
    }

    private fun checkValidity(): Boolean {
        if (soldPacketList.size < 1) {
            binding.packet.error = Config.REQUIRED_FIELD
            binding.packet.requestFocus()
            return false
        }

        remark = binding.remark.text.toString()
        paymentType = binding.paymentType.text.toString()

        if (paymentType.isNullOrEmpty()) {
            binding.paymentType.error = Config.REQUIRED_FIELD
            binding.paymentType.requestFocus()
            return false
        }

        if (!binding.brokerPercentage.text.toString().isNullOrEmpty()) {
            brokerPercentage = binding.brokerPercentage.text.toString().toDouble()
        }
        if (!binding.brokerAmount.text.toString().isNullOrEmpty()) {
            brokerAmount = binding.brokerAmount.text.toString().toDouble()
        }
        if (!binding.discountPercentage.text.toString().isNullOrEmpty()) {
            discountPercentage = binding.discountPercentage.text.toString().toDouble()
        }
        if (!binding.discountAmount.text.toString().isNullOrEmpty()) {

            discountAmount = binding.discountAmount.text.toString().toDouble()
        }

        return true
    }

    // Toolbar menu setting
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.done_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpToolbar() {

        binding.toolbar.title = Config.TOOLBAR_TITLE_ADD_LEDGER

        binding.toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "Back pressed")
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft)
    }
}