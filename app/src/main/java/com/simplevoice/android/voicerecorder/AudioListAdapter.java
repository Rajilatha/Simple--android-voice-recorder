package com.simplevoice.android.voicerecorder;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {

    private File[] allFiles;
    private TimeAgo timeAgo;
    private onItemListClick onItemListClick;


    public AudioListAdapter(File[] allFiles, onItemListClick onItemListClick) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();

        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, final int position) {

        holder.listTitle.setText(allFiles[position].getName());
        holder.listDate.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));
        holder.renameFilename();
        holder.deleteFile();
        holder.shareRecording();


    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }


    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView listImage;
        private TextView listTitle;
        private TextView listDate;
        private Button deleteBtn;
        private Button renameBtn;
        private Button shareBtn;

        private String authorities = "com.simplevoice.android.voicerecorder.fileprovider";

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            listImage = itemView.findViewById(R.id.list_image_view);
            listTitle = itemView.findViewById(R.id.list_title);
            listDate = itemView.findViewById(R.id.list_date);

            deleteBtn = itemView.findViewById(R.id.delete_btn);
            renameBtn = itemView.findViewById(R.id.rename_btn);
            shareBtn = itemView.findViewById(R.id.share_btn);


            itemView.setOnClickListener(this);


        }

        private void deleteFile() {

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    deleteAlertDialog();
                }
            });
        }

        private void renameFilename() {

            renameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    renameAlertDialog();
                }
            });
        }

        private void shareRecording() {

            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Uri path = FileProvider.getUriForFile(itemView.getContext(), authorities, allFiles[getAdapterPosition()]);

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, path);

                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setType("audio/*");
                    itemView.getContext().startActivity(Intent.createChooser(shareIntent, "share recording file"));
                }
            });
        }


        private void renameAlertDialog() {


            LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());
            final View promptView = layoutInflater.inflate(R.layout.rename_alert_dialog, null);

            final AlertDialog alertDialogBuilder = new AlertDialog.Builder(itemView.getContext()).create();


            alertDialogBuilder.setView(promptView);

            final EditText userInput = promptView.findViewById(R.id.rename_text);

            userInput.setText(allFiles[getAdapterPosition()].getName());

            Button renameAlertBtnPositive = promptView.findViewById(R.id.renameAlertBtnPositive);
            Button renameAlertBtnNegative = promptView.findViewById(R.id.renameAlertBtnNegative);


            alertDialogBuilder.setCancelable(false);


            renameAlertBtnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Editable renamedText = userInput.getText();
                    listTitle.setText(renamedText);


                    File filepath = new File("/storage/emulated/0/Android/data/com.simplevoice.android.voicerecorder/files/" + allFiles[getAdapterPosition()].getName());


                    File renamedPath = new File("/storage/emulated/0/Android/data/com.simplevoice.android.voicerecorder/files/" + renamedText + ".mp3");

                    filepath.renameTo(renamedPath);


                    allFiles[getAdapterPosition()] = renamedPath;


                    Toast.makeText(itemView.getContext(), "File renamed", Toast.LENGTH_SHORT).show();

                    alertDialogBuilder.dismiss();

                }
            });

            renameAlertBtnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogBuilder.cancel();
                }
            });


            alertDialogBuilder.show();
        }

        private void deleteAlertDialog() {

            final LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());

            final View promptView = layoutInflater.inflate(R.layout.common_alert_dialog, null);

            final AlertDialog alertDialogBuilder = new AlertDialog.Builder(itemView.getContext()).create();

            alertDialogBuilder.setView(promptView);

            Button alertBtnPositive = promptView.findViewById(R.id.commonAlertPosBtn);
            Button alertBtnNegative = promptView.findViewById(R.id.commonAlertNegBtn);

            alertDialogBuilder.setCancelable(false);

            alertBtnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    allFiles[getAdapterPosition()].delete();

                    listImage.setVisibility(View.GONE);
                    listTitle.setVisibility(View.GONE);
                    listDate.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
                    renameBtn.setVisibility(View.GONE);
                    shareBtn.setVisibility(View.GONE);

                    Toast.makeText(v.getContext(), "File deleted", Toast.LENGTH_LONG).show();

                    notifyItemRemoved(getAdapterPosition());

                    alertDialogBuilder.dismiss();

                }
            });

            alertBtnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogBuilder.cancel();
                }
            });

            alertDialogBuilder.setTitle("Are you sure want to delete ?");
            alertDialogBuilder.show();

        }

        @Override
        public void onClick(View v) {
            onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
        }

    }

    public interface onItemListClick {
        void onClickListener(File file, int position);
    }
}
