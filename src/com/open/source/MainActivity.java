package com.open.source;

import com.example.custom.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class MainActivity extends Activity {

    public static class WaveDrawableFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView;
            if (true) {
                rootView = inflater.inflate(R.layout.fragment_wave_drawable, container, false);
                final WaveDrawable waveDrawable = new WaveDrawable();
                waveDrawable.setInterval(60);
                waveDrawable.setFastMode(false);
                SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
                waveDrawable.addWave(30, 1, 0x440000ff, 1.8f, ((float) seekBar.getProgress()) / seekBar.getMax(), .5f);
                waveDrawable.addWave(30, 18, 0x440000ff, 2.4f, ((float) seekBar.getProgress()) / seekBar.getMax(), .7f);
                rootView.setBackgroundDrawable(waveDrawable);
                waveDrawable.start();
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        for (int i = 0; i < waveDrawable.getWaveCount(); i++) {
                            waveDrawable.setWaterLevel(i, ((float) progress) / seekBar.getMax());
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }
            return rootView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new WaveDrawableFragment()).commit();
        }
    }
}
