/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.component.BaseConfiguration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.card.model.CardType;
import com.adyen.checkout.core.api.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link Configuration} class required by {@link CardComponent} to change it's behavior. Pass it to the {@link CardComponent#PROVIDER}.
 */
public class CardConfiguration extends BaseConfiguration {

    private static final CardType[] DEFAULT_SUPPORTED_CARDS =
            new CardType[]{CardType.VISA, CardType.AMERICAN_EXPRESS, CardType.MASTERCARD};

    private final String mPublicKey;
    private final DisplayMetrics mDisplayMetrics;
    private final String mShopperReference;
    private final boolean mHolderNameRequire;
    private final List<CardType> mSupportedCardTypes;
    private final boolean mShowStorePaymentField;

    /**
     * Constructor with all parameters. You can use the Builder to initialize this object more easily.
     *
     * @param shopperLocale         The locale that should be used to display strings and layouts. Can differ from device default.
     * @param environment           The environment to be used to make network calls.
     * @param displayMetrics        The current {@link DisplayMetrics} of the device to fetch images of matching size.
     * @param publicKey             The public key used for encryption of the card data. You can get it from the Customer Area.
     * @param shopperReference      The unique identifier of the shopper.
     * @param holderNameRequire     If the holder name of the card should be shown as a required field.
     * @param showStorePaymentField If the component should show the option to store the card for later use.
     * @param supportCardTypes      The list of supported card brands to be shown to the user.
     */
    public CardConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull DisplayMetrics displayMetrics,
            @NonNull String publicKey,
            boolean holderNameRequire,
            @NonNull String shopperReference,
            boolean showStorePaymentField,
            @NonNull CardType... supportCardTypes) {
        super(shopperLocale, environment);

        mPublicKey = publicKey;
        mDisplayMetrics = displayMetrics;
        mHolderNameRequire = holderNameRequire;
        mSupportedCardTypes = Collections.unmodifiableList(Arrays.asList(supportCardTypes));
        mShopperReference = shopperReference;
        mShowStorePaymentField = showStorePaymentField;
    }

    /**
     * Get public key.
     *
     * @return {@link String}
     */
    @NonNull
    public String getPublicKey() {
        return mPublicKey;
    }

    /**
     * The list of {@link CardType} that this payment supports. Used to predict the card type of the
     *
     * @return The list of {@link CardType}.
     */
    @NonNull
    public List<CardType> getSupportedCardTypes() {
        return mSupportedCardTypes;
    }

    /**
     * @return If the Holder Name is required for this Card payment.
     */
    public boolean isHolderNameRequire() {
        return mHolderNameRequire;
    }

    /**
     * Get display metrics.
     *
     * @return {@link DisplayMetrics}
     */
    @NonNull
    public DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    @Nullable
    public String getShopperReference() {
        return mShopperReference;
    }

    public boolean isShowStorePaymentFieldEnable() {
        return mShowStorePaymentField;
    }

    /**
     * Builder to create a {@link CardConfiguration} more easily.
     */
    public static final class Builder extends BaseConfigurationBuilder<CardConfiguration> {

        private String mBuilderPublicKey;
        private DisplayMetrics mBuilderDisplayMetrics;

        private CardType[] mBuilderSupportedCardTypes = DEFAULT_SUPPORTED_CARDS;
        private boolean mBuilderHolderNameRequire;
        private boolean mBuilderShowStorePaymentField = true;
        private String mShopperReference;

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        public Builder(@NonNull Context context, @NonNull String publicKey) {
            super(context);
            mBuilderDisplayMetrics = context.getResources().getDisplayMetrics();
            mBuilderPublicKey = publicKey;
        }

        /**
         * Builder with required parameters for a {@link CardConfiguration}.
         *
         * @param shopperLocale     The Locale of the shopper.
         * @param environment       The {@link Environment} to be used for network calls to Adyen.
         * @param displayMetrics    The DisplayMetrics to fetch images with the correct size.
         * @param publicKey         The public key used for encryption of the card data. You can get it from the Customer Area.
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @NonNull DisplayMetrics displayMetrics,
                @NonNull String publicKey) {
            super(shopperLocale, environment);
            mBuilderDisplayMetrics = displayMetrics;
            mBuilderPublicKey = publicKey;
        }

        /**
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        public void setPublicKey(@NonNull String publicKey) {
            mBuilderPublicKey = publicKey;
        }

        /**
         * @param displayMetrics The DisplayMetrics to fetch images with the correct size.
         */
        public void setDisplayMetrics(@NonNull DisplayMetrics displayMetrics) {
            mBuilderDisplayMetrics = displayMetrics;
        }

        /**
         * Set supported card types for card-payment.
         *
         * @param supportCardTypes array of {@link CardType}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setSupportedCardTypes(@NonNull CardType... supportCardTypes) {
            this.mBuilderSupportedCardTypes = supportCardTypes;
            return this;
        }

        /**
         * Set that if holder name require.
         *
         * @param holderNameRequire {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setHolderNameRequire(boolean holderNameRequire) {
            this.mBuilderHolderNameRequire = holderNameRequire;
            return this;
        }

        /**
         * Show store payment field.
         *
         * @param showStorePaymentField {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setShowStorePaymentField(boolean showStorePaymentField) {
            this.mBuilderShowStorePaymentField = showStorePaymentField;
            return this;
        }

        @NonNull
        public Builder setShopperReference(@NonNull String shopperReference) {
            this.mShopperReference = shopperReference;
            return this;
        }

        /**
         * Build {@link CardConfiguration} object from {@link CardConfiguration.Builder} inputs.
         *
         * @return {@link CardConfiguration}
         */
        @NonNull
        public CardConfiguration build() {
            return new CardConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderDisplayMetrics,
                    mBuilderPublicKey,
                    mBuilderHolderNameRequire,
                    mShopperReference,
                    mBuilderShowStorePaymentField,
                    mBuilderSupportedCardTypes
            );
        }
    }

}
