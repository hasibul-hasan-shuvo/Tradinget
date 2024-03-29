package org.binaryitplanet.tradinget.Features.View.Ledger

import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import org.binaryitplanet.tradinget.Features.Prsenter.SellerLedgerPresenterIml
import org.binaryitplanet.tradinget.R
import org.binaryitplanet.tradinget.Utils.BuyUtils
import org.binaryitplanet.tradinget.Utils.Config
import org.binaryitplanet.tradinget.Utils.SellerLedgerUtils
import org.binaryitplanet.tradinget.Utils.StakeholderUtils
import org.binaryitplanet.tradinget.databinding.ActivityAddSellerLedgerBinding
import java.util.*

class AddSellerLedger : AppCompatActivity(), SellerLedgerView {

    private val TAG = "AddSellerLedger"
    private lateinit var binding: ActivityAddSellerLedgerBinding

    private var transactionType: String? = null
    private var paymentType: String? = null
    private var amount: String? = null
    private var discountAmount: Double = 0.0
    private var remark: String? = null
    private var issueDate: String? = null


    private var issueDay: Int = 0
    private var issueMonth: Int = 0
    private var issueYear: Int = 0
    private var dateMillis: Long = 0
    private lateinit var months: Array<String>

    private lateinit var buy: BuyUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_seller_ledger)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_seller_ledger)

        setUpToolbar()

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.done && checkValidity()) {
                saveData()
            }
            return@setOnMenuItemClickListener super.onOptionsItemSelected(it)
        }


        getCurrentDate()

        setUpDropDown()

        setUpIssueDate()

        setUpIssueDateListener()
    }

    override fun onResume() {
        super.onResume()
        buy = intent?.getSerializableExtra(Config.BUY) as BuyUtils
    }

    private fun saveData() {

        val sellerLedger = SellerLedgerUtils(
            null,
            buy.id,
            transactionType!!,
            paymentType!!,
            discountAmount,
            amount!!.toDouble() - discountAmount,
            remark!!,
            issueDate!!,
            dateMillis
        )

        val presenter = SellerLedgerPresenterIml(this, this)
        presenter.insertLedger(sellerLedger)
    }

    override fun onInsertLedgerListener(status: Boolean) {
        super.onInsertLedgerListener(status)
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
                Config.SUCCESS_MESSAGE,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Checking validity
    private fun checkValidity(): Boolean {
        transactionType = binding.type.text.toString()
        amount = binding.amount.text.toString()
        remark = binding.remark.text.toString()
        paymentType = binding.paymentType.text.toString()

        if (!binding.discount.text.toString().isNullOrEmpty())
            discountAmount = binding.discount.text.toString().toDouble()

        if (transactionType.isNullOrEmpty()) {
            binding.type.error = Config.REQUIRED_FIELD
            binding.type.requestFocus()
            return false
        }
        if (paymentType.isNullOrEmpty()) {
            binding.paymentType.error = Config.REQUIRED_FIELD
            binding.paymentType.requestFocus()
            return false
        }

        if (amount.isNullOrEmpty()) {
            binding.amount.error = Config.REQUIRED_FIELD
            binding.amount.requestFocus()
            return false
        }

        return true
    }

    private fun getCurrentDate() {
        val calendar = Calendar.getInstance()
        issueDay = calendar.get(Calendar.DAY_OF_MONTH)
        issueMonth = calendar.get(Calendar.MONTH)
        issueYear = calendar.get(Calendar.YEAR)
    }
    private fun setUpIssueDateListener() {
        binding.issueDate.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                issueDay = dayOfMonth
                issueMonth = month
                issueYear = year
                setUpIssueDate()
            }, issueYear, issueMonth, issueDay).show()
        }
    }

    private fun setUpIssueDate() {
        dateMillis = issueYear * 10000L +
                issueMonth * 100L +
                issueDay
        issueDate = "%02d/%02d/%04d".format(
            issueDay,
            issueMonth+1,
            issueYear
        )
        binding.issueDate.text = issueDate
    }

    private fun setUpDropDown() {
        var transactionTypes = resources.getStringArray(R.array.transactionType)
        months = resources.getStringArray(R.array.months)
        var years = resources.getStringArray(R.array.years)
        var paymentTypes = resources.getStringArray(R.array.paymentType)

        var transactionAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            transactionTypes
        )

        var paymentAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            paymentTypes
        )


        binding.type.setText(transactionTypes[0])
        binding.paymentType.setText(paymentTypes[0])

        binding.type.setAdapter(transactionAdapter)
        binding.paymentType.setAdapter(paymentAdapter)
    }


    // Toolbar menu setting
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.done_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun setUpToolbar() {

        binding.toolbar.title = Config.TOOLBAR_TITLE_ADD_SELLER_LEDGER
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