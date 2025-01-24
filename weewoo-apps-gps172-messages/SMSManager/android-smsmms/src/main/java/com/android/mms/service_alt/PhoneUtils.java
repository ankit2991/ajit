package com.android.mms.service_alt;

import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;

import java.lang.reflect.Method;
import java.util.Locale;

import timber.log.Timber;

public class PhoneUtils {

    public static String getNationalNumber(TelephonyManager telephonyManager, int subId,
                                           String phoneText) {
        final String country = getSimOrDefaultLocaleCountry(telephonyManager, subId);
        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        final Phonenumber.PhoneNumber parsed = getParsedNumber(phoneNumberUtil, phoneText, country);
        if (parsed == null) {
            return phoneText;
        }
        return phoneNumberUtil
                .format(parsed, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                .replaceAll("\\D", "");
    }

    private static Phonenumber.PhoneNumber getParsedNumber(PhoneNumberUtil phoneNumberUtil,
                                                           String phoneText, String country) {
        try {
            final Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneText, country);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                return phoneNumber;
            } else {
                Timber.e("getParsedNumber: not a valid phone number"
                        + " for country " + country);
                return null;
            }
        } catch (final NumberParseException e) {
            Timber.e("getParsedNumber: Not able to parse phone number");
            return null;
        }
    }

    private static String getSimOrDefaultLocaleCountry(TelephonyManager telephonyManager,
                                                       int subId) {
        String country = getSimCountry(telephonyManager, subId);
        if (TextUtils.isEmpty(country)) {
            country = Locale.getDefault().getCountry();
        }

        return country;
    }

    private static String getSimCountry(TelephonyManager telephonyManager, int subId) {
        String country = "";

        try {
            Method method = telephonyManager.getClass().getMethod("getSimCountryIso", int.class);
            country = (String) method.invoke(telephonyManager, subId);
        } catch (Exception e) {
            country = telephonyManager.getSimCountryIso();
        }

        if (TextUtils.isEmpty(country)) {
            return null;
        }
        return country.toUpperCase();
    }
}
