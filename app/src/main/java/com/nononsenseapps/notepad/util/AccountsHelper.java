package com.nononsenseapps.notepad.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.nononsenseapps.notepad.data.local.sql.MyContentProvider;
import com.nononsenseapps.notepad.ui.settings.Constants;

public class AccountsHelper {
    /**
     * Finds and returns the account of the name given
     */
    @SuppressWarnings("MissingPermission")
    public static Account getAccount(AccountManager manager, String accountName) {
        Account[] accounts = manager.getAccountsByType("com.google");
        for (Account account : accounts) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    public static void setSyncInterval(Context activity,
                                       SharedPreferences sharedPreferences) {
        String accountName = sharedPreferences.getString(Constants.KEY_ACCOUNT, "");
        boolean backgroundSync = sharedPreferences.getBoolean(
                Constants.KEY_BACKGROUND_SYNC, false);

        if (!accountName.isEmpty()) {
            Account account = getAccount(AccountManager.get(activity), accountName);
            if (account != null) {
                if (!backgroundSync) {
                    // Disable periodic syncing
                    ContentResolver.removePeriodicSync(
                            account,
                            MyContentProvider.AUTHORITY, new Bundle());
                } else {
                    // Convert from minutes to seconds
                    long pollFrequency = 3600;
                    // Set periodic syncing
                    ContentResolver.addPeriodicSync(
                            account,
                            MyContentProvider.AUTHORITY, new Bundle(),
                            pollFrequency);
                }
            }
        }
    }
}
