package com.yarolegovich.discretescrollview.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yarolegovich.discretescrollview.sample.databinding.ActivityMainBinding;
import com.yarolegovich.discretescrollview.sample.gallery.GalleryActivity;
import com.yarolegovich.discretescrollview.sample.shop.ShopActivity;
import com.yarolegovich.discretescrollview.sample.weather.WeatherActivity;


public class MainActivity extends AppCompatActivity {

    private static final Uri URL_TAYA_BEHANCE = Uri.parse("https://www.behance.net/yurkivt");
    private static final Uri URL_SHOP_PHOTOS = Uri.parse("https://herriottgrace.com/collections/all");
    private static final Uri URL_CITY_ICONS = Uri.parse("https://www.flaticon.com");
    private static final Uri URL_APP_REPO = Uri.parse("https://github.com/yarolegovich/DiscreteScrollView");

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        setSupportActionBar(binding.atoolbartoolbar.toolbar);

        binding.previewShop.setOnClickListener(v -> start(ShopActivity.class));
        binding.previewWeather.setOnClickListener(v -> start(WeatherActivity.class));
        binding.previewVertical.setOnClickListener(v -> start(GalleryActivity.class));

        binding.creditCityIcons.setOnClickListener(v -> open(URL_CITY_ICONS));
        binding.creditShopPhotos.setOnClickListener(v -> open(URL_SHOP_PHOTOS));
        binding.creditTaya.setOnClickListener(v -> open(URL_TAYA_BEHANCE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mi_github) {
            open(URL_APP_REPO);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void open(Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(url);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(binding.getRoot(),
                    R.string.msg_no_browser,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void start(Class<? extends Activity> token) {
        Intent intent = new Intent(this, token);
        startActivity(intent);
    }
}
