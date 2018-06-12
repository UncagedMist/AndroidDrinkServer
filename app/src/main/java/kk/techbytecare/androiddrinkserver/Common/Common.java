package kk.techbytecare.androiddrinkserver.Common;

import kk.techbytecare.androiddrinkserver.Retrofit.IDrinkShopAPI;
import kk.techbytecare.androiddrinkserver.Retrofit.RetrofitClient;

public class Common {

    public static final String BASE_URL = "http://10.0.2.2/drinkshop/";

    public static IDrinkShopAPI getAPI()    {

        return RetrofitClient.getInstance(BASE_URL).create(IDrinkShopAPI.class);
    }
}
