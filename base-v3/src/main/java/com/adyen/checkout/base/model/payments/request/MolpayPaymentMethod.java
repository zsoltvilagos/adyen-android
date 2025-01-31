/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exeption.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class MolpayPaymentMethod extends IssuerListPaymentMethod {
    @NonNull
    public static final Creator<MolpayPaymentMethod> CREATOR = new Creator<>(MolpayPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.MOLPAY;

    @NonNull
    public static final Serializer<MolpayPaymentMethod> SERIALIZER = new Serializer<MolpayPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull MolpayPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());
                jsonObject.putOpt(IssuerListPaymentMethod.ISSUER, modelObject.getIssuer());


            } catch (JSONException e) {
                throw new ModelSerializationException(MolpayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public MolpayPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final MolpayPaymentMethod idealPaymentMethod = new MolpayPaymentMethod();

            // getting parameters from parent class
            idealPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));
            idealPaymentMethod.setIssuer(jsonObject.optString(IssuerListPaymentMethod.ISSUER, null));

            return idealPaymentMethod;
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

}
