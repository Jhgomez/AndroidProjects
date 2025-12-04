package com.yarolegovich.discretescrollview.sample.weather;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.sample.R;
import com.yarolegovich.discretescrollview.sample.DiscreteScrollViewOptions;
import com.yarolegovich.discretescrollview.sample.databinding.ActivityWeatherBinding;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

/**
 * Created by yarolegovich on 08.03.2017.
 */

public class WeatherActivity extends AppCompatActivity implements
        DiscreteScrollView.ScrollStateChangeListener<ForecastAdapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<ForecastAdapter.ViewHolder> {

    private List<Forecast> forecasts;

    private ActivityWeatherBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeatherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        forecasts = WeatherStation.get().getForecasts();

        binding.forecastCityPicker.setSlideOnFling(true);
        binding.forecastCityPicker.setAdapter(new ForecastAdapter(forecasts));
        binding.forecastCityPicker.addOnItemChangedListener(this);
        binding.forecastCityPicker.addScrollStateChangeListener(this);
        binding.forecastCityPicker.scrollToPosition(2);
        binding.forecastCityPicker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        binding.forecastCityPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());

        binding.forecastView.setForecast(forecasts.get(0));

        binding.home.setOnClickListener(v -> finish());
        binding.btnTransitionTime.setOnClickListener(v -> DiscreteScrollViewOptions.configureTransitionTime(binding.forecastCityPicker));
        binding.btnSmoothScroll.setOnClickListener(v -> DiscreteScrollViewOptions.smoothScrollToUserSelectedPosition(binding.forecastCityPicker, v));
    }

    @Override
    public void onCurrentItemChanged(@Nullable ForecastAdapter.ViewHolder holder, int position) {
        //viewHolder will never be null, because we never remove items from adapter's list
        if (holder != null) {
            binding.forecastView.setForecast(forecasts.get(position));
            holder.showText();
        }
    }

    @Override
    public void onScrollStart(@NonNull ForecastAdapter.ViewHolder holder, int position) {
        holder.hideText();
    }

    @Override
    public void onScroll(
            float position,
            int currentIndex, int newIndex,
            @Nullable ForecastAdapter.ViewHolder currentHolder,
            @Nullable ForecastAdapter.ViewHolder newHolder) {
        Forecast current = forecasts.get(currentIndex);
        RecyclerView.Adapter<?> adapter = binding.forecastCityPicker.getAdapter();
        int itemCount = adapter != null ? adapter.getItemCount() : 0;
        if (newIndex >= 0 && newIndex < itemCount) {
            Forecast next = forecasts.get(newIndex);
            binding.forecastView.onScroll(1f - Math.abs(position), current, next);
        }
    }

    @Override
    public void onScrollEnd(@NonNull ForecastAdapter.ViewHolder holder, int position) {

    }
}
