package kk.techbytecare.androiddrinkserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kk.techbytecare.androiddrinkserver.Adapter.DrinkListAdapter;
import kk.techbytecare.androiddrinkserver.Common.Common;
import kk.techbytecare.androiddrinkserver.Model.Drink;
import kk.techbytecare.androiddrinkserver.Retrofit.IDrinkShopAPI;

public class DrinkListActivity extends AppCompatActivity {

    IDrinkShopAPI mService;
    RecyclerView recyclerView;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);

        mService = Common.getAPI();

        recyclerView = findViewById(R.id.recycler_drinks);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setHasFixedSize(true);

        loadListDrink(Common.currentCategory.getID());
    }

    private void loadListDrink(String id) {
        compositeDisposable.add(mService.getDrink(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Drink>>() {
                    @Override
                    public void accept(List<Drink> drinks) throws Exception {
                        displayDrinkList(drinks);
                    }
                }));
    }

    private void displayDrinkList(List<Drink> drinks) {
        DrinkListAdapter adapter = new DrinkListAdapter(this,drinks);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        loadListDrink(Common.currentCategory.getID());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
