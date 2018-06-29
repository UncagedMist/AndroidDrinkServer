package kk.techbytecare.androiddrinkserver;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kk.techbytecare.androiddrinkserver.Common.Common;
import kk.techbytecare.androiddrinkserver.Model.Category;
import kk.techbytecare.androiddrinkserver.Retrofit.IDrinkShopAPI;
import kk.techbytecare.androiddrinkserver.Utils.ProgressRequestBody;
import kk.techbytecare.androiddrinkserver.Utils.UploadCallbacks;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProductActivity extends AppCompatActivity implements UploadCallbacks {

    MaterialSpinner spinner_product;

    private int PICK_FILE_REQUEST = 5152;

    ImageView img_browse;
    EditText edt_product_name,edt_product_price;

    IDrinkShopAPI mService;

    CompositeDisposable compositeDisposable;

    Uri selectedUri = null;
    String uploaded_img_path = "", selected_category = "";

    Button btn_delete,btn_update;

    HashMap<String, String> menu_data_for_get_key = new HashMap<>();
    HashMap<String, String> menu_data_for_get_value = new HashMap<>();

    List<String> menu_data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        if (Common.currentDrink != null)    {

            uploaded_img_path = Common.currentDrink.Link;
            selected_category = Common.currentDrink.MenuId;
        }

        mService = Common.getAPI();

        compositeDisposable = new CompositeDisposable();

        edt_product_name = findViewById(R.id.edt_drink_name);
        edt_product_price = findViewById(R.id.edt_drink_price);

        img_browse = findViewById(R.id.img_browse);

        btn_delete = findViewById(R.id.btn_delete);
        btn_update = findViewById(R.id.btn_update);

        spinner_product = findViewById(R.id.spinner_menu);

        setSpinnerMenu();

        img_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),"Select a file"),
                        PICK_FILE_REQUEST);
            }
        });

        spinner_product.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selected_category = menu_data_for_get_key.get(menu_data.get(position));
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProduct();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct();
            }
        });

        setProductInfo();
    }

    private void setProductInfo() {
        if (Common.currentDrink != null)    {
            edt_product_name.setText(Common.currentDrink.Name);
            edt_product_price.setText(Common.currentDrink.Price);

            Picasso.with(this)
                    .load(Common.currentDrink.Link)
                    .into(img_browse);

            spinner_product.setSelectedIndex(menu_data.indexOf(menu_data_for_get_value.get(Common.currentCategory.getID())));
        }
    }

    private void deleteProduct() {
        compositeDisposable.add(mService.deleteProduct(
                Common.currentDrink.ID
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, "" + s, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }));
    }

    private void updateProduct() {
        compositeDisposable.add(mService.updateProduct(
                Common.currentDrink.ID,
                edt_product_name.getText().toString(),
                uploaded_img_path,
                edt_product_price.getText().toString(),
                selected_category
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, "" + s, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        Toast.makeText(UpdateProductActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)  {

            if (requestCode == PICK_FILE_REQUEST)   {

                if (data != null)   {

                    selectedUri = data.getData();

                    if (selectedUri != null && !selectedUri.getPath().isEmpty())  {

                        img_browse.setImageURI(selectedUri);
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
        if (selectedUri != null)   {

            File file = FileUtils.getFile(this,selectedUri);

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

                                    uploaded_img_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/category/category_img/")
                                            .append(response.body().toString())
                                            .toString();

                                    Log.d("IMG_PATH", uploaded_img_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(UpdateProductActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }).start();
        }
    }

    private void setSpinnerMenu() {
        for (Category category : Common.menuList)   {

            menu_data_for_get_key.put(category.getName(),category.getID());
            menu_data_for_get_value.put(category.getID(),category.getName());

            menu_data.add(category.getName());
        }
        spinner_product.setItems(menu_data);
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
