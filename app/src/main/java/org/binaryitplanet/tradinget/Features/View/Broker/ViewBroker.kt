package org.binaryitplanet.tradinget.Features.View.Broker

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import org.binaryitplanet.tradinget.Features.Adapter.LedgerAdapter
import org.binaryitplanet.tradinget.Features.Common.StakeholderView
import org.binaryitplanet.tradinget.Features.Prsenter.LedgerPresenterIml
import org.binaryitplanet.tradinget.Features.Prsenter.StakeholderPresenterIml
import org.binaryitplanet.tradinget.Features.View.Ledger.ViewLedgers
import org.binaryitplanet.tradinget.R
import org.binaryitplanet.tradinget.Utils.Config
import org.binaryitplanet.tradinget.Utils.LedgerUtils
import org.binaryitplanet.tradinget.Utils.StakeholderUtils
import org.binaryitplanet.tradinget.databinding.ActivityViewBrokerBinding

class ViewBroker : AppCompatActivity(), StakeholderView, ViewLedgers {

    private val TAG = "ViewBroker"
    private lateinit var binding: ActivityViewBrokerBinding
    private lateinit var stakeholder: StakeholderUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_broker)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.enterTransition = null
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_broker)

        stakeholder = intent?.getSerializableExtra(Config.STAKEHOLDER) as StakeholderUtils
        setUpToolbar()

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.edit) {
                editData()
            } else if (it.itemId == R.id.delete) {
                deleteData()
            }
            return@setOnMenuItemClickListener super.onOptionsItemSelected(it)
        }
    }


    override fun onResume() {
        super.onResume()
        val presenter = StakeholderPresenterIml(this, this)
        presenter.fetchStakeholderById(stakeholder.id!!)
    }

    override fun onFetchStakeholderListener(stakeholder: StakeholderUtils) {
        super.onFetchStakeholderListener(stakeholder)
        this.stakeholder = stakeholder
        setupViews()
        val presenter = LedgerPresenterIml(this, this)
        presenter.fetchLedgerListByBrokerId(stakeholder.id!!)
    }

    override fun onFetchLedgerListListener(ledgerList: List<LedgerUtils>) {
        super.onFetchLedgerListListener(ledgerList)
        Log.d(TAG, "LedgerList: $ledgerList")
        val adapter = LedgerAdapter(
            this,
            ledgerList as ArrayList<LedgerUtils>,
            false
        )

        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.setItemViewCacheSize(Config.LIST_CACHED_SIZE)
    }

    private fun deleteData() {

        val builder = AlertDialog.Builder(this)

        builder.setTitle(Config.DELETE_BROKER_TITLE)
        builder.setMessage(Config.DELETE_BROKER_MESSAGE)

        builder.setIcon(R.drawable.ic_launcher)

        builder.setPositiveButton(Config.YES_MESSAGE){
                dialog: DialogInterface?, which: Int ->
            val presenter = StakeholderPresenterIml(this, this)
            presenter.deleteStakeholder(stakeholder)
        }

        builder.setNegativeButton(
            Config.NO_MESSAGE
        ){
                dialog: DialogInterface?, which: Int ->
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onDeleteStakeholderListener(status: Boolean) {
        super.onDeleteStakeholderListener(status)
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

    private fun editData() {
        val intent = Intent(this, AddBroker::class.java)
        intent.putExtra(Config.OPERATION_FLAG, false)
        intent.putExtra(Config.STAKEHOLDER, stakeholder)
        startActivity(intent)
        overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft)
    }

    private fun setupViews() {
        binding.name.text = stakeholder.name
        binding.mobileNumber.text = stakeholder.mobileNumber
        binding.altMobileNumber.text = stakeholder.altMobileNumber
        binding.stateCode.text = Config.STATE_CODE + ": " + stakeholder.stateCode

        if (stakeholder.firmName.isNullOrEmpty())
            binding.firmName.visibility = View.GONE
        else
            binding.firmName.text = Config.FIRM_NAME + ": " + stakeholder.firmName

        if (stakeholder.address.isNullOrEmpty())
            binding.address.visibility = View.GONE
        else
            binding.address.text = Config.ADDRESS + ": " + stakeholder.address

        if (stakeholder.gstNumber.isNullOrEmpty())
            binding.gstNumber.visibility = View.GONE
        else
            binding.gstNumber.text = Config.GST_NUMBER + ": " + stakeholder.gstNumber

        if (stakeholder.panNumber.isNullOrEmpty())
            binding.panNumber.visibility = View.GONE
        else
            binding.panNumber.text = Config.PAN_NUMBER + ": " + stakeholder.panNumber

        binding.mobileNumber.setOnClickListener {
            makeCall(stakeholder.mobileNumber)
        }
        binding.altMobileNumber.setOnClickListener {
            makeCall(stakeholder.altMobileNumber)
        }
    }

    private fun makeCall(number:String) {
        Log.d(TAG, "Making call to $number")
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CALL_PHONE), Config.REQUEST_CALL)
        } else {
            startActivity(intent)
        }
    }


    // Toolbar menu setting
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.view_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUpToolbar() {

        binding.toolbar.title = stakeholder.name

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