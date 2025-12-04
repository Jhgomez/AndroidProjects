package com.yarolegovich.discretescrollview.sample.shop;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.sample.DiscreteScrollViewOptions;
import com.yarolegovich.discretescrollview.sample.R;
import com.yarolegovich.discretescrollview.sample.databinding.ActivityShopBinding;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

/**
 * Created by yarolegovich on 07.03.2017.
 */

public class ShopActivity extends AppCompatActivity implements DiscreteScrollView.OnItemChangedListener<ShopAdapter.ViewHolder> {

    private List<Item> data;
    private Shop shop;
    private InfiniteScrollAdapter<?> infiniteAdapter;

    private ActivityShopBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shop = Shop.get();
        data = shop.getData();

        binding.itemPicker.setOrientation(DSVOrientation.HORIZONTAL);
        binding.itemPicker.addOnItemChangedListener(this);
        infiniteAdapter = InfiniteScrollAdapter.wrap(new ShopAdapter(data));
        binding.itemPicker.setAdapter(infiniteAdapter);
        binding.itemPicker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        binding.itemPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());

        onItemChanged(data.get(0));

        binding.itemBtnRate.setOnClickListener(view -> {
            int realPosition = infiniteAdapter.getRealPosition(binding.itemPicker.getCurrentItem());
            Item current = data.get(realPosition);
            shop.setRated(current.getId(), !shop.isRated(current.getId()));
            changeRateButtonState(current);
        });
        binding.itemBtnBuy.setOnClickListener(v -> showUnsupportedSnackBar());
        binding.itemBtnComment.setOnClickListener(v -> showUnsupportedSnackBar());

        binding.home.setOnClickListener(v -> finish());
        binding.btnSmoothScroll.setOnClickListener(v -> DiscreteScrollViewOptions.smoothScrollToUserSelectedPosition(binding.itemPicker, v));
        binding.btnTransitionTime.setOnClickListener(v -> DiscreteScrollViewOptions.configureTransitionTime(binding.itemPicker));
    }

    private void onItemChanged(Item item) {
        binding.itemName.setText(item.getName());
        binding.itemPrice.setText(item.getPrice());
        changeRateButtonState(item);
    }

    private void changeRateButtonState(Item item) {
        if (shop.isRated(item.getId())) {
            binding.itemBtnRate.setImageResource(R.drawable.ic_star_black_24dp);
            binding.itemBtnRate.setColorFilter(ContextCompat.getColor(this, R.color.shopRatedStar));
        } else {
            binding.itemBtnRate.setImageResource(R.drawable.ic_star_border_black_24dp);
            binding.itemBtnRate.setColorFilter(ContextCompat.getColor(this, R.color.shopSecondary));
        }
    }

    @Override
    public void onCurrentItemChanged(@Nullable ShopAdapter.ViewHolder viewHolder, int adapterPosition) {
        int positionInDataSet = infiniteAdapter.getRealPosition(adapterPosition);
        onItemChanged(data.get(positionInDataSet));
    }

    private void showUnsupportedSnackBar() {
        Snackbar.make(binding.itemPicker, R.string.msg_unsupported_op, Snackbar.LENGTH_SHORT).show();
    }
}
