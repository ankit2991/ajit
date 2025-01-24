package com.zaeem.mytvcast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.ironsource.mediationsdk.IronSource;
import com.zaeem.mytvcast.Adapters.DBAdapter;
import com.zaeem.mytvcast.Utils.Constant;
import com.zaeem.mytvcast.Utils.Dropbox.DropboxClientFactory;
import com.zaeem.mytvcast.Utils.Dropbox.GetFinalLinkTask;
import com.zaeem.mytvcast.Utils.Dropbox.GetSharedLinkTask;
import com.zaeem.mytvcast.Utils.Dropbox.ListFolderTask;
import com.zaeem.mytvcast.Utils.Dropbox.ListSharedLinkTask;
import com.zaeem.mytvcast.Utils.Dropbox.PicassoClient;
import com.zaeem.mytvcast.Utils.Dropbox.RevokeTokenTask;
import com.zaeem.mytvcast.Utils.ItemClickSupport;
import com.zaeem.mytvcast.Utils.StreamingManager;
import com.zaeem.mytvcast.Utils.TinyDB;

import java.util.ArrayList;
import java.util.List;

public class DropBoxActivity extends AppCompatActivity {

    private static final String DROPBOX_APP_KEY = "mgvb2geegjvyp49";

    private static final String TAG = "DropBoxActivity";
    private RecyclerView recyclerView;
    private DBAdapter adapter;
    private LinearLayoutManager manager;
    private Toolbar toolbar;
    private List<Metadata> files = new ArrayList<>();
    private String dbPath = "";
    private ProgressDialog progressDialog;
    private ArrayList<String> paths = new ArrayList<>();
    private boolean needShowAds = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler);
        setSupportActionBar(toolbar);
        setTitle("Dropbox");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                final Metadata meta = files.get(position);
                final String fileName = meta.getName();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                final String type = mime.getMimeTypeFromExtension(ext);
                if (meta instanceof FileMetadata) {
                    if (!StreamingManager.getInstance(DropBoxActivity.this).isDeviceConnected()) {
                        startActivity(new Intent(DropBoxActivity.this, ConnectDeviceActivity.class));
                        return;
                    }
                    showProgress("Preparing file...");
                    new ListSharedLinkTask(DropboxClientFactory.getClient(), new ListSharedLinkTask.Callback() {
                        @Override
                        public void onDataLoaded(ListSharedLinksResult result) {
                            if (result != null && result.getLinks().size() > 0) {
                                getFinalLink(fileName, result.getLinks().get(0).getUrl() + "&raw=1", type);
                            } else {
                                getLinkWith(fileName, meta.getPathDisplay(), type);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            hideProgress();
                            Toast.makeText(DropBoxActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
                        }
                    }).execute(meta.getPathDisplay());
                } else {
                    setTitle(meta.getName());
                    String path = meta.getPathDisplay();
                    paths.add(path);
                    dbPath = path;
                    loadData();
                }
            }
        });
        if (!hasDropboxToken()) {
            needShowAds = true;
            Auth.startOAuth2Authentication(DropBoxActivity.this, DROPBOX_APP_KEY);
        } else {
            showAds();
        }
    }

    public void showAds() {


    }

    public void getFinalLink(final String name, String path, final String type) {
        new GetFinalLinkTask(new GetFinalLinkTask.Callback() {
            @Override
            public void onDataLoaded(String result) {
                hideProgress();
                if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".gif")) {
                    StreamingManager.getInstance(DropBoxActivity.this).showContent(name, type, result);
                } else if (name.toLowerCase().endsWith(".mov") || name.toLowerCase().endsWith(".mp4")) {
                    StreamingManager.getInstance(DropBoxActivity.this).playMedia(name, type, result);
                } else if (name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".wav")) {
                    StreamingManager.getInstance(DropBoxActivity.this).playAudio(name, type, result);
                }
                showAds();
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                Toast.makeText(DropBoxActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(path);
    }

    public void getLinkWith(final String name, String path, final String type) {
        new GetSharedLinkTask(DropboxClientFactory.getClient(), new GetSharedLinkTask.Callback() {
            @Override
            public void onDataLoaded(SharedLinkMetadata result) {
                hideProgress();
                getFinalLink(name, result.getUrl() + "&raw=1", type);
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                Toast.makeText(DropBoxActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(path);
    }


    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        if (files.size() == 0)
            loadData();
    }

    public void loadData() {
        showProgress("Loading files...");
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                hideProgress();
                List<Metadata> sortedFiles = new ArrayList<>();
                for (Metadata d : result.getEntries()) {
                    if (d instanceof FileMetadata) {
                        String fileName = d.getName();
                        if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".gif")) {
                            sortedFiles.add(d);
                        } else if (fileName.toLowerCase().endsWith(".mov") || fileName.endsWith(".mp4")) {
                            sortedFiles.add(d);
                        } else if (fileName.toLowerCase().endsWith(".mp3") || fileName.toLowerCase().endsWith(".wav")) {
                            sortedFiles.add(d);
                        }
                    } else {
                        sortedFiles.add(d);
                    }
                }
                files = sortedFiles;
                adapter = new DBAdapter(files, DropBoxActivity.this, PicassoClient.getPicasso());
                recyclerView.setAdapter(adapter);
                if (needShowAds) {
                    needShowAds = false;
                    showAds();
                }
            }

            @Override
            public void onError(Exception e) {
                hideProgress();
                Toast.makeText(DropBoxActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
            }
        }).execute(dbPath);
    }

    private void showProgress(String text) {
        progressDialog = new ProgressDialog(DropBoxActivity.this);
        progressDialog.setMessage(text);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (paths.size() == 0) {
            finish();
        } else {
            paths.remove(paths.size() - 1);
            if (paths.size() == 0) {
                dbPath = "";
                setTitle("Dropbox");
            } else {
                dbPath = paths.get(paths.size() - 1);
                String segments[] = dbPath.split("/");
                if (segments.length > 0) {
                    String folder = segments[segments.length - 1];
                    setTitle(folder);
                } else {
                    setTitle("Dropbox");
                }
            }
            loadData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exit_menu, menu);
        MenuItem exitItem = menu.findItem(R.id.action_exit);
        exitItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (DropboxClientFactory.haveClient() && DropboxClientFactory.getClient() != null) {
                    showProgress("Logging out...");
                    new RevokeTokenTask(DropboxClientFactory.getClient(), new RevokeTokenTask.Callback() {
                        @Override
                        public void onDataLoaded() {
                            clearDBCache();
                            finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            hideProgress();
                            clearDBCache();
                            finish();
                        }
                    }).execute();
                } else {
                    finish();
                }
                return true;
            }
        });
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void clearDBCache() {

        TinyDB.getInstance(this).putString(Constant.ACCESS_TOKEN, "");

        DropboxClientFactory.sDbxClient = null;
    }

    protected boolean hasDropboxToken() {
        String accessToken = TinyDB.getInstance(this).getString(Constant.ACCESS_TOKEN);
        return accessToken != null && !accessToken.equals("");
    }


    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String accessToken = TinyDB.getInstance(this).getString(Constant.ACCESS_TOKEN);
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null && !accessToken.isEmpty()) {
                TinyDB.getInstance(this).putString(Constant.ACCESS_TOKEN, accessToken);
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
        IronSource.onResume(this);
    }

}