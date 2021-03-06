package com.datasharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datasharing.Adapter.FolderListAdapter;
import com.datasharing.Adapter.OnSelectStateListener;
import com.datasharing.Adapter.VideoPickAdapter;
import com.datasharing.Const.Constant;
import com.datasharing.filter.FileFilter;
import com.datasharing.filter.callback.FilterResultCallback;
import com.datasharing.filter.entity.Directory;
import com.datasharing.filter.entity.VideoFile;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideosFragment extends MainFragment {

    public static final String THUMBNAIL_PATH = "FilePick";
    public static final String IS_NEED_CAMERA = "IsNeedCamera";
    public static final String IS_TAKEN_AUTO_SELECTED = "IsTakenAutoSelected";

    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final int COLUMN_NUMBER = 3;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final ArrayList<VideoFile> mSelectedList = new ArrayList<>();
    List<VideoFile> imgDownloadList = new ArrayList<>();
    List<VideoFile> imgMainDownloadList = new ArrayList<>();
    List<VideoFile> imgMain1DownloadList = new ArrayList<>();
    LinearLayoutManager layoutManager;
    @BindView(R.id.tv_app)
    TextView tv_app;
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private RecyclerView mRecyclerView;
    private VideoPickAdapter mAdapter;
    private RelativeLayout rl_progress;
    private AVLoadingIndicatorView avi;
    private boolean isNeedCamera;
    private boolean isTakenAutoSelected;
    private List<Directory<VideoFile>> mAll;
    private ProgressBar mProgressBar;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    //    private TextView tv_count;
    private TextView tv_folder;
    private ImageView img_no_data;
    private LinearLayout ll_folder;
    //    private RelativeLayout tb_pick;
    private SearchView searchView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MyReceiver r;

    public VideosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideosFragment newInstance(String param1, String param2) {
        VideosFragment fragment = new VideosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    void startAnim() {
        rl_progress.setVisibility(View.VISIBLE);
        avi.show();
    }

    void stopAnim() {
        rl_progress.setVisibility(View.GONE);
        avi.hide();
    }

    @Override
    void permissionGranted() {
        loadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        // Inflate the layout for this fragment
        return view;
    }

    private void initView(View view) {
//        tv_count = (TextView) view.findViewById(R.id.tv_count);
//        tv_count.setText(mCurrentNumber + "/" + mMaxNumber);

        rl_progress = view.findViewById(R.id.rl_progress);
        avi = view.findViewById(R.id.avi);
        mRecyclerView = view.findViewById(R.id.rv_video_pick);
        layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        searchView = view.findViewById(R.id.video_search);
        mAdapter = new VideoPickAdapter(getContext(), isNeedCamera, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);
        new LongOperation().execute();
        img_no_data = view.findViewById(R.id.img_no_data);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<VideoFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, VideoFile file) {
                if (state) {
                    mSelectedList.add(file);
                    Constant.filePaths.add(file.getPath());
                    Constant.FileName.add(file.getName());
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    Constant.filePaths.remove(file.getPath());
                    Constant.FileName.add(file.getName());
                    mCurrentNumber--;
                }
                TransferFragment.fragmentPlayListBinding.tvSendFile.setText("SEND ( " + Constant.filePaths.size() + " )");
            }
        });

        File folder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + THUMBNAIL_PATH);
        if (!folder.exists()) {
            startAnim();
        } else {
            stopAnim();
        }

        ll_folder = view.findViewById(R.id.ll_folder);
        if (isNeedFolderList) {
            ll_folder.setVisibility(View.GONE);
            ll_folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mFolderHelper.toggle(tb_pick);
                }
            });
            tv_folder = view.findViewById(R.id.tv_folder);
            tv_folder.setText(getResources().getString(R.string.vw_all));

            mFolderHelper.setFolderListListener(new FolderListAdapter.FolderListListener() {
                @Override
                public void onFolderListClick(Directory directory) {
//                    mFolderHelper.toggle(tb_pick);
                    tv_folder.setText(directory.getName());

                    if (TextUtils.isEmpty(directory.getPath())) { //All
                        refreshData(mAll);
                    } else {
                        for (Directory<VideoFile> dir : mAll) {
                            if (dir.getPath().equals(directory.getPath())) {
                                List<Directory<VideoFile>> list = new ArrayList<>();
                                list.add(dir);
                                refreshData(list);
                                break;
                            }
                        }
                    }
                }
            });
        }

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // filter recycler view when query submitted
//
//                mAdapter.getFilter().filter(query);
//                tv_app.setText("Video (" + mAdapter.filteredList.size() + ")");
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String query) {
//                // filter recycler view when text is changed
//
//                mAdapter.getFilter().filter(query);
//                if(query.equals(""))
//                tv_app.setText("Video (" + mAdapter.mList.size() + ")");
//                return false;
//            }
//        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(mAdapter.mVideoPath);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    getActivity().sendBroadcast(mediaScanIntent);

                    loadData();
                }
                break;
        }
    }

    private void loadData() {
        FileFilter.getVideos(getActivity(), new FilterResultCallback<VideoFile>() {
            @Override
            public void onResult(List<Directory<VideoFile>> directories) {
//                mProgressBar.setVisibility(View.GONE);
                stopAnim();
                // Refresh folder list
                if (isNeedFolderList) {
                    ArrayList<Directory> list = new ArrayList<>();
                    Directory all = new Directory();
                    all.setName(getResources().getString(R.string.vw_all));
                    list.add(all);
                    list.addAll(directories);
                    mFolderHelper.fillData(list);
                }

                mAll = directories;
                if (directories.size() == 0)
                    img_no_data.setVisibility(View.VISIBLE);

                refreshData(directories);

//                scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//                    @Override
//                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//                        if (v.getChildAt(v.getChildCount() - 1) != null) {
//                            if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
//                                    scrollY > oldScrollY) {
//
//                                visibleItemCount = layoutManager.getChildCount();
//                                totalItemCount = layoutManager.getItemCount();
//                                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
//
//                                if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                                    onScrolledToBottom();
////                                    new LongOperation().execute();
//                                }
//                            }
//                        }
//                    }
//                });
            }
        });
    }

    private void refreshData(List<Directory<VideoFile>> directories) {
        boolean tryToFindTaken = isTakenAutoSelected;

        // if auto-select taken file is enabled, make sure requirements are met
        if (tryToFindTaken && !TextUtils.isEmpty(mAdapter.mVideoPath)) {
            File takenFile = new File(mAdapter.mVideoPath);
            tryToFindTaken = !mAdapter.isUpToMax() && takenFile.exists(); // try to select taken file only if max isn't reached and the file exists
        }

        List<VideoFile> list = new ArrayList<>();
        for (Directory<VideoFile> directory : directories) {
            list.addAll(directory.getFiles());

            // auto-select taken file?
            if (tryToFindTaken) {
                tryToFindTaken = findAndAddTaken(directory.getFiles());   // if taken file was found, we're done
            }
        }

        for (VideoFile file : mSelectedList) {
            int index = list.indexOf(file);
            if (index != -1) {
                list.get(index).setSelected(true);
            }
        }
        imgDownloadList.clear();
        imgDownloadList = list;
//        new LongOperation().execute();
        getActivity().runOnUiThread(() -> {
            mAdapter.refresh(imgDownloadList);
        });
    }

    private boolean findAndAddTaken(List<VideoFile> list) {
        for (VideoFile videoFile : list) {
            if (videoFile.getPath().equals(mAdapter.mVideoPath)) {
                mSelectedList.add(videoFile);
                Constant.filePaths.add(videoFile.getPath());
                Constant.FileName.add(videoFile.getName());
                mCurrentNumber++;
                mAdapter.setCurrentNumber(mCurrentNumber);
//                tv_count.setText(mCurrentNumber + "/" + mMaxNumber);

                return true;   // taken file was found and added
            }
        }
        return false;    // taken file wasn't found
    }

    private void onScrolledToBottom() {

        if (imgDownloadList.size() == 0) {
            getActivity().runOnUiThread(() -> {
                mRecyclerView.setVisibility(View.GONE);
            });
        } else {
            if (imgMain1DownloadList.size() < imgDownloadList.size()) {
                int x, y;
                if ((imgDownloadList.size() - imgMain1DownloadList.size()) >= 60) {
                    x = imgMain1DownloadList.size();
                    y = x + 60;
                } else {
                    x = imgMain1DownloadList.size();
                    y = x + imgDownloadList.size() - imgMain1DownloadList.size();
                }
                imgMainDownloadList.clear();
                for (int i = x; i < y; i++) {
                    imgMainDownloadList.add(imgDownloadList.get(i));
                    imgMain1DownloadList.add(imgDownloadList.get(i));
                }

                getActivity().runOnUiThread(() -> {
                    mAdapter.refresh(imgMainDownloadList);
                });
            }
        }
    }

    public void notifyData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mAdapter.filteredList.clear();
                mAdapter.refresh(new ArrayList<>());
                mSelectedList.clear();
                Constant.filePaths.clear();
                Constant.FileName.clear();
                loadData();
                Log.e("LLLL_VideoNotify: ", "Done");
                mAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            VideosFragment.this.notifyData();
        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Void... params) {
            return "Execute";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Execute")) {
                getActivity().runOnUiThread(() -> {

//                    tv_sys_app.setText("System Apps (" + systemApplicationModels.size() + ")");
                    tv_app.setText("Video (" + mAdapter.mList.size() + ")");
                    stopAnim();
                });
            }
        }
    }

}