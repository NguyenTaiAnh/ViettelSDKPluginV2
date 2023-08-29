package cordova.nta.viettel.sdk.plugin;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ivb.ivbone.MainActivity;
import com.viettel.biometrics.signature.helpers.GoSignSDK;
import com.viettel.biometrics.signature.helpers.GoSignSDKSetup;
import com.viettel.biometrics.signature.helpers.MySignSDK;
import com.viettel.biometrics.signature.listener.ServiceApiListener;
import com.viettel.biometrics.signature.listener.ServiceApiListenerEmpty;
import com.viettel.biometrics.signature.network.request.PendingAuthorisationRequest;

import com.viettel.biometrics.signature.network.response.CertificateResponse;
import com.viettel.biometrics.signature.network.response.ResponseError;
import com.viettel.biometrics.signature.ultils.BiometricApiType;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class ViettelSDKPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.d("actopn kiemtra: ", action);
        System.out.println("check: " + args);
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("getDeviceId")) {
            this.getDeviceId(callbackContext);
            return true;
        } else if (action.equals("registerDevice")) {
            String token = args.getString(0);
            String user_id = args.getString(1);
            Log.d("test kiemtra:", args.getString(0));
            cordova.getThreadPool().execute(() -> registerDevice(token, user_id, callbackContext));
            return true;
        } else if (action.equals("authorisationPendingRequest")) {
//            List<PendingAuthorisationRequest> request = (List<PendingAuthorisationRequest>) args.getJSONArray(0);
            String request = args.getString(0);
            String access_token = args.getString(1);
            String transactionID = args.getString(2);
            String user_id = args.getString(3);
            Log.d("test kiemtra request:", args.getString(0));
            Log.d("test kiemtra access_token: ", args.getString(1));
            Log.d("test kiemtra: transactionID", args.getString(2));
            cordova.getThreadPool().execute(() -> authorisationPendingRequest(request, access_token, transactionID, user_id, callbackContext));
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void getDeviceId(CallbackContext callbackContext) {
        System.out.println("====================================GETDEVICEID=================================");
        try {
            GoSignSDKSetup.initialize(cordova.getActivity().getApplication(), "https://remotesigning.viettel.vn:8773", null);
            String deviceId = GoSignSDK.get().getDeviceId();
            System.out.println("test: kiemtra device " + deviceId);
            callbackContext.success(deviceId);
        } catch (Exception e) {
            System.out.println("test: kiemtra loi " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void registerDevice(String token, String userId, CallbackContext callbackContext) {
        if (token != null && token.length() > 0) {
            try {
                cordova.getActivity().runOnUiThread(() -> {
                    GoSignSDKSetup.initialize(cordova.getActivity().getApplication(), "https://remotesigning.viettel.vn:8773", null);

                    new MySignSDK.Builder()
                            .withUserId(userId)
                            .withToken(token)
                            .withActivity((MainActivity) cordova.getContext()).withBiometricApiType(BiometricApiType.AUTO).registerDevice(new ServiceApiListener<CertificateResponse>() {
                                @Override
                                public void onSuccess(CertificateResponse data) {
                                    Log.d("registerDevice", "Success");
                                    cordova.getActivity().runOnUiThread(() -> callbackContext.success("success"));
                                }

                                @Override
                                public void onFail(ResponseError error) {
                                    Log.d("registerDevice", "onFail");
                                    Log.d("Error Message:", error.getErrorMessage());
                                    cordova.getActivity().runOnUiThread(() -> callbackContext.error(error.getErrorMessage()));
                                }

                                @Override
                                public void showLoading() {
                                }

                                @Override
                                public void hideLoading() {
                                }
                            }).build();
                });
            } catch (Exception e) {
                Log.d("Kiemtra bug: tétttt ", e.getMessage());
                callbackContext.error("Register device failed: " + e.getMessage());
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void authorisationPendingRequest(String request, String access_token, String transactionID, String user_id, CallbackContext callbackContext) {
        Log.d("kiemtra access_token:", access_token);
        Log.d("kiemtra transactionID:", transactionID);
        System.out.println("kiemtra type request: " + request);
        System.out.println("kiemtra check true false:" + request != null && request.length() > 0 && access_token != null && access_token.length() > 0 && transactionID != null && transactionID.length() > 0);

        if (request != null && request.length() > 0 && access_token != null && access_token.length() > 0 && transactionID != null && transactionID.length() > 0) {
            try {
                cordova.getActivity().runOnUiThread(() -> {
                    new MySignSDK.Builder()
                            .withUserId(user_id)
                            .withToken(access_token)
                            .withActivity((MainActivity) cordova.getContext())
                            .withBiometricApiType(BiometricApiType.AUTO).authorisationPendingRequest(
                                    new PendingAuthorisationRequest(transactionID, request, "SHA256"), new ServiceApiListenerEmpty() {
                                        @Override
                                        public void onSuccess() {
                                            callbackContext.success("success");
                                        }

                                        @Override
                                        public void onFail(ResponseError error) {
                                            System.out.println("authorisationPendingRequest: onFail");
                                            System.out.println("=====================================BEGIN==========================");
                                            System.out.println(error.getErrorMessage());
                                            callbackContext.error(error.getErrorMessage());
//                cordova.getActivity().runOnUiThread(() -> callbackContext.error("Register device failed: " + responseError.getErrorMessage()));
                                            System.out.println("=====================================END==========================");
                                        }

                                        @Override
                                        public void showLoading() {
                                        }

                                        @Override
                                        public void hideLoading() {
                                        }
                                    }).build();
                });
            } catch (Exception e) {
                Log.d("Kiemtra bug: ", e.getMessage());
                callbackContext.error("Register device failed: try catch " + e.getMessage());
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

}

