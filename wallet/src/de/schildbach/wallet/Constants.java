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

package de.schildbach.wallet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.BaseEncoding;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import de.schildbach.wallet.R;

import android.os.Build;
import android.os.Environment;
import android.text.format.DateUtils;

/**
 * @author Andreas Schildbach
 */
public final class Constants {
    public static final boolean TEST = R.class.getPackage().getName().contains("_test");

    /** Network this wallet is on (e.g. testnet or mainnet). */
    public static final NetworkParameters NETWORK_PARAMETERS = TEST ? TestNet3Params.get() : MainNetParams.get();

    /** Bitcoinj global context. */
    public static final Context CONTEXT = new Context(NETWORK_PARAMETERS);

    /** Enable switch for synching of the blockchain */
    public static final boolean ENABLE_BLOCKCHAIN_SYNC = true;
    /** Enable switch for fetching and showing of exchange rates */
    public static final boolean ENABLE_EXCHANGE_RATES = false;
    /** Enable switch for sweeping of paper wallets */
    public static final boolean ENABLE_SWEEP_WALLET = true;
    /** Enable switch for browsing to block explorers */
    public static final boolean ENABLE_BROWSE = true;

    public final static class Files {
        private static final String FILENAME_NETWORK_SUFFIX = NETWORK_PARAMETERS.getId()
                .equals(NetworkParameters.ID_MAINNET) ? "" : "-testnet";

        /** Filename of the wallet. */
        public static final String WALLET_FILENAME_PROTOBUF = "wallet-protobuf" + FILENAME_NETWORK_SUFFIX;

        /** How often the wallet is autosaved. */
        public static final long WALLET_AUTOSAVE_DELAY_MS = 3 * DateUtils.SECOND_IN_MILLIS;

        /** Filename of the automatic key backup (old format, can only be read). */
        public static final String WALLET_KEY_BACKUP_BASE58 = "key-backup-base58" + FILENAME_NETWORK_SUFFIX;

        /** Filename of the automatic wallet backup. */
        public static final String WALLET_KEY_BACKUP_PROTOBUF = "key-backup-protobuf" + FILENAME_NETWORK_SUFFIX;

        /** Path to external storage */
        public static final File EXTERNAL_STORAGE_DIR = Environment.getExternalStorageDirectory();

        /** Manual backups go here. */
        public static final File EXTERNAL_WALLET_BACKUP_DIR = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        /** Filename of the manual key backup (old format, can only be read). */
        public static final String EXTERNAL_WALLET_KEY_BACKUP = "alchain-wallet-keys" + FILENAME_NETWORK_SUFFIX;

        /** Filename of the manual wallet backup. */
        public static final String EXTERNAL_WALLET_BACKUP = "alchain-wallet-backup" + FILENAME_NETWORK_SUFFIX;

        /** Suffix for the subject of the manual wallet backup. */
        public static final String EXTERNAL_WALLET_BACKUP_SUBJECT_SUFFIX = NETWORK_PARAMETERS.getId()
                .equals(NetworkParameters.ID_MAINNET) ? "" : " [testnet3]";

        /** Filename of the block store for storing the chain. */
        public static final String BLOCKCHAIN_FILENAME = "blockchain" + FILENAME_NETWORK_SUFFIX;

        /** Filename of the block checkpoints file. */
        public static final String CHECKPOINTS_FILENAME = "checkpoints" + FILENAME_NETWORK_SUFFIX + ".txt";

        /** Filename of the fees files. */
        public static final String FEES_FILENAME = "fees" + FILENAME_NETWORK_SUFFIX + ".txt";

        /** Filename of the file containing Electrum servers. */
        public static final String ELECTRUM_SERVERS_FILENAME = "electrum-servers.txt";
    }

    /** Maximum size of backups. Files larger will be rejected. */
    public static final long BACKUP_MAX_CHARS = 10000000;

    /** Currency code for the wallet name resolver. */
    public static final String WALLET_NAME_CURRENCY_CODE = NETWORK_PARAMETERS.getId()
            .equals(NetworkParameters.ID_MAINNET) ? "ALC" : "tALC";

    /** URL to fetch version alerts from. */
    public static final HttpUrl VERSION_URL = HttpUrl.parse("https://wallet.schildbach.de/version");
    /** URL to fetch dynamic fees from. */
    public static final HttpUrl DYNAMIC_FEES_URL = HttpUrl.parse("https://wallet.schildbach.de/fees");

    /** MIME type used for transmitting single transactions. */
    public static final String MIMETYPE_TRANSACTION = "application/x-alctx";

    /** MIME type used for transmitting wallet backups. */
    public static final String MIMETYPE_WALLET_BACKUP = "application/x-alchain-wallet-backup";

    /** Number of confirmations until a transaction is fully confirmed. */
    public static final int MAX_NUM_CONFIRMATIONS = 7;

    /** User-agent to use for network access. */
    public static final String USER_AGENT = "ALChain Wallet";

    /** Default currency to use if all default mechanisms fail. */
    public static final String DEFAULT_EXCHANGE_CURRENCY = "USD";

    /** Donation address for tip/donate action. */
    public static final String DONATION_ADDRESS = NETWORK_PARAMETERS.getId().equals(NetworkParameters.ID_MAINNET)
            ? "" : null;

    /** Recipient e-mail address for reports. */
    public static final String REPORT_EMAIL = "ALChain.developers@gmail.com";

    /** Subject line for manually reported issues. */
    public static final String REPORT_SUBJECT_ISSUE = "Reported issue";

    /** Subject line for crash reports. */
    public static final String REPORT_SUBJECT_CRASH = "Crash report";

    public static final char CHAR_HAIR_SPACE = '\u200a';
    public static final char CHAR_THIN_SPACE = '\u2009';
    public static final char CHAR_ALMOST_EQUAL_TO = '\u2248';
    public static final char CHAR_CHECKMARK = '\u2713';
    public static final char CURRENCY_PLUS_SIGN = '\uff0b';
    public static final char CURRENCY_MINUS_SIGN = '\uff0d';
    public static final String PREFIX_ALMOST_EQUAL_TO = Character.toString(CHAR_ALMOST_EQUAL_TO) + CHAR_THIN_SPACE;
    public static final int ADDRESS_FORMAT_GROUP_SIZE = 4;
    public static final int ADDRESS_FORMAT_LINE_SIZE = 12;

    public static final MonetaryFormat LOCAL_FORMAT = new MonetaryFormat().noCode().minDecimals(2).optionalDecimals();

    public static final BaseEncoding HEX = BaseEncoding.base16().lowerCase();

    public static final String SOURCE_URL = "https://github.com/ALChain/ALChain";
    public static final String BINARY_URL = "https://github.com/ALChain/ALChain/releases";
    public static final String MARKET_APP_URL = "market://details?id=%s";
    public static final String WEBMARKET_APP_URL = "https://play.google.com/store/apps/details?id=%s";

    public static final int PEER_DISCOVERY_TIMEOUT_MS = 10 * (int) DateUtils.SECOND_IN_MILLIS;
    public static final int PEER_TIMEOUT_MS = 15 * (int) DateUtils.SECOND_IN_MILLIS;

    public static final long LAST_USAGE_THRESHOLD_JUST_MS = DateUtils.HOUR_IN_MILLIS;
    public static final long LAST_USAGE_THRESHOLD_RECENTLY_MS = 2 * DateUtils.DAY_IN_MILLIS;
    public static final long LAST_USAGE_THRESHOLD_INACTIVE_MS = 4 * DateUtils.WEEK_IN_MILLIS;

    public static final long DELAYED_TRANSACTION_THRESHOLD_MS = 2 * DateUtils.HOUR_IN_MILLIS;

    public static final int SDK_DEPRECATED_BELOW = Build.VERSION_CODES.JELLY_BEAN;

    public static final boolean BUG_OPENSSL_HEARTBLEED = Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN
            && Build.VERSION.RELEASE.startsWith("4.1.1");

    public static final int MEMORY_CLASS_LOWEND = 64;

    public static final int NOTIFICATION_ID_CONNECTED = 1;
    public static final int NOTIFICATION_ID_COINS_RECEIVED = 2;
    public static final int NOTIFICATION_ID_MAINTENANCE = 3;
    public static final int NOTIFICATION_ID_INACTIVITY = 4;
    public static final String NOTIFICATION_GROUP_KEY_RECEIVED = "group-received";
    public static final String NOTIFICATION_CHANNEL_ID_RECEIVED = "received";
    public static final String NOTIFICATION_CHANNEL_ID_ONGOING = "ongoing";
    public static final String NOTIFICATION_CHANNEL_ID_IMPORTANT = "important";

    /** Desired number of scrypt iterations for deriving the spending PIN */
    public static final int SCRYPT_ITERATIONS_TARGET = 65536;
    public static final int SCRYPT_ITERATIONS_TARGET_LOWRAM = 32768;

    /** Default ports for Electrum servers */
    public static final int ELECTRUM_SERVER_DEFAULT_PORT_TCP = NETWORK_PARAMETERS.getId()
            .equals(NetworkParameters.ID_MAINNET) ? 50001 : 51001;
    public static final int ELECTRUM_SERVER_DEFAULT_PORT_TLS = NETWORK_PARAMETERS.getId()
            .equals(NetworkParameters.ID_MAINNET) ? 50002 : 51002;

    /** Shared HTTP client, can reuse connections */
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    static {
        HTTP_CLIENT.setFollowRedirects(false);
        HTTP_CLIENT.setFollowSslRedirects(true);
        HTTP_CLIENT.setConnectTimeout(15, TimeUnit.SECONDS);
        HTTP_CLIENT.setWriteTimeout(15, TimeUnit.SECONDS);
        HTTP_CLIENT.setReadTimeout(15, TimeUnit.SECONDS);

        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(final String message) {
                        log.debug(message);
                    }
                });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        HTTP_CLIENT.interceptors().add(loggingInterceptor);
    }




    public static final int confirms = 1;

    public static final int eachFeedAmount = 10000;

    public static final String DESTROY_ADDRESS = "AbewbnvCn9M9Drz1Z4i9Vfr4nATDf7Wsr3";

    public static final int  DESTROY_AMOUNT = 3300000;

    public static final String BOSS_ADDRESS = "AZrBpp4UymXF5dEa7u2kPbnEksnSXoioLi";

    public static final int planetconfirms = 0;

//    public static final int PlanetUpdteAmount = 1;
//    public static final int PlanetNormalAmount = 2;
//    public static final int PlanetSupperAmount = 3;
//    public static final int PlanetTopAmount = 5;
//    public static final int 	updateLvl0 = 1;//35;
//    public static final int 	updateLvl1 = 2;//70;

    public static final int PlanetUpdteAmount =  20000;
    public static final int PlanetNormalAmount = 1000000;
    public static final int PlanetSupperAmount = 10000000;
    public static final int PlanetTopAmount =    100000000;
    public static final int 	updateLvl0 = 1;//35;
    public static final int 	updateLvl1 = 2;//70;


    public static final double PlanetNormalValue = 0.1;
    public static final double PlanetSupperValue = 0.5;
    public static final double PlanetTopValue = 1;
    public static final double IncomeCoinsPerYear = (0.000001);




    public static final  int onedaytimevalsec = (24*3600);


    public static final String PLANET_ADDRESS = "AP6ujp2pxsefXhczhKgyQVtgxYjfyjgZUz";


    public static final ArrayList<String> FEED_ADDRESSES = new ArrayList<String>() ;
    static {
        FEED_ADDRESSES.add("AafiiGE9mtE7wT6N8oVTvNSnDJAJS3dMqq");
        FEED_ADDRESSES.add("AND2ri13bpY2g1m8XkrwHpCB977D4TqRVw");
        FEED_ADDRESSES.add("ANg4ww3464Si6QVCd2LzRAYvu69ZCzz7Wz");
        FEED_ADDRESSES.add("Ac4UpCXkEYTRVUN4t2fD3UYh8TBz3w2Dsj");
        FEED_ADDRESSES.add("AZFGo6CbhCSXPWyHfnbBMTaG4wrT5Q9tPx");
        FEED_ADDRESSES.add("AXm5xYSeKbX12JnBT2QpMkkd11yQz7wBXt");
        FEED_ADDRESSES.add("AM43rGjDZ6fP7ZJvZcFvtGVnaB52MTKXbz");
        FEED_ADDRESSES.add("AanXYUBw3dvSd4EX2L3TVro56A1bFmG4xZ");
        FEED_ADDRESSES.add("AQq5z6J1N4hoc9KCKBC6b9keEu8cFTXrbB");
        FEED_ADDRESSES.add("ATKhArGh8AxCrMDzDkCCzU2PpYpGzsMegY");
        FEED_ADDRESSES.add("AKRJLsSzj3Gz9CAXpt7pHyEh2YvGdxViQX");
        FEED_ADDRESSES.add("AKuiytqn3VhAbYAzNbWyNxbRXTLSBB7mNT");
        FEED_ADDRESSES.add("AMoGCKopSiwQDoXEFCqRfouVrn2KJjBXSr");
        FEED_ADDRESSES.add("AbiRwBgW71KeWaHnPXRUL3vRDK81X1uDyv");
        FEED_ADDRESSES.add("AexmHuQcnGn7PdY2xWxsfTApQAvoGWWjmg");
        FEED_ADDRESSES.add("ANtXUQdDcA6oP24jpu3fMtsaCDDzDqrKyz");
        FEED_ADDRESSES.add("ALuk7N4CQp8gAwjBDjCu531MYV591E58Cv");
        FEED_ADDRESSES.add("AamiujKCndhrwzpZ9MZZmn6YdDDWXCm6bz");
        FEED_ADDRESSES.add("AQchsrt2m7rwFjvjdsr6Fko6bhSfaikNTv");
        FEED_ADDRESSES.add("AHiw2dXMABB3YWkp5jnPU67s2iksjpxs2W");
        FEED_ADDRESSES.add("AX7Dzuym7yJNrosntcCmj9NRV7i5cpURAH");
        FEED_ADDRESSES.add("AdmZ7mCLVCgaFzga2iCbzfqse6RakoBzbj");
        FEED_ADDRESSES.add("Ae2VLGHcjeHHtc28j1kgAu9VaHuxVTGqT8");
    }



    public static Map<String,int[][]> PET_PROPETY_NUMBER = new HashMap<>();

    static {
        PET_PROPETY_NUMBER.put("liliang", new int[][]{{99, 0}, {-1, 2000}, {0, 1500}, {1, 1000}, {2, 500}, {3, 100}} );
        PET_PROPETY_NUMBER.put("minjie"   , new int[][]{{99, 0}, {-1, 2000}, {0, 1500}, {1, 1000}, {2, 500}, {3, 100}} );
        PET_PROPETY_NUMBER.put("zhili"    , new int[][]{{99, 0}, {-1, 2000}, {0, 1500}, {1, 1000}, {2, 500}, {3, 100}} );
        PET_PROPETY_NUMBER.put("tongshuai", new int[][]{{99, 0}, {-1, 200}, {0, 150}, {1, 100}, {2, 100}, {3, 100}} );
        PET_PROPETY_NUMBER.put("gedang"   , new int[][]{{99, 0}, {-1, 20}, {0, 15}, {1, 10}, {2, 10}, {3, 10}} );
        PET_PROPETY_NUMBER.put("baoji"    , new int[][]{{99, 0}, {-1, 20}, {0, 15}, {1, 10}, {2, 10}, {3, 10}} );
        PET_PROPETY_NUMBER.put("yidong"   , new int[][]{{99, 0}, {-1, 5}, {0, 3}, {1, 2}, {2, 2}, {3, 2}} );
        PET_PROPETY_NUMBER.put("tiaoju"   , new int[][]{{99, 0}, {-1, 5}, {0, 3}, {1, 2}, {2, 2}, {3, 2}} );
        PET_PROPETY_NUMBER.put("gongju"   , new int[][]{{99, 0}, {-1, 5}, {0, 3}, {1, 2}, {2, 2}, {3, 2}} );
        PET_PROPETY_NUMBER.put("shunfa"   , new int[][]{{99, 0}, {-1, 5}, {0, 3}, {1, 2}, {2, 2}, {3, 2}} );
        PET_PROPETY_NUMBER.put("keji"     , new int[][]{{99, 0}, {-1, 10}, {0, 8}, {1, 5}, {2, 5}, {3, 5}} );
        PET_PROPETY_NUMBER.put("chaonengli", new int[][]{{99, 0}, {-1, 10}, {0, 8}, {1, 5}, {2, 5}, {3, 5}} );
        PET_PROPETY_NUMBER.put("tuanzhan" , new int[][]{{99, 0}, {-1, 15}, {0, 10}, {1, 5}, {2, 5}, {3, 5}} );
        PET_PROPETY_NUMBER.put("juejin"   , new int[][]{{99, 0}, {-1, 20}, {0, 15}, {1, 10}, {2, 5}, {3, 1}} );
    }


    public static Map<String,Integer>  petPropetyResult = new HashMap<>();

    static {
        petPropetyResult.put("liliang"	, 0);
        petPropetyResult.put("minjie"	, 0);
        petPropetyResult.put("zhili"	, 0);
        petPropetyResult.put("tongshuai", 0);
        petPropetyResult.put("gedang"	, 0);
        petPropetyResult.put("baoji"	, 0);
        petPropetyResult.put("yidong"	, 0);
        petPropetyResult.put("tiaoju"	, 0);
        petPropetyResult.put("gongju"	, 0);
        petPropetyResult.put("shunfa"	, 0);
        petPropetyResult.put("keji" 	, 0);
        petPropetyResult.put("chaonengli", 0);
        petPropetyResult.put("tuanzhan" , 0);
        petPropetyResult.put("juejin"	, 0);
    };


    private static final Logger log = LoggerFactory.getLogger(Constants.class);
}
