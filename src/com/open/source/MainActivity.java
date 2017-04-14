package com.open.source;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.example.custom.R;

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
                waveDrawable.addWave(60, 1, 0x440000ff, 1.8f, ((float) seekBar.getProgress()) / seekBar.getMax(), .8f);
                waveDrawable.addWave(40, 18, 0x440000ff, 2.4f, ((float) seekBar.getProgress()) / seekBar.getMax(), .9f);
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
