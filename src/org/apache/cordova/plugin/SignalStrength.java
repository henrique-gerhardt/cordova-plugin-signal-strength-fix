package org.apache.cordova.plugin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import java.lang.Exception;
import java.lang.Thread;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class SignalStrength extends CordovaPlugin {

        private TelephonyManager tm;
        private int dbm = -1;
        public int asulevel = -1;

        protected final static String[] permissions = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION };
        public static final int CONTINUE = 1;
        public static final int PERMISSION_DENIED_ERROR = 20;
        private CallbackContext callbackContext;

        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

                if (action.equals("dbm")) {
                        this.callbackContext = callbackContext;
                        tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                        
                        if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                                PermissionHelper.requestPermission(this, CONTINUE, Manifest.permission.READ_PHONE_STATE);
                        } else {
                                this.dbm = getDbm();
                        }
                        callbackContext.success(this.dbm);
                        return true;
                }

                return false;
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
                for (int r : grantResults) {
                        if (r == PackageManager.PERMISSION_DENIED) {
                                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                                return;
                        }
                }
                switch (requestCode) {
                        case CONTINUE:
                                this.dbm = getDbm();
                                break;
                }
        }

        private int getDbm() {
                List<CellInfo> cellInfoList = this.tm.getAllCellInfo();
                if (cellInfoList != null) {
                        for (CellInfo cellInfo : cellInfoList) {
                                if (cellInfo instanceof CellInfoLte) {
                                        CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                                        return cellSignalStrengthLte.getDbm();
                                } else if (cellInfo instanceof CellInfoWcdma) {
                                        CellSignalStrengthWcdma cellSignalStrengthWcdma = ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                                        return cellSignalStrengthWcdma.getDbm();
                                } else if (cellInfo instanceof CellInfoGsm) {
                                        CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                                        return cellSignalStrengthGsm.getDbm();
                                } else if (cellInfo instanceof CellInfoCdma) {
                                        CellSignalStrengthCdma cellSignalStrengthCdma = ((CellInfoCdma) cellInfo).getCellSignalStrength();
                                        return cellSignalStrengthCdma.getDbm();
                                } else {
                                        return -1;
                                }
                        }
                } else {
                        try { 
                                SignalStrengthStateListener sssListener = new SignalStrengthStateListener();
                                this.tm.listen(sssListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                                int cc = 0;
                                while (this.asulevel == -1) {
                                        Thread.sleep(300);
                                        if (cc++ >= 5)
                                        {
                                                break;
                                        }
                                }
                                this.tm.listen(sssListener, PhoneStateListener.LISTEN_NONE);

                                if(this.asulevel == -1) {
                                        return -1;
                                }

                                return -113 + 2 * this.asulevel;
                        } catch (Exception e) {
                                throw new IllegalArgumentException(e);
                        }
                }

                return -1;
        }

        public class SignalStrengthStateListener extends PhoneStateListener {
                @Override
                public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
                        super.onSignalStrengthsChanged(signalStrength);
                        asulevel = signalStrength.getGsmSignalStrength();
                }
        }
}
