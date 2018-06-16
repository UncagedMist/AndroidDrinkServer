package kk.techbytecare.androiddrinkserver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import kk.techbytecare.androiddrinkserver.Adapter.MenuAdapter;
import kk.techbytecare.androiddrinkserver.Common.Common;
import kk.techbytecare.androiddrinkserver.Model.Category;
import kk.techbytecare.androiddrinkserver.Retrofit.IDrinkShopAPI;
import kk.techbytecare.androiddrinkserver.Utils.ProgressRequestBody;
import kk.techbytecare.androiddrinkserver.Utils.UploadCallbacks;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UploadCallbacks {

    private static final int REQUEST_PERMISSION_CODE = 1000;
    private static final int PICK_FILE_REQUEST = 5152;
    RecyclerView recycler_menu;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    IDrinkShopAPI mService;

    EditText edt_name;
    ImageView img_browse;

    Uri selected_uri = null;
    String uploaded_file_path = "";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)    {

            case REQUEST_PERMISSION_CODE:   {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)    {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                }
                else    {
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getAPI();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)   {
            ActivityCompat.requestPermissions(this,new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },REQUEST_PERMISSION_CODE);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCategoryDialog();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recycler_menu = findViewById(R.id.lst_menu);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        recycler_menu.setHasFixedSize(true);

        getMenuItems();
    }

    private void showAddCategoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        View view = LayoutInflater.from(this)
                .inflate(R.layout.add_category_layout,null);

        edt_name = view.findViewById(R.id.edt_name);
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

                if (edt_name.getText().toString().isEmpty())    {
                    Toast.makeText(HomeActivity.this, "Plz enter the name of category...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (uploaded_file_path.isEmpty())   {
                    Toast.makeText(HomeActivity.this, "Plz select image", Toast.LENGTH_SHORT).show();
                    return;
                }

                compositeDisposable.add(mService.addNewCategory(
                        edt_name.getText().toString(),uploaded_file_path)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Toast.makeText(HomeActivity.this, s, Toast.LENGTH_SHORT).show();

                                getMenuItems();

                                uploaded_file_path = "";
                                selected_uri = null;
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

                    mService.uploadCategoryFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {

                                    uploaded_file_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/category/category_img/")
                                            .append(response.body().toString())
                                            .toString();

                                    Log.d("IMG_PATH", uploaded_file_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(HomeActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }).start();
        }

    }

    private void getMenuItems() {
        compositeDisposable.add(mService.getMenu()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<List<Category>>() {
                            @Override
                            public void accept(List<Category> categories) throws Exception {
                                displayMenuList(categories);
                            }
                        }));
    }

    private void displayMenuList(List<Category> categories) {
        MenuAdapter adapter = new MenuAdapter(this,categories);
        recycler_menu.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMenuItems();
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the Home action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
