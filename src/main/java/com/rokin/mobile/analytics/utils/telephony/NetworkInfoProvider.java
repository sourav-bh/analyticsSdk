package com.rokin.mobile.analytics.utils.telephony;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.Arrays;

import com.rokin.mobile.analytics.constant.AppConstant;
import com.rokin.mobile.analytics.utils.Logger;


/**
 * Created by Sourav.
 */
public class NetworkInfoProvider extends PhoneStateListener
{
    private static NetworkInfoProvider instance = null;
    private Context context;

    private CellInfo cellInfo = new CellInfo();
    private TelephonyManager telephonyManager = null;

    private NetworkInfoProvider(Context context)
    {
        this.context = context.getApplicationContext();

        telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SERVICE_STATE );
    }

    public static NetworkInfoProvider getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new NetworkInfoProvider(context);
        }
        return instance;
    }

    /**
     * Returns cellular network information, like as MCC, MNC, LAC, Signal Strength
     *
     * @return a CellInfo object
     */
    public CellInfo getCellInfo()
    {
        try
        {
            if(telephonyManager != null)
            {
                int simState = telephonyManager.getSimState();
                if (simState == TelephonyManager.SIM_STATE_READY)
                {
                    String networkOperator = telephonyManager.getNetworkOperator();
                    if (networkOperator != null && networkOperator.length() > 0)
                    {
                        try
                        {
                            cellInfo.setMCC(Integer.parseInt(networkOperator.substring(0, 3)));
                            cellInfo.setMNC(Integer.parseInt(networkOperator.substring(3)));
                        }
                        catch (NumberFormatException e)
                        {
                        }
                    }
                }
                else
                {
                    cellInfo.setMCC(0);
                    cellInfo.setMNC(0);
                }
            }
        }
        catch (Exception e)
        {
            Logger.consolePrintInfo(getClass().getSimpleName().toString(), e.toString());
        }
        return cellInfo;
    }

    public TelephonyManager getTelephonyManager()
    {
        return telephonyManager;
    }

    /**
     * Get the active network type, if there is an active mobile network then any other active network will be ignored.
     * <br>
     * The options for given network type is limited as following:
     * <br>
     * <br>
     * <b>Unknown</b> - doesn't match one of the strings below. For new types platform and a numeric value will be added, e.g. "Unknown-Android-12"
     * <br>
     * <b>No Network</b> - if device is not connected to GSM or NETWORK_TYPE_WIFI
     * <br>
     * <b>CDMA</b> - includes EVDO*, 1xRTT, EHRPD
     * <br>
     * <b>GPRS</b> - includes EDGE
     * <br>
     * <b>UMTS</b> - includes HSDPA, HSUPA, HSPA, HSPAP
     * <br>
     * <b>LTE</b>
     * <br>
     * <b>NETWORK_TYPE_WIFI</b> - Only if not connected to a GSM network
     *
     * @return is a human-readable String representation for the network type.
     */
    public String getNetWorkType()
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfoArray = cm.getAllNetworkInfo();
        ArrayList<NetworkInfo> networkInfos = new ArrayList<NetworkInfo>();//(Arrays.asList(networkInfoArray));
        networkInfos.addAll(Arrays.asList(networkInfoArray));

        ArrayList<String> networkInfoNames = new ArrayList<String>();

        for(NetworkInfo info:networkInfos)
        {
            if(info.getTypeName().startsWith(AppConstant.NETWORK_TYPE_MOBILE.toLowerCase()) && info.isAvailable())
                networkInfoNames.add(AppConstant.NETWORK_TYPE_MOBILE.toLowerCase());
            else if(info.isAvailable())
            {
                networkInfoNames.add(info.getTypeName());
            }
        }

        String networkType;
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        String activeNetworkTypeName = "";

        if (activeNetworkInfo != null) {
            activeNetworkTypeName = activeNetworkInfo.getTypeName();
        }

        if (activeNetworkTypeName != null
            && (activeNetworkTypeName.contains(AppConstant.NETWORK_TYPE_MOBILE) || activeNetworkTypeName.contains(AppConstant.NETWORK_TYPE_MOBILE.toLowerCase()))
            && activeNetworkInfo.isConnected())
        {
            int networkTypeValue = 0;
            if (telephonyManager != null)
            {
                networkTypeValue = telephonyManager.getNetworkType();
            }

            switch (networkTypeValue)
            {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    networkType = AppConstant.NETWORK_TYPE_GPRS;
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    networkType = AppConstant.NETWORK_TYPE_GPRS;
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    networkType = AppConstant.NETWORK_TYPE_UMTS;
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    networkType = AppConstant.NETWORK_TYPE_UMTS;
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    networkType = AppConstant.NETWORK_TYPE_UMTS;
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    networkType = AppConstant.NETWORK_TYPE_UMTS;
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    networkType = AppConstant.NETWORK_TYPE_UMTS;
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    networkType = AppConstant.NETWORK_TYPE_CDMA;
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    networkType = AppConstant.NETWORK_TYPE_CDMA;
                    break;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    networkType = AppConstant.NETWORK_TYPE_CDMA;
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    networkType = AppConstant.NETWORK_TYPE_CDMA;
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    networkType = AppConstant.NETWORK_TYPE_CDMA;
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    networkType = AppConstant.NETWORK_TYPE_CDMA;
                    break;
                default:
                    networkType = (networkTypeValue == 0 ? AppConstant.NETWORK_TYPE_UNKNOWN : AppConstant.NETWORK_TYPE_UNKNOWN_ANDROID + networkTypeValue);
                    break;
            }
        }
        else if (activeNetworkTypeName != null
                && (activeNetworkTypeName.contains(AppConstant.NETWORK_TYPE_WIFI) || activeNetworkTypeName.contains(AppConstant.NETWORK_TYPE_WIFI.toLowerCase()))
                && activeNetworkInfo.isConnected())
        {
            networkType = AppConstant.NETWORK_TYPE_WIFI;
        }
        else if(networkInfoNames != null && networkInfoNames.size() <= 0)
        {
            networkType = AppConstant.NETWORK_TYPE_NO_NETWORK;
        }
        else
        {
            networkType = AppConstant.NETWORK_TYPE_UNKNOWN;
        }

        return networkType;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
        super.onSignalStrengthsChanged(signalStrength);
        int signalStrengthValue;
        if(signalStrength.isGsm())
        {
            if (signalStrength.getGsmSignalStrength() != 99)
                signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
            else
                signalStrengthValue = signalStrength.getGsmSignalStrength();

        }
        else
        {
            signalStrengthValue = signalStrength.getCdmaDbm();
        }
        cellInfo.setSignalStrength(signalStrengthValue);
    }

    @Override
    public void onCellLocationChanged(CellLocation location)
    {
        try
        {
            super.onCellLocationChanged(location);
            GsmCellLocation gsmLocation = (GsmCellLocation)location;
            if (gsmLocation != null)
            {
                cellInfo.setCellID(gsmLocation.getCid());
                cellInfo.setLAC(gsmLocation.getLac());
            }
        }
        catch (Exception e)
        {
            Logger.consolePrintInfo("ErrorLog", e.toString());
        }
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState)
    {
        super.onServiceStateChanged(serviceState);
    }

    public void destroy()
    {
        if(telephonyManager!=null)
        {
            telephonyManager.listen(this ,PhoneStateListener.LISTEN_NONE);
        }

        instance = null;
    }
}
