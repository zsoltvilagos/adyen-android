/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/3/2019.
 */

package com.adyen.checkout.example

import com.adyen.checkout.base.model.payments.request.* // ktlint-disable no-wildcard-imports
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.example.api.CheckoutApiService
import com.adyen.checkout.example.api.model.PaymentsRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * This is just an example on how to make network calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 */
class ExampleDropInService : DropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        Logger.d(TAG, "makePaymentsCall")

        val serializedPaymentComponentData = PaymentComponentData.SERIALIZER.deserialize(paymentComponentData)

        val paymentMethod = serializedPaymentComponentData.paymentMethod ?: return CallResult(CallResult.ResultType.ERROR, "Empty payment data")

        val paymentsRequest = PaymentsRequest(paymentMethod,
                serializedPaymentComponentData.shopperReference!!,
                serializedPaymentComponentData.isStorePaymentMethodEnable)

        val moshi = Moshi.Builder()
                .add(PolymorphicJsonAdapterFactory.of(PaymentMethodDetails::class.java, PaymentMethodDetails.TYPE)
                        .withSubtype(CardPaymentMethod::class.java, CardPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(IdealPaymentMethod::class.java, IdealPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(MolpayPaymentMethod::class.java, MolpayPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(EPSPaymentMethod::class.java, EPSPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(DotpayPaymentMethod::class.java, DotpayPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(EntercashPaymentMethod::class.java, EntercashPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(OpenBankingPaymentMethod::class.java, OpenBankingPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(GooglePayPaymentMethod::class.java, GooglePayPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(GenericPaymentMethod::class.java, "other")
                )
                .build()
        val jsonAdapter = moshi.adapter(PaymentsRequest::class.java)
        val requestString = jsonAdapter.toJson(paymentsRequest)

        val request = JSONObject(requestString)
        request.remove("paymentMethod")
        request.put("paymentMethod", paymentComponentData.getJSONObject("paymentMethod"))

        Logger.v(TAG, "requestJson - ${request.toString(JsonUtils.IDENT_SPACES)}")

        val requestBody = RequestBody.create(MediaType.parse("application/json"), request.toString())

        val call = CheckoutApiService.INSTANCE.payments(requestBody)

        return try {
            val response = call.execute()
            val paymentsResponse = response.body()

            // Error body
            val byteArray = response.errorBody()?.bytes()
            if (byteArray != null) {
                Logger.e(TAG, "errorBody - ${String(byteArray)}")
            }

            if (response.isSuccessful && paymentsResponse != null) {

                if (paymentsResponse.action != null) {
                    CallResult(CallResult.ResultType.ACTION, Action.SERIALIZER.serialize(paymentsResponse.action).toString())
                } else {
                    CallResult(CallResult.ResultType.FINISHED, paymentsResponse.resultCode ?: "EMPTY")
                }
            } else {
                Logger.e(TAG, "FAILED - ${response.message()}")
                CallResult(CallResult.ResultType.ERROR, "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            CallResult(CallResult.ResultType.ERROR, "IOException")
        }
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        Logger.d(TAG, "makeDetailsCall \n${actionComponentData.toString(JsonUtils.IDENT_SPACES)}")

        val requestBody = RequestBody.create(MediaType.parse("application/json"), actionComponentData.toString())
        val call = CheckoutApiService.INSTANCE.details(requestBody)

        return try {
            val response = call.execute()
            val detailsResponse = response.body()

            if (response.isSuccessful && detailsResponse != null) {
                if (detailsResponse.action != null) {
                    CallResult(CallResult.ResultType.ACTION, Action.SERIALIZER.serialize(detailsResponse.action).toString())
                } else {
                    CallResult(CallResult.ResultType.FINISHED, detailsResponse.resultCode ?: "EMPTY")
                }
            } else {
                Logger.e(TAG, "FAILED - ${response.message()}")
                CallResult(CallResult.ResultType.ERROR, "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            CallResult(CallResult.ResultType.ERROR, "IOException")
        }
    }
}
