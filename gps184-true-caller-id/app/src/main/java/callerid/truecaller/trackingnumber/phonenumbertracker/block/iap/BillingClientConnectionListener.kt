package callerid.truecaller.trackingnumber.phonenumbertracker.block.iap

interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}