/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.service

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import android.support.v4.content.LocalBroadcastManager
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exeption.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import org.json.JSONObject

/**
 * Base service to be extended by the merchant to provide the network calls that connect to the Adyen endpoints.
 */
abstract class DropInService : JobIntentService() {

    companion object {
        protected val TAG = LogUtil.getTag()

        // Key of the response content on the intent bundle
        const val API_CALL_RESULT_KEY = "payments_api_call_result"

        // Define the type of request the service needs to perform
        private const val REQUEST_TYPE_KEY = "request_type"
        private const val PAYMENTS_REQUEST = "type_payments"
        private const val DETAILS_REQUEST = "type_details"

        // Internal key of the content for the request
        private const val PAYMENT_COMPONENT_DATA_EXTRA_KEY = "payment_component_data_extra"
        private const val DETAILS_EXTRA_KEY = "details_method_extra"

        private const val dropInJobId = 11

        // Base for the action strings
        private const val adyenCheckoutBaseActionSuffix = ".adyen.checkout"
        // com.merchant.package.adyen.checkout.CALL_RESULT
        private const val callResultSuffix = "$adyenCheckoutBaseActionSuffix.CALL_RESULT"

        /**
         * Get the action sent to the [LocalBroadcastManager] to notify the result of the payments call.
         */
        fun getCallResultAction(context: Context): String {
            return context.packageName + callResultSuffix
        }

        /**
         * Helper function that sends a request for the merchant to make the payments call.
         */
        // False positive
        @Suppress("FunctionParameterNaming")
        fun requestPaymentsCall(context: Context, paymentComponentData: PaymentComponentData<out PaymentMethodDetails>) {
            Logger.d(TAG, "requestPaymentsCall - ${paymentComponentData.paymentMethod?.type}")

            val merchantService = DropIn.INSTANCE.configuration.serviceComponentName
            Logger.d(TAG, "merchantService - $merchantService")

            val workIntent = Intent()
            workIntent.putExtra(REQUEST_TYPE_KEY, PAYMENTS_REQUEST)
            workIntent.putExtra(PAYMENT_COMPONENT_DATA_EXTRA_KEY, paymentComponentData)

            enqueueWork(context, merchantService, dropInJobId, workIntent)
        }

        /**
         * Helper function that sends a request for the merchant to make the details call.
         */
        fun requestDetailsCall(context: Context, details: JSONObject) {
            Logger.d(TAG, "requestDetailsCall")

            val merchantService = DropIn.INSTANCE.configuration.serviceComponentName

            val workIntent = Intent()
            workIntent.putExtra(REQUEST_TYPE_KEY, DETAILS_REQUEST)
            workIntent.putExtra(DETAILS_EXTRA_KEY, details.toString())

            enqueueWork(context, merchantService, dropInJobId, workIntent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        Logger.d(TAG, "onHandleWork")

        when (intent.getStringExtra(REQUEST_TYPE_KEY)) {
            PAYMENTS_REQUEST -> {
                val paymentComponentDataForRequest =
                    intent.getParcelableExtra<PaymentComponentData<in PaymentMethodDetails>>(PAYMENT_COMPONENT_DATA_EXTRA_KEY)
                askPaymentsCall(paymentComponentDataForRequest)
            }
            DETAILS_REQUEST -> {
                val detailsString = intent.getStringExtra(DETAILS_EXTRA_KEY)
                val details = JSONObject(detailsString)
                askDetailsCall(details)
            }
        }
    }

    /**
     * Call this method for asynchronous handling of [makePaymentsCall]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun asyncCallback(callResult: CallResult) {
        handleCallResult(callResult)
    }

    private fun askPaymentsCall(paymentComponentData: PaymentComponentData<in PaymentMethodDetails>) {
        Logger.d(TAG, "askPaymentsCall")

        // Merchant makes network call
        val paymentsCallResult = makePaymentsCall(PaymentComponentData.SERIALIZER.serialize(paymentComponentData))

        handleCallResult(paymentsCallResult)
    }

    private fun askDetailsCall(details: JSONObject) {
        Logger.d(TAG, "askDetailsCall")

        // Merchant makes network call
        val detailsCallResult = makeDetailsCall(details)

        handleCallResult(detailsCallResult)
    }

    private fun handleCallResult(callResult: CallResult?) {
        if (callResult == null) {
            // Make sure people don't return Null from Java code
            throw CheckoutException("CallResult result from DropInService cannot be null.")
        }
        Logger.d(TAG, "handleCallResult - ${callResult.type.name}")

        // if type is WAIT do nothing and wait for async callback.
        if (callResult.type != CallResult.ResultType.WAIT) {
            // send response back to activity
            val resultIntent = Intent()

            resultIntent.action = getCallResultAction(this)
            resultIntent.putExtra(API_CALL_RESULT_KEY, callResult)

            val localBroadcastManager = LocalBroadcastManager.getInstance(this)
            localBroadcastManager.sendBroadcast(resultIntent)
        }
    }

    /**
     * In this method the merchant should make the network call to the payments/ endpoint.
     * We provide the "paymentMethod" parameter content, the rest should be filled in according to your needs.
     *
     * This call is expected to be synchronous, as it already runs in the background, and the base class will handle messaging with the UI after it
     * finishes based on the [CallResult]. If you want to make the call asynchronously, return [CallResult.ResultType.WAIT] on the type and call the
     * [asyncCallback] method afterwards.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentData The result data from the [PaymentComponent] the compose your call.
     * @return The result of the network call
     */
    abstract fun makePaymentsCall(paymentComponentData: JSONObject): CallResult

    /**
     * In this method the merchant should make the network call to the payments/details/ endpoint.
     *
     * This call is expected to be synchronous, as it already runs in the background, and the base class will handle messaging with the UI after it
     * finishes based on the [CallResult]. If you want to make the call asynchronously, return [CallResult.ResultType.WAIT] on the type and call the
     * [asyncCallback] method afterwards.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The result data from the [ActionComponent] the compose your call.
     * @return The result of the network call
     */
    abstract fun makeDetailsCall(actionComponentData: JSONObject): CallResult
}