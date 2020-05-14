package sino.android.rxphoto;


import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import sino.android.rxmatisse.Matisse;
import sino.android.rxmatisse.MimeType;
import sino.android.rxmatisse.engine.impl.GlideEngine;
import sino.android.rxmatisse.entity.CaptureStrategy;
import sino.android.rxmatisse.filter.Filter;
import sino.android.rxmatisse.filter.GifSizeFilter;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHOOSE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.photo_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCameraPermission();
                    }
                });
    }

    private void onCameraPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new SubObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            onMatisse();
                        } else {
                            Log.d("rx", "请允许权限: ");
                        }
                    }
                });
    }

    private void onPhoto() {
        new RxPhotos(this)
                .request("sino.android.rxphotox.fileprovider", true)
                .subscribe(new SubObserver<RxPhoto>() {
                    @Override
                    public void onNext(RxPhoto rxPhoto) {
                        showImageView(rxPhoto.getPath(), rxPhoto.getUri());
                    }
                });
    }

    private void onCopy(final Uri uri) {
        Observable.just(uri)
                .map(new Function<Uri, String>() {
                    @Override
                    public String apply(Uri uri) throws Exception {
                        return Utils.asFilePath(MainActivity.this, uri);
                    }
                })
                .compose(RxJavas.<String>scheduler())
                .subscribe(new SubObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        showImageView(s, uri);
                    }
                });
    }

    private void showImageView(String path, Uri uri) {
        // /storage/emulated/0/Pictures/JPEG_20200426_151014.jpg
        // content://sino.android.rxphotox.FileProvider/external-path/Pictures/JPEG_20200426_151014.jpg
        ImageView imageView = findViewById(R.id.image_view);

//        if (Utils.isAndroidQ()) {
//            Log.d("rx", "showImageView: uri= " + uri);
//            Glide.with(this).load(uri).into(imageView);
//        } else {
//            Log.d("rx", "showImageView: path= " + path);
//            Glide.with(this).load(path).into(imageView);
//        }

        Log.d("rx", "showImageView: path= " + path);
        Glide.with(this).load(path).into(imageView);
    }


    private void onMatisse() {
        Matisse.from(this)
                .choose(MimeType.ofImage(), true)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .showSingleMediaType(true)
                .originalEnable(true)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .forResult(REQUEST_CODE_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> uris = Matisse.obtainResult(data);
            List<String> photos = Matisse.obtainPathResult(data);
            Log.e("OnActivityResult ", String.valueOf(Matisse.obtainOriginalState(data)));

            if (uris != null && photos != null) {
                if (uris.size() != 0 && photos.size() != 0) {

                    showImageView(photos.get(0), uris.get(0));
                }
            }

        }
    }

}
