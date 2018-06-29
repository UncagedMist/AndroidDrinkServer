package kk.techbytecare.androiddrinkserver.Services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import kk.techbytecare.androiddrinkserver.Common.Common;
import kk.techbytecare.androiddrinkserver.Retrofit.IDrinkShopAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        updateTokenToServer(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateTokenToServer(String token) {
        IDrinkShopAPI mService = Common.getAPI();
        mService.updateToken("server_app_01",token,"1")
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.d("DEBUG", response.body());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("DEBUG", t.getMessage());
                    }
                });
    }
}
