/*
 * Copyright 2011-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.google.common.base.Charsets;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.VersionedChecksummedBytes;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.Wallet.BalanceType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.schildbach.wallet.Configuration;
import de.schildbach.wallet.Constants;
import de.schildbach.wallet.R;
import de.schildbach.wallet.WalletApplication;
import de.schildbach.wallet.data.AddressBookProvider;
import de.schildbach.wallet.data.PaymentIntent;
import de.schildbach.wallet.ui.InputParser.BinaryInputParser;
import de.schildbach.wallet.ui.InputParser.StringInputParser;
import de.schildbach.wallet.ui.preference.PreferenceActivity;
import de.schildbach.wallet.ui.send.SendCoinsActivity;
import de.schildbach.wallet.ui.send.SweepWalletActivity;
import de.schildbach.wallet.util.CrashReporter;
import de.schildbach.wallet.util.Crypto;
import de.schildbach.wallet.util.Io;
import de.schildbach.wallet.util.Nfc;
import de.schildbach.wallet.util.WalletUtils;

/**
 * @author Andreas Schildbach
 */
public final class PetsStarActivity extends AbstractBindServiceActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,View.OnClickListener {
    private static final int DIALOG_BACKUP_WALLET_PERMISSION = 0;
    private static final int DIALOG_RESTORE_WALLET_PERMISSION = 1;
    private static final int DIALOG_RESTORE_WALLET = 2;

    private WalletApplication application;
    private Configuration config;
    private Wallet wallet;

    private Handler handler = new Handler();

    private static final int REQUEST_CODE_SCAN = 0;
    private static final int REQUEST_CODE_BACKUP_WALLET = 1;
    private static final int REQUEST_CODE_RESTORE_WALLET = 2;

    private TextView subStarLevelView;
    private TextView subStarOreView;
    private ViewAnimator vaInfoView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        setContentView(R.layout.pets_star_activity);

        final View exchangeRatesFragment = findViewById(R.id.wallet_main_twopanes_exchange_rates);
        if (exchangeRatesFragment != null)
            exchangeRatesFragment.setVisibility(Constants.ENABLE_EXCHANGE_RATES ? View.VISIBLE : View.GONE);

        if (savedInstanceState == null) {
            final View contentView = findViewById(android.R.id.content);
            final View slideInLeftView = contentView.findViewWithTag("slide_in_left");
            if (slideInLeftView != null)
                slideInLeftView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
            final View slideInRightView = contentView.findViewWithTag("slide_in_right");
            if (slideInRightView != null)
                slideInRightView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            final View slideInTopView = contentView.findViewWithTag("slide_in_top");
            if (slideInTopView != null)
                slideInTopView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_top));
            final View slideInBottomView = contentView.findViewWithTag("slide_in_bottom");
            if (slideInBottomView != null)
                slideInBottomView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom));

            checkSavedCrashTrace();
        }


        subStarLevelView = findViewById(R.id.sub_star_level);
        subStarLevelView.setOnClickListener(this);
        subStarOreView = findViewById(R.id.sub_star_ore);
        subStarOreView.setOnClickListener(this);

        findViewById(R.id.sjxj).setOnClickListener(this);
        findViewById(R.id.gmkx).setOnClickListener(this);
        findViewById(R.id.gmbz).setOnClickListener(this);
        findViewById(R.id.gmcj).setOnClickListener(this);
        findViewById(R.id.gmdj).setOnClickListener(this);


        vaInfoView =  findViewById(R.id.va_info);

        config.touchLastUsed();

        handleIntent(getIntent());

        final FragmentManager fragmentManager = getFragmentManager();
        MaybeMaintenanceFragment.add(fragmentManager);
        AlertDialogsFragment.add(fragmentManager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // delayed start so that UI has enough time to initialize
                getWalletApplication().startBlockchainService(true);
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacksAndMessages(null);

        super.onPause();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(final Intent intent) {
        final String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            final String inputType = intent.getType();
            final NdefMessage ndefMessage = (NdefMessage) intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0];
            final byte[] input = Nfc.extractMimePayload(Constants.MIMETYPE_TRANSACTION, ndefMessage);

            new BinaryInputParser(inputType, input) {
                @Override
                protected void handlePaymentIntent(final PaymentIntent paymentIntent) {
                    cannotClassify(inputType);
                }

                @Override
                protected void error(final int messageResId, final Object... messageArgs) {
                    dialog(PetsStarActivity.this, null, 0, messageResId, messageArgs);
                }
            }.parse();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults) {
        if (requestCode == REQUEST_CODE_BACKUP_WALLET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                handleBackupWallet();
            else
                showDialog(DIALOG_BACKUP_WALLET_PERMISSION);
        } else if (requestCode == REQUEST_CODE_RESTORE_WALLET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                handleRestoreWallet();
            else
                showDialog(DIALOG_RESTORE_WALLET_PERMISSION);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            final String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);

            new StringInputParser(input) {
                @Override
                protected void handlePaymentIntent(final PaymentIntent paymentIntent) {
                    SendCoinsActivity.start(PetsStarActivity.this, paymentIntent);
                }

                @Override
                protected void handlePrivateKey(final VersionedChecksummedBytes key) {
                    if (Constants.ENABLE_SWEEP_WALLET)
                        SweepWalletActivity.start(PetsStarActivity.this, key);
                    else
                        super.handlePrivateKey(key);
                }

                @Override
                protected void handleDirectTransaction(final Transaction tx) throws VerificationException {
                    application.processDirectTransaction(tx);
                }

                @Override
                protected void error(final int messageResId, final Object... messageArgs) {
                    dialog(PetsStarActivity.this, null, R.string.button_scan, messageResId, messageArgs);
                }
            }.parse();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wallet_options_request:
                handleRequestCoins();
                return true;

            case R.id.wallet_options_send:
                handleSendCoins();
                return true;

            case R.id.wallet_options_scan:
                handleScan();
                return true;

            case R.id.wallet_options_address_book:
                AddressBookActivity.start(this);
                return true;

            case R.id.wallet_options_exchange_rates:
                startActivity(new Intent(this, ExchangeRatesActivity.class));
                return true;

            case R.id.wallet_options_sweep_wallet:
                SweepWalletActivity.start(this);
                return true;

            case R.id.wallet_options_network_monitor:
                startActivity(new Intent(this, NetworkMonitorActivity.class));
                return true;

            case R.id.wallet_options_restore_wallet:
                handleRestoreWallet();
                return true;

            case R.id.wallet_options_backup_wallet:
                handleBackupWallet();
                return true;

            case R.id.wallet_options_encrypt_keys:
                handleEncryptKeys();
                return true;

            case R.id.wallet_options_preferences:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;

            case R.id.wallet_options_safety:
                HelpDialogFragment.page(getFragmentManager(), R.string.help_safety);
                return true;

            case R.id.wallet_options_technical_notes:
                HelpDialogFragment.page(getFragmentManager(), R.string.help_technical_notes);
                return true;

            case R.id.wallet_options_report_issue:
                handleReportIssue();
                return true;

            case R.id.wallet_options_help:
                HelpDialogFragment.page(getFragmentManager(), R.string.help_wallet);
                return true;

            case R.id.pets_school:
                startActivity(new Intent(this, NetworkMonitorActivity.class));
                return true;

            case R.id.pets_star:
                HelpDialogFragment.page(getFragmentManager(), R.string.pets_star);

                return true;
        }

        if(item.getTitle() == null){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearSubSelect(){
        int color = 0xffcccccc;
        int drawableId = R.drawable.btn_corner_bluegray_bg2;


        subStarLevelView.setTextColor(color);
        subStarLevelView.setBackground(ContextCompat.getDrawable(this,drawableId));
        subStarOreView.setTextColor(color);
        subStarOreView.setBackground(ContextCompat.getDrawable(this,drawableId));
    }
    private void subSelect(TextView t){
        int color = 0xffffffff;
        int drawableId = R.drawable.btn_corner_bluegray_bg;
        t.setTextColor(color);
        t.setBackground(ContextCompat.getDrawable(this,drawableId));
    }

    @Override
    public void onClick(final View v) {
        clearSubSelect();

        long coinUnit = 100000000l;
        switch (v.getId()) {
            case R.id.sub_star_level:
                subSelect(subStarLevelView);
                vaInfoView.setDisplayedChild(0);
                break;
            case R.id.sub_star_ore:
                subSelect(subStarOreView);
                vaInfoView.setDisplayedChild(1);
                break;

            case R.id.sjxj:
                SendCoinsActivity.start(this, PaymentIntent.from(Constants.PLANET_ADDRESS,"矿星",Coin.valueOf(20000l*coinUnit)));
                break;
            case R.id.gmkx:
                SendCoinsActivity.start(this, PaymentIntent.fromAddress(Constants.PLANET_ADDRESS,null));
                break;
            case R.id.gmbz:
                SendCoinsActivity.start(this, PaymentIntent.from(Constants.PLANET_ADDRESS,"矿星",Coin.valueOf(1000000l*coinUnit)));
                break;
            case R.id.gmcj:
                SendCoinsActivity.start(this, PaymentIntent.from(Constants.PLANET_ADDRESS,"矿星",Coin.valueOf(10000000l*coinUnit)));
                break;
            case R.id.gmdj:
                SendCoinsActivity.start(this, PaymentIntent.from(Constants.PLANET_ADDRESS,"矿星",Coin.valueOf(100000000l*coinUnit)));
                break;


        }
    }

    public void handleRequestCoins() {
        startActivity(new Intent(this, RequestCoinsActivity.class));
    }

    public void handleSendCoins() {
        startActivity(new Intent(this, SendCoinsActivity.class));
    }

    public void handleScan() {
        startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE_SCAN);
    }

    public void handleBackupWallet() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            BackupWalletDialogFragment.show(getFragmentManager());
        else
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_CODE_BACKUP_WALLET);
    }

    public void handleRestoreWallet() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            showDialog(DIALOG_RESTORE_WALLET);
        else
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_CODE_RESTORE_WALLET);
    }

    public void handleEncryptKeys() {
        EncryptKeysDialogFragment.show(getFragmentManager());
    }

    private void handleReportIssue() {
        final ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(this,
                R.string.report_issue_dialog_title_issue, R.string.report_issue_dialog_message_issue) {
            @Override
            protected String subject() {
                return Constants.REPORT_SUBJECT_ISSUE + ": " + WalletApplication.versionLine(application.packageInfo());
            }

            @Override
            protected CharSequence collectApplicationInfo() throws IOException {
                final StringBuilder applicationInfo = new StringBuilder();
                CrashReporter.appendApplicationInfo(applicationInfo, application);
                return applicationInfo;
            }

            @Override
            protected CharSequence collectDeviceInfo() throws IOException {
                final StringBuilder deviceInfo = new StringBuilder();
                CrashReporter.appendDeviceInfo(deviceInfo, PetsStarActivity.this);
                return deviceInfo;
            }

            @Override
            protected CharSequence collectWalletDump() {
                return application.getWallet().toString(false, true, true, null);
            }
        };
        dialog.show();
    }

    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args) {
        if (id == DIALOG_BACKUP_WALLET_PERMISSION)
            return createBackupWalletPermissionDialog();
        else if (id == DIALOG_RESTORE_WALLET_PERMISSION)
            return createRestoreWalletPermissionDialog();
        else if (id == DIALOG_RESTORE_WALLET)
            return createRestoreWalletDialog();
        else
            throw new IllegalArgumentException();
    }

    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
        if (id == DIALOG_RESTORE_WALLET)
            prepareRestoreWalletDialog(dialog);
    }

    private Dialog createBackupWalletPermissionDialog() {
        final DialogBuilder dialog = new DialogBuilder(this);
        dialog.setTitle(R.string.backup_wallet_permission_dialog_title);
        dialog.setMessage(getString(R.string.backup_wallet_permission_dialog_message));
        dialog.singleDismissButton(null);
        return dialog.create();
    }

    private Dialog createRestoreWalletPermissionDialog() {
        final DialogBuilder dialog = new DialogBuilder(this);
        dialog.setTitle(R.string.restore_wallet_permission_dialog_title);
        dialog.setMessage(getString(R.string.restore_wallet_permission_dialog_message));
        dialog.singleDismissButton(null);
        return dialog.create();
    }

    private Dialog createRestoreWalletDialog() {
        final View view = getLayoutInflater().inflate(R.layout.restore_wallet_dialog, null);
        final Spinner fileView = (Spinner) view.findViewById(R.id.import_keys_from_storage_file);
        final EditText passwordView = (EditText) view.findViewById(R.id.import_keys_from_storage_password);

        final DialogBuilder dialog = new DialogBuilder(this);
        dialog.setTitle(R.string.import_keys_dialog_title);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.import_keys_dialog_button_import, new OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                final File file = (File) fileView.getSelectedItem();
                final String password = passwordView.getText().toString().trim();
                passwordView.setText(null); // get rid of it asap

                if (WalletUtils.BACKUP_FILE_FILTER.accept(file))
                    restoreWalletFromProtobuf(file);
                else if (WalletUtils.KEYS_FILE_FILTER.accept(file))
                    restorePrivateKeysFromBase58(file);
                else if (Crypto.OPENSSL_FILE_FILTER.accept(file))
                    restoreWalletFromEncrypted(file, password);
            }
        });
        dialog.setNegativeButton(R.string.button_cancel, new OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                passwordView.setText(null); // get rid of it asap
            }
        });
        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                passwordView.setText(null); // get rid of it asap
            }
        });

        fileView.setAdapter(new FileAdapter(this) {
            @Override
            public View getDropDownView(final int position, View row, final ViewGroup parent) {
                final File file = getItem(position);
                final boolean isExternal = Constants.Files.EXTERNAL_WALLET_BACKUP_DIR.equals(file.getParentFile());
                final boolean isEncrypted = Crypto.OPENSSL_FILE_FILTER.accept(file);

                if (row == null)
                    row = inflater.inflate(R.layout.restore_wallet_file_row, null);

                final TextView filenameView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_filename);
                filenameView.setText(file.getName());

                final TextView securityView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_security);
                final String encryptedStr = context
                        .getString(isEncrypted ? R.string.import_keys_dialog_file_security_encrypted
                                : R.string.import_keys_dialog_file_security_unencrypted);
                final String storageStr = context
                        .getString(isExternal ? R.string.import_keys_dialog_file_security_external
                                : R.string.import_keys_dialog_file_security_internal);
                securityView.setText(encryptedStr + ", " + storageStr);

                final TextView createdView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_created);
                createdView.setText(context.getString(
                        isExternal ? R.string.import_keys_dialog_file_created_manual
                                : R.string.import_keys_dialog_file_created_automatic,
                        DateUtils.getRelativeTimeSpanString(context, file.lastModified(), true)));

                return row;
            }
        });

        return dialog.create();
    }

    private void prepareRestoreWalletDialog(final Dialog dialog) {
        final AlertDialog alertDialog = (AlertDialog) dialog;

        final String path;
        final String backupPath = Constants.Files.EXTERNAL_WALLET_BACKUP_DIR.getAbsolutePath();
        final String storagePath = Constants.Files.EXTERNAL_STORAGE_DIR.getAbsolutePath();
        if (backupPath.startsWith(storagePath))
            path = backupPath.substring(storagePath.length());
        else
            path = backupPath;

        final List<File> files = new LinkedList<File>();

        // external storage
        final File[] externalFiles = Constants.Files.EXTERNAL_WALLET_BACKUP_DIR.listFiles();
        if (externalFiles != null)
            for (final File file : externalFiles)
                if (Crypto.OPENSSL_FILE_FILTER.accept(file))
                    files.add(file);

        // internal storage
        for (final String filename : fileList())
            if (filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF + '.'))
                files.add(new File(getFilesDir(), filename));

        // sort
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(final File lhs, final File rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        final TextView messageView = (TextView) alertDialog.findViewById(R.id.restore_wallet_dialog_message);
        messageView.setText(getString(
                !files.isEmpty() ? R.string.import_keys_dialog_message : R.string.restore_wallet_dialog_message_empty,
                path));

        final Spinner fileView = (Spinner) alertDialog.findViewById(R.id.import_keys_from_storage_file);
        fileView.setVisibility(!files.isEmpty() ? View.VISIBLE : View.GONE);
        final FileAdapter adapter = (FileAdapter) fileView.getAdapter();
        adapter.setFiles(files);

        final EditText passwordView = (EditText) alertDialog.findViewById(R.id.import_keys_from_storage_password);
        passwordView.setVisibility(!files.isEmpty() ? View.VISIBLE : View.GONE);
        passwordView.setText(null);

        final CheckBox showView = (CheckBox) alertDialog.findViewById(R.id.import_keys_from_storage_show);
        showView.setVisibility(!files.isEmpty() ? View.VISIBLE : View.GONE);
        showView.setOnCheckedChangeListener(new ShowPasswordCheckListener(passwordView));

        final View replaceWarningView = alertDialog
                .findViewById(R.id.restore_wallet_from_storage_dialog_replace_warning);
        final boolean hasCoins = wallet.getBalance(BalanceType.ESTIMATED).signum() > 0;
        replaceWarningView.setVisibility(hasCoins ? View.VISIBLE : View.GONE);

        final ImportDialogButtonEnablerListener dialogButtonEnabler = new ImportDialogButtonEnablerListener(
                passwordView, alertDialog) {
            @Override
            protected boolean hasFile() {
                return fileView.getSelectedItem() != null;
            }

            @Override
            protected boolean needsPassword() {
                final File selectedFile = (File) fileView.getSelectedItem();
                return selectedFile != null ? Crypto.OPENSSL_FILE_FILTER.accept(selectedFile) : false;
            }
        };
        passwordView.addTextChangedListener(dialogButtonEnabler);
        fileView.setOnItemSelectedListener(dialogButtonEnabler);
    }

    private void checkSavedCrashTrace() {
        if (CrashReporter.hasSavedCrashTrace()) {
            final StringBuilder stackTrace = new StringBuilder();

            try {
                CrashReporter.appendSavedCrashTrace(stackTrace);
            } catch (final IOException x) {
                log.info("problem appending crash info", x);
            }

            final ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(this,
                    R.string.report_issue_dialog_title_crash, R.string.report_issue_dialog_message_crash) {
                @Override
                protected String subject() {
                    final PackageInfo packageInfo = getWalletApplication().packageInfo();
                    return Constants.REPORT_SUBJECT_CRASH + ": " + WalletApplication.versionLine(packageInfo);
                }

                @Override
                protected CharSequence collectApplicationInfo() throws IOException {
                    final StringBuilder applicationInfo = new StringBuilder();
                    CrashReporter.appendApplicationInfo(applicationInfo, application);
                    return applicationInfo;
                }

                @Override
                protected CharSequence collectStackTrace() throws IOException {
                    if (stackTrace.length() > 0)
                        return stackTrace;
                    else
                        return null;
                }

                @Override
                protected CharSequence collectDeviceInfo() throws IOException {
                    final StringBuilder deviceInfo = new StringBuilder();
                    CrashReporter.appendDeviceInfo(deviceInfo, PetsStarActivity.this);
                    return deviceInfo;
                }

                @Override
                protected CharSequence collectWalletDump() {
                    return wallet.toString(false, true, true, null);
                }
            };

            dialog.show();
        }
    }

    private void restoreWalletFromEncrypted(final File file, final String password) {
        try {
            final BufferedReader cipherIn = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
            final StringBuilder cipherText = new StringBuilder();
            Io.copy(cipherIn, cipherText, Constants.BACKUP_MAX_CHARS);
            cipherIn.close();

            final byte[] plainText = Crypto.decryptBytes(cipherText.toString(), password.toCharArray());
            final InputStream is = new ByteArrayInputStream(plainText);

            restoreWallet(WalletUtils.restoreWalletFromProtobufOrBase58(is, Constants.NETWORK_PARAMETERS));

            log.info("successfully restored encrypted wallet: {}", file);
        } catch (final IOException x) {
            final DialogBuilder dialog = DialogBuilder.warn(this, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(getString(R.string.import_keys_dialog_failure, x.getMessage()));
            dialog.setPositiveButton(R.string.button_dismiss, null);
            dialog.setNegativeButton(R.string.button_retry, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id) {
                    showDialog(DIALOG_RESTORE_WALLET);
                }
            });
            dialog.show();

            log.info("problem restoring wallet: " + file, x);
        }
    }

    private void restoreWalletFromProtobuf(final File file) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            restoreWallet(WalletUtils.restoreWalletFromProtobuf(is, Constants.NETWORK_PARAMETERS));

            log.info("successfully restored unencrypted wallet: {}", file);
        } catch (final IOException x) {
            final DialogBuilder dialog = DialogBuilder.warn(this, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(getString(R.string.import_keys_dialog_failure, x.getMessage()));
            dialog.setPositiveButton(R.string.button_dismiss, null);
            dialog.setNegativeButton(R.string.button_retry, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id) {
                    showDialog(DIALOG_RESTORE_WALLET);
                }
            });
            dialog.show();

            log.info("problem restoring unencrypted wallet: " + file, x);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException x2) {
                    // swallow
                }
            }
        }
    }

    private void restorePrivateKeysFromBase58(final File file) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            restoreWallet(WalletUtils.restorePrivateKeysFromBase58(is, Constants.NETWORK_PARAMETERS));

            log.info("successfully restored unencrypted private keys: {}", file);
        } catch (final IOException x) {
            final DialogBuilder dialog = DialogBuilder.warn(this, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(getString(R.string.import_keys_dialog_failure, x.getMessage()));
            dialog.setPositiveButton(R.string.button_dismiss, null);
            dialog.setNegativeButton(R.string.button_retry, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id) {
                    showDialog(DIALOG_RESTORE_WALLET);
                }
            });
            dialog.show();

            log.info("problem restoring private keys: " + file, x);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException x2) {
                    // swallow
                }
            }
        }
    }

    private void restoreWallet(final Wallet wallet) throws IOException {
        application.replaceWallet(wallet);

        config.disarmBackupReminder();

        final DialogBuilder dialog = new DialogBuilder(this);
        final StringBuilder message = new StringBuilder();
        message.append(getString(R.string.restore_wallet_dialog_success));
        message.append("\n\n");
        message.append(getString(R.string.restore_wallet_dialog_success_replay));
        if (wallet.isEncrypted()) {
            message.append("\n\n");
            message.append(getString(R.string.restore_wallet_dialog_success_encrypted));
        }
        dialog.setMessage(message);
        dialog.setNeutralButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                getWalletApplication().resetBlockchain();
                finish();
            }
        });
        dialog.show();
    }

    private  int petBorn = 99;

    private class TransModel{
        public TransModel(int depth,long hitAmount,Date time,String address,String sendTxid){
            this.depth = depth;
            this.hitAmount = hitAmount;
            this.time = time;
            this.address = address;
            this.sendTxid = sendTxid;
        }
        //确认数
        int depth = 0;
        //转账数
        long hitAmount = 0;
        // time
        Date time ;
        //转账地址
        String address =  "" ;
        String sendTxid = "";

    }

    private class PlanetGetInfo {
        double totalResource;// 总资源;
        int uiNorPlanetCount;
        int uiSupperPlanetCount;
        int uiTopPlanetCount;
        double dTotalInCome; // 总收益;
        double dTotalFenhong; // 分红;
        boolean IsAbleBuyMore; // 超过10个矿星不让继续购买;
        int  uiPlanetLevel;
    };

    private TransModel getTransModel(Transaction tx, final Map<Sha256Hash,TransactionsAdapter.TransactionCacheEntry>  transactionCache){
        final TransactionConfidence confidence = tx.getConfidence();
        final Transaction.Purpose purpose = tx.getPurpose();
        final Coin fee = tx.getFee();

        TransactionsAdapter.TransactionCacheEntry txCache = transactionCache.get(tx.getHash());
        if (txCache == null) {
            final Coin value = tx.getValue(wallet);
            final boolean sent = value.signum() < 0;
            final boolean self = WalletUtils.isEntirelySelf(tx, wallet);
            final boolean showFee = sent && fee != null && !fee.isZero();

            final Address address;
            if (sent)
                address = WalletUtils.getToAddressOfSent(tx, wallet);
            else
                address = WalletUtils.getWalletAddressOfReceived(tx, wallet);
            final String addressLabel = address != null
                    ? AddressBookProvider.resolveLabel(this, address.toBase58()) : null;

            txCache = new TransactionsAdapter.TransactionCacheEntry(value, sent, self, showFee, address, addressLabel);
            transactionCache.put(tx.getHash(), txCache);
        }

        final Coin value ;
        if (purpose == Transaction.Purpose.RAISE_FEE) {
            return null;
        } else {
            if(txCache == null){
                value = tx.getValue(wallet).add(fee);
            }else {
                if(fee != null)
                    value = txCache.value.add(fee);
                else
                    value = txCache.value;
            }
        }

        //确认数
        int depth = confidence.getDepthInBlocks();
        //转账数
        long hitAmount = value.getValue();
        // time
        final Date time = tx.getUpdateTime();
        //转账地址
        String address = txCache.address.toString() ;

        String sendTxid = tx.getHashAsString();

        if(hitAmount<0){
            hitAmount = 0-hitAmount;
            hitAmount = hitAmount/100000000;
        }
        return new TransModel(depth,hitAmount,time,address,sendTxid);
    }

    private int getPetPropetyNumber(String key,int level){
        int[][] values = Constants.PET_PROPETY_NUMBER.get(key);
        if(values == null)
            return 0;
        if(level == 99 || level == 444)
            return 0;
        for(int i=0;i<values.length;i++){
            if(values[i][0] == level)
                return values[i][1];
        }
        return 0;
    }

    private boolean isPetDestroy(final List<Transaction> transactions,final Map<Sha256Hash, TransactionsAdapter.TransactionCacheEntry>  transactionCache){

        boolean isDestroy = false;
        for (int i=0;i<transactions.size();i++) {


            Transaction tx = transactions.get(i);
            TransModel transModel = getTransModel(tx,transactionCache);


            if(Constants.DESTROY_ADDRESS.equals(transModel.address) && transModel.hitAmount == Constants.DESTROY_AMOUNT){
                isDestroy = true;
                break;
            }
        }
        return isDestroy;
    }

    private void refreshPetsUI(final List<Transaction> transactions,final Map<Sha256Hash, TransactionsAdapter.TransactionCacheEntry>  transactionCache){



        //查看宠物是否销毁
        boolean isDestroy = isPetDestroy(transactions,transactionCache);


        final View contentView = findViewById(android.R.id.content);

        int zhanli = 0;
        int feedCount = 0;
        String feedTimeTip = "";
        int juejin = 0;
        if(isDestroy){
            juejin = 0;
            petBorn = 444;
        }else {

            //获取几代接口


            petBorn = 99;
            for (int i=0;i<transactions.size();i++) {
                Transaction tx = transactions.get(i);
                TransModel transModel = getTransModel(tx,transactionCache);
                if(Constants.BOSS_ADDRESS.equals(transModel.address)){
                    int amount = (int) transModel.hitAmount;
                    switch (amount)
                    {
                        case 1000000000:
                            petBorn = -1;
                            break;
                        case 100000000:
                            petBorn = 0;
                            break;
                        case 10000000:
                            petBorn = 1;
                            break;
                        case 1000000:
                            petBorn = 2;
                            break;
                        case 100000:
                            petBorn = 3;
                            break;
                        default:
                            petBorn = 99;
                    }
                    break;
                }

            }



            //获取属性接口


            Date lastFeedTime = null;
            Date nextFeedTime = null;

            if (petBorn != 99 && petBorn !=444){
                for (int i=0;i<transactions.size();i++) {
                    Transaction tx = transactions.get(i);
                    TransModel transModel = getTransModel(tx, transactionCache);

                    for (String feedAddress : Constants.FEED_ADDRESSES) {
                        if(feedAddress.equals(transModel.address)){
                            feedCount ++;
                            lastFeedTime = transModel.time;
                        }
                    }
                }
            }

            if(lastFeedTime != null){
                nextFeedTime =  new Date(lastFeedTime.getTime()+ 18*3600*1000);
                SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd HH:mm") ;
                feedTimeTip = "下次可喂养时间: " + format.format(nextFeedTime);
            }else {
                feedTimeTip = "请喂养您的宠物！" ;
            }


            juejin = feedCount * getPetPropetyNumber("juejin",petBorn);

            ArrayList<TransModel> transModelList = new ArrayList<>();

            for (int i=0;i<transactions.size();i++) {
                Transaction tx = transactions.get(i);
                TransModel transModel = getTransModel(tx, transactionCache);

                if(Constants.PLANET_ADDRESS.equals(transModel.address)){
                    transModelList.add(transModel);
                }
            }

            // 更新升级信息;
            int iupdatesucCount = getUpdateSucCounts(transModelList);
            final TextView starTextView = contentView.findViewWithTag("star");
            starTextView.setText(""+iupdatesucCount);

            final TextView jjzjcTextView = contentView.findViewWithTag("jjzjc");
            jjzjcTextView.setText(""+iupdatesucCount+"%");

            double zhjjz = 1.0 * juejin * (1+0.01*iupdatesucCount);
            final TextView zhjjzTextView = contentView.findViewWithTag("zhjjz");
            zhjjzTextView.setText(""+zhjjz);


            PlanetGetInfo info = getPlanetInfos(zhjjz,transModelList);

            //更新已经拥有的星球信息;
            // 标准星球个数
            final TextView bzkxTextView = contentView.findViewWithTag("bzkx");
            bzkxTextView.setText("标准矿星(" + info.uiNorPlanetCount + ")");

            // 超级星球个数
            final TextView cjkxTextView = contentView.findViewWithTag("cjkx");
            cjkxTextView.setText("超级矿星(" + info.uiSupperPlanetCount + ")");

            // 顶级矿星个数
            final TextView djkxTextView = contentView.findViewWithTag("djkx");
            djkxTextView.setText("顶级矿星(" + info.uiTopPlanetCount + ")");

            // 总资源
            final TextView zyTextView = contentView.findViewWithTag("zy");
            zyTextView.setText(" " + info.totalResource + "");

            // 掘金
            final TextView jjTextView = contentView.findViewWithTag("jj");
            jjTextView.setText(" " + String.format("%.2f", info.dTotalInCome)  + " ALX");

            // 分红
            final TextView fhTextView = contentView.findViewWithTag("fh");
            fhTextView.setText("" + info.dTotalFenhong + "");


            if(info.IsAbleBuyMore == false){
                final ImageView gmkxView = contentView.findViewById(R.id.gmkx);
                gmkxView.setEnabled(false);
            }

        }
    }

    PlanetGetInfo getPlanetInfos(double juejinValue,ArrayList<TransModel>  planets)
    {
        // 当前掘金值;
        PlanetGetInfo info = new PlanetGetInfo();
        int iCount = 0;
        int DaysPerYear = 365;

        // 待添加掘矿和分红;
        double tempvalue = 1.0;
        for ( int i = 0; i < planets.size(); i++)
        {
            TransModel model = planets.get(i);
            int uiDays = getPlanetOwnTime(model.time.getTime());
            if(model.hitAmount == Constants.PlanetNormalAmount) {
                // 计算值;
                iCount += 1;
                if(model.depth >= 1){
                    info.uiNorPlanetCount += 1;
                    info.totalResource += Constants.PlanetNormalAmount;
                    info.dTotalInCome += (double)(Constants.PlanetNormalAmount * juejinValue * uiDays) * Constants.IncomeCoinsPerYear  / DaysPerYear;
                    tempvalue += Constants.PlanetNormalValue;
                }
            } else if(model.hitAmount == Constants.PlanetSupperAmount){
                iCount += 1;
                if(model.depth >= 1){
                    info.uiSupperPlanetCount += 1;
                    info.totalResource += Constants.PlanetSupperAmount;
                    info.dTotalInCome += (double)(Constants.PlanetSupperAmount * juejinValue * uiDays) * Constants.IncomeCoinsPerYear / DaysPerYear;
                    tempvalue += Constants.PlanetSupperValue;
                }
            } else if(model.hitAmount == Constants.PlanetTopAmount){
                iCount += 1;
                if(model.depth >= 1){
                    info.uiTopPlanetCount += 1;
                    info.totalResource += Constants.PlanetTopAmount;
                    info.dTotalInCome += (double)(Constants.PlanetTopAmount * juejinValue * uiDays) * Constants.IncomeCoinsPerYear  / DaysPerYear;
                    tempvalue += Constants.PlanetTopValue;
                }
            }
        }
        if(iCount >= 10){
            info.IsAbleBuyMore = false;
            // 计算
            info.dTotalFenhong = (double)(juejinValue) * tempvalue  / DaysPerYear;
        } else {
            info.dTotalFenhong = 0.0;
            info.IsAbleBuyMore = true;
        }
        return info;
    }

    // 通过秒来计算实际天数;
    int getPlanetOwnTime(long begintime)
    {
        Date now = new Date();
        // 计算时间;
        int  result =(int) (now.getTime() - begintime);
        int onedaytimevalsec = 24*3600 * 1000;

        result = result/onedaytimevalsec;
        return result+1;
    }


    int getUpdateSucCounts(ArrayList<TransModel> list)
    {
        int iValidCount = 0;
        for (TransModel model:list)
        {

            if(model.depth >= 1 && Constants.PlanetUpdteAmount == model.hitAmount) {
                String strTxid = model.sendTxid;
                if(isUpdateSucc(strTxid)){
                    iValidCount += 1;
                }
            }
        }
        return iValidCount;
    }


    boolean isUpdateSucc(String strTxid)
    {
        int nNumberCount = 0;
        if(strTxid.length() < 5){
            return false;
        }

        for(int i = 0; i< 5; i++){
            if(strTxid.charAt(i) >= '0' && strTxid.charAt(i) <= '9'){
                nNumberCount += 1;
            }
        }
        return nNumberCount >= 3;
    }

    public void refrePets(final List<Transaction> transactions,final Map<Sha256Hash, TransactionsAdapter.TransactionCacheEntry> transactionCache){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshPetsUI(transactions,transactionCache);
            }
        }, 1000);
    }
}
