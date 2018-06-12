package kk.techbytecare.androiddrinkserver.Retrofit;

import java.util.List;

import io.reactivex.Observable;
import kk.techbytecare.androiddrinkserver.Model.Category;
import retrofit2.http.GET;

public interface IDrinkShopAPI {

    @GET("getmenu.php")
    Observable<List<Category>> getMenu();
}
