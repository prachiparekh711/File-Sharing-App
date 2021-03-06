package com.datasharing.Adapter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.datasharing.Database.DBHelper;
import com.datasharing.Model.History;
import com.datasharing.R;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ReceiveHistoryAdapter extends RecyclerView.Adapter<ReceiveHistoryAdapter.MyClassView> {

    ArrayList<History> pathList;
    Activity mActivity;
    DBHelper dbHelper;

    public ReceiveHistoryAdapter(ArrayList<History> pathList, Activity mActivity) {
        this.pathList = pathList;
        this.mActivity = mActivity;
        dbHelper = new DBHelper(mActivity);
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_history, parent, false);
        return new MyClassView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        History history = pathList.get(position);
        File file = new File(history.getFilePath());

        String filename = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1);
        holder.tv_title.setText(filename);

        holder.cbx.setVisibility(View.GONE);
        holder.img_cross.setVisibility(View.VISIBLE);

        // Get length of file in bytes
        long fileSizeInBytes = file.getAbsoluteFile().length();
        long fileSizeInKB = fileSizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;

        Log.e("LLLL_size: ", String.valueOf(fileSizeInBytes));

        if (fileSizeInBytes > 1024 * 1024) {
            holder.tv_size.setText(fileSizeInMB + "MB");
        } else if (fileSizeInBytes > 1024) {
            holder.tv_size.setText(fileSizeInKB + "KB");
        } else {
            holder.tv_size.setText(fileSizeInBytes + "B");
        }

        if (file.getPath().endsWith("xls") || file.getPath().endsWith("xlsx")) {
            holder.ic_audio.setImageResource(R.drawable.ic_xls);
        } else if (file.getPath().endsWith("doc") || file.getPath().endsWith("docx")) {
            holder.ic_audio.setImageResource(R.drawable.ic_word);
        } else if (file.getPath().endsWith("ppt") || file.getPath().endsWith("pptx")) {
            holder.ic_audio.setImageResource(R.drawable.ic_power_point);
        } else if (file.getPath().endsWith("pdf")) {
            holder.ic_audio.setImageResource(R.drawable.ic_pdf);
        } else if (file.getPath().endsWith("txt")) {
            holder.ic_audio.setImageResource(R.drawable.ic_txt);
        } else if (file.getPath().endsWith("apk")) {

            PackageManager packageManager = mActivity.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(), 0);

            // you need to set this variables manually for some reason to get icon from APK file that has not been installed
            packageInfo.applicationInfo.sourceDir = file.getAbsolutePath();
            packageInfo.applicationInfo.publicSourceDir = file.getAbsolutePath();

            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            Glide.with(mActivity)
                    .load(icon)
                    .error(R.drawable.ic_circle_gray)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .priority(Priority.IMMEDIATE)
                    .into(holder.ic_audio);
        } else if (file.getPath().endsWith("mp3") || file.getPath().endsWith("wmv") || file.getPath().endsWith("ogg") || file.getPath().endsWith("pcm") || file.getPath().endsWith("aiff")) {
            holder.ic_audio.setImageResource(R.drawable.ic_h_music);
        } else if (file.getPath().endsWith(".jpg") || file.getPath().endsWith(".JPG")
                || file.getPath().endsWith(".jpeg") || file.getPath().endsWith(".JPEG")
                || file.getPath().endsWith(".png") || file.getPath().endsWith(".PNG")
                || file.getPath().endsWith(".gif") || file.getPath().endsWith(".GIF")) {
            Glide.with(mActivity)
                    .load(Uri.fromFile(new File(file.getPath())))
                    .error(R.drawable.ic_circle_gray)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .priority(Priority.IMMEDIATE)
                    .into(holder.ic_audio);
        } else if (file.getPath().endsWith("mp4") || file.getPath().endsWith("mpeg") || file.getPath().endsWith("wav") || file.getPath().endsWith("flv")) {
            RequestOptions options = new RequestOptions();
            Glide.with(mActivity)
                    .load(file.getPath())
                    .apply(options.centerCrop())
                    .centerCrop()
                    .transition(withCrossFade())
                    .into(holder.ic_audio);
        } else {
            holder.ic_audio.setImageResource(R.drawable.ic_txt);
        }

        holder.img_cross.setOnClickListener(v -> {
            dbHelper.deleteHistoryRecords(history.getFilePath());
            if (file.exists()) file.delete();
            pathList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, pathList.size());
        });
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        TextView tv_title, tv_size;
        ImageView ic_audio, cbx, img_cross;

        public MyClassView(@NonNull View itemView) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.tv_title);
            tv_size = itemView.findViewById(R.id.tv_size);
            ic_audio = itemView.findViewById(R.id.ic_audio);
            cbx = itemView.findViewById(R.id.cbx);
            img_cross = itemView.findViewById(R.id.img_cross);
        }
    }
}
