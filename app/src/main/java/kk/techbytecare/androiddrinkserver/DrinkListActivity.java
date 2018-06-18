package kk.techbytecare.androiddrinkserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kk.techbytecare.androiddrinkserver.Adapter.DrinkListAdapter;
import kk.techbytecare.androiddrinkserver.Common.Common;
import kk.techbytecare.androiddrinkserver.Model.Drink;
import kk.techbytecare.androiddrinkserver.Retrofit.IDrinkShopAPI;
import kk.techbytecare.androiddrinkserver.Utils.ProgressRequestBody;
import kk.techbytecare.androiddrinkserver.Utils.UploadCallbacks;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrinkListActivity extends AppCompatActivity implements UploadCallbacks {

    private static final int PICK_FILE_REQUEST = 5152;
    IDrinkShopAPI mService;
    RecyclerView recyclerView;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    FloatingActionButton btn_add;

    ImageView img_browse;
    EditText edt_drink_name,edt_drink_price;

    Uri selected_uri = null;
    String uploaded_file_path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);

        mService = Common.getAPI();

        recyclerView = findViewById(R.id.recycler_drinks);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setHasFixedSize(true);

        btn_add = findViewById(R.id.fab);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProductDialog();
            }
        });

        loadListDrink(Common.currentCategory.getID());
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        View view = LayoutInflater.from(this)
                .inflate(R.layout.add_menu_product_layout,null);

        edt_drink_name = view.findViewById(R.id.edt_drink_name);
        edt_drink_price = view.findViewById(R.id.edt_drink_price);

        img_browse = view.findViewById(R.id.img_browse);

        img_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),"Select a file"),
                        PICK_FILE_REQUEST);
            }
        });
        builder.setView(view);

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                uploaded_file_path = "";
                selected_uri = null;
            }
        }).setPositiveButton("ADD CATEGORY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (edt_drink_name.getText().toString().isEmpty())    {
                    Toast.makeText(DrinkListActivity.this, "Plz enter the name of product...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (edt_drink_price.getText().toString().isEmpty())    {
                    Toast.makeText(DrinkListActivity.this, "Plz enter the price of product...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (uploaded_file_path.isEmpty())   {
                    Toast.makeText(DrinkListActivity.this, "Plz select image", Toast.LENGTH_SHORT).show();
                    return;
                }

                compositeDisposable.add(mService.addNewProduct(
                        edt_drink_name.getText().toString(),
                        uploaded_file_path,
                        edt_drink_price.getText().toString(),
                        Common.currentCategory.ID
                ).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Toast.makeText(DrinkListActivity.this, ""+s, Toast.LENGTH_SHORT).show();
                                loadListDrink(Common.currentCategory.getID());
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(DrinkListActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }));

            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)  {

            if (requestCode == PICK_FILE_REQUEST)   {

                if (data != null)   {

                    selected_uri = data.getData();

                    if (selected_uri != null && !selected_uri.getPath().isEmpty())  {

                        img_browse.setImageURI(selected_uri);
                        uploadFileToServer();
                    }
                    else    {
                        Toast.makeText(this, "Can't upload file to server...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void uploadFileToServer() {

        if (selected_uri != null)   {

            File file = FileUtils.getFile(this,selected_uri);

            String fileName = new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString()))
                    .toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file,this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file",fileName,requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    mService.uploadProductFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {

                                    uploaded_file_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/product/product_img/")
                                            .append(response.body().toString())
                                            .toString();

                                    Log.d("IMG_PATH", uploaded_file_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(DrinkListActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }).start();
        }

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

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
