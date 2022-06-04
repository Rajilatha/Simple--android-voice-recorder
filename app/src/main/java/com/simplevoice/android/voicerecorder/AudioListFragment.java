package com.simplevoice.android.voicerecorder;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;



public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick{

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList;
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay;


    private ImageButton playBtn;
    private TextView playerHeader;
    private TextView playerFileName;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;


    private AdView adView;


    public AudioListFragment() {

    }


    class Pair implements Comparable {
        public long t;
        public File f;

        public Pair(File file) {
            f = file;
            t = file.lastModified();
        }

        public int compareTo(Object o) {
            long u = ((Pair) o).t;
            return t > u ? -1 : t == u ? 0 : 1;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.audio_list_view);

        playBtn = view.findViewById(R.id.player_play_btn);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerFileName = view.findViewById(R.id.player_file_name);
        playerSeekbar = view.findViewById(R.id.player_seekbar);



        AudienceNetworkAds.initialize(getActivity());

        adView = new AdView(getActivity(), "REAL_AD_ID_HERE", AdSize.BANNER_HEIGHT_50);


        LinearLayout adContainer = (LinearLayout) view.findViewById(R.id.banner_container);


        adContainer.addView(adView);


        adView.loadAd();


        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();



        Pair[] pairs = new Pair[allFiles.length];
        for (int i = 0; i < allFiles.length; i++)
            pairs[i] = new Pair(allFiles[i]);


        Arrays.sort(pairs);


        for (int i = 0; i < allFiles.length; i++)
            allFiles[i] = pairs[i].f;

        audioListAdapter = new AudioListAdapter(allFiles, this);
        audioList.setHasFixedSize(true);

        audioList.setLayoutManager(new LinearLayoutManager(getContext()));

        audioList.setAdapter(audioListAdapter);


        audioList.setItemViewCacheSize(20);
        audioList.setDrawingCacheEnabled(true);
        audioList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        audioListAdapter.notifyItemRemoved(audioList.getId());
        audioListAdapter.notifyItemChanged(audioList.getId());
        audioListAdapter.notifyItemInserted(audioList.getId());



        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_HIDDEN){

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        playBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (isPlaying){
                    pauseAudio();
                } else {

                    if (fileToPlay != null) {
                        resumeAudio();
                    } else {
                        Toast.makeText(getContext(), "Please select a voice record to play",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                    resumeAudio();
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if (isPlaying){

            stopAudio();
            playAudio(fileToPlay);

        } else {

            if (fileToPlay != null){

                playAudio(fileToPlay);
            } else {



            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void pauseAudio(){
        if (fileToPlay != null) {
            mediaPlayer.pause();

            playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
            isPlaying = false;
            seekbarHandler.removeCallbacks(updateSeekbar);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void resumeAudio(){
        mediaPlayer.start();

        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn,null));
        isPlaying = true;
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopAudio() {

            playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
            playerHeader.setText("Stopped");
            isPlaying = false;
            mediaPlayer.stop();
            seekbarHandler.removeCallbacks(updateSeekbar);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playAudio(final File fileToPlay) {

        mediaPlayer = new MediaPlayer();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        try {

            mediaPlayer.setDataSource(fileToPlay.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        playerFileName.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        isPlaying = true;


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompletion(MediaPlayer mp) {
                    stopAudio();
                    playerHeader.setText("finished");


                    mediaPlayer.reset();
                    try {
                        mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        });


        playerSeekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {

                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());


                seekbarHandler.postDelayed(this, 0);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            stopAudio();
        }
    }
}
