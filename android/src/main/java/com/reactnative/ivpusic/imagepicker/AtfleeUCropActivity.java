package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;

import java.util.ArrayList;
import java.util.List;

public class AtfleeUCropActivity extends Activity {
    private static final int DEFAULT_COMPRESS_QUALITY = 90;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int compressQuality = DEFAULT_COMPRESS_QUALITY;
    private GestureCropImageView cropImageView;
    private OverlayView overlayView;
    private ProgressBar loaderView;
    private View blockingView;
    private final List<TextView> ratioTabs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        setContentView(R.layout.atflee_ucrop_activity);
        applySystemBarInsets();
        bindViews();
        configureCropper(getIntent());
        configureRatioTabs();
        configureFooter();
        loadImage(getIntent());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cropImageView != null) {
            cropImageView.cancelAllAnimations();
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
    }

    private void applySystemBarInsets() {
        View root = findViewById(R.id.atflee_crop_root);
        View shell = findViewById(R.id.atflee_crop_shell);
        View ratioScroll = findViewById(R.id.atflee_crop_ratio_scroll);
        View footer = findViewById(R.id.atflee_crop_footer);

        final int ratioHeight = ratioScroll.getLayoutParams().height;
        final int footerHeight = footer.getLayoutParams().height;

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            shell.setPadding(bars.left, 0, bars.right, 0);
            setViewHeight(ratioScroll, ratioHeight + bars.top);
            ratioScroll.setPadding(0, bars.top, 0, 0);
            setViewHeight(footer, footerHeight + bars.bottom);
            footer.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setViewHeight(@NonNull View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params.height == height) {
            return;
        }
        params.height = height;
        view.setLayoutParams(params);
    }

    private void bindViews() {
        UCropView cropView = findViewById(R.id.atflee_ucrop);
        cropImageView = cropView.getCropImageView();
        overlayView = cropView.getOverlayView();
        loaderView = findViewById(R.id.atflee_crop_loader);
        blockingView = findViewById(R.id.atflee_crop_blocking_view);
        cropImageView.setTransformImageListener(transformImageListener);
    }

    private void configureCropper(@NonNull Intent intent) {
        processCompressionOptions(intent);
        cropImageView.setScaleEnabled(true);
        cropImageView.setRotateEnabled(false);
        cropImageView.setMaxBitmapSize(intent.getIntExtra(
                UCrop.Options.EXTRA_MAX_BITMAP_SIZE,
                CropImageView.DEFAULT_MAX_BITMAP_SIZE
        ));
        cropImageView.setMaxScaleMultiplier(intent.getFloatExtra(
                UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER,
                CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER
        ));
        cropImageView.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(
                UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION,
                CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION
        ));
        configureOverlay(intent);
    }

    private void processCompressionOptions(@NonNull Intent intent) {
        String formatName = intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME);
        if (!TextUtils.isEmpty(formatName)) {
            compressFormat = Bitmap.CompressFormat.valueOf(formatName);
        }
        compressQuality = intent.getIntExtra(
                UCrop.Options.EXTRA_COMPRESSION_QUALITY,
                DEFAULT_COMPRESS_QUALITY
        );
    }

    private void configureOverlay(@NonNull Intent intent) {
        overlayView.setFreestyleCropEnabled(true);
        overlayView.setDimmedColor(intent.getIntExtra(
                UCrop.Options.EXTRA_DIMMED_LAYER_COLOR,
                ContextCompat.getColor(this, com.yalantis.ucrop.R.color.ucrop_color_default_dimmed)
        ));
        overlayView.setCircleDimmedLayer(intent.getBooleanExtra(
                UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER,
                OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER
        ));
        overlayView.setShowCropFrame(intent.getBooleanExtra(
                UCrop.Options.EXTRA_SHOW_CROP_FRAME,
                OverlayView.DEFAULT_SHOW_CROP_FRAME
        ));
        overlayView.setShowCropGrid(intent.getBooleanExtra(
                UCrop.Options.EXTRA_SHOW_CROP_GRID,
                OverlayView.DEFAULT_SHOW_CROP_GRID
        ));
        overlayView.setCropGridRowCount(intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT,
                OverlayView.DEFAULT_CROP_GRID_ROW_COUNT
        ));
        overlayView.setCropGridColumnCount(intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT,
                OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT
        ));
    }

    private void configureRatioTabs() {
        LinearLayout ratioList = findViewById(R.id.atflee_crop_ratio_list);
        RatioOption[] options = new RatioOption[]{
                RatioOption.fixed("원본", CropImageView.SOURCE_IMAGE_ASPECT_RATIO),
                RatioOption.free("자유롭게"),
                RatioOption.fixed("1:1", 1f),
                RatioOption.fixed("3:4", 3f / 4f),
                RatioOption.fixed("4:3", 4f / 3f),
                RatioOption.fixed("9:16", 9f / 16f),
                RatioOption.fixed("16:9", 16f / 9f),
                RatioOption.fit("화면 맞춤")
        };
        for (int index = 0; index < options.length; index++) {
            addRatioTab(ratioList, options[index], index == 1);
        }
    }

    private void addRatioTab(LinearLayout ratioList, RatioOption option, boolean selected) {
        TextView tab = new TextView(this);
        tab.setText(option.label);
        tab.setTextSize(14);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine(true);
        tab.setMinWidth(dp(52));
        tab.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(34)
        );
        params.setMargins(dp(2), 0, dp(2), 0);
        tab.setLayoutParams(params);
        tab.setOnClickListener(view -> applyRatioOption((TextView) view, option));
        ratioList.addView(tab);
        ratioTabs.add(tab);
        if (selected) {
            applyRatioOption(tab, option);
        } else {
            styleRatioTab(tab, false);
        }
    }

    private void applyRatioOption(TextView selectedTab, RatioOption option) {
        if (option.fitOnly) {
            wrapImageIfLoaded();
            return;
        }
        overlayView.setFreestyleCropEnabled(option.freestyle);
        cropImageView.setTargetAspectRatio(option.aspectRatio);
        wrapImageIfLoaded();
        for (TextView tab : ratioTabs) {
            styleRatioTab(tab, tab == selectedTab);
        }
    }

    private void wrapImageIfLoaded() {
        if (cropImageView.getDrawable() != null) {
            cropImageView.setImageToWrapCropBounds();
        }
    }

    private void styleRatioTab(TextView tab, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(17));
        background.setColor(selected ? Color.WHITE : Color.TRANSPARENT);
        tab.setBackground(background);
        tab.setTextColor(selected ? Color.BLACK : Color.WHITE);
    }

    private void configureFooter() {
        findViewById(R.id.atflee_crop_cancel).setOnClickListener(view -> cancelCrop());
        findViewById(R.id.atflee_crop_rotate).setOnClickListener(view -> rotateByAngle(90));
        findViewById(R.id.atflee_crop_done).setOnClickListener(view -> cropAndSaveImage());
    }

    private void loadImage(@NonNull Intent intent) {
        Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI);
        Uri outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
        if (inputUri == null || outputUri == null) {
            setResultError(new NullPointerException("Input or output Uri is absent"));
            finish();
            return;
        }
        try {
            cropImageView.setImageUri(inputUri, outputUri);
        } catch (Exception error) {
            setResultError(error);
            finish();
        }
    }

    private void cancelCrop() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void rotateByAngle(int angle) {
        cropImageView.postRotate(angle);
        cropImageView.setImageToWrapCropBounds();
    }

    private void cropAndSaveImage() {
        setBlocking(true);
        cropImageView.cropAndSaveImage(compressFormat, compressQuality, cropCallback);
    }

    private void setBlocking(boolean isBlocking) {
        blockingView.setClickable(isBlocking);
        blockingView.setVisibility(isBlocking ? View.VISIBLE : View.GONE);
        loaderView.setVisibility(isBlocking ? View.VISIBLE : View.GONE);
        setFooterEnabled(!isBlocking);
    }

    private void setFooterEnabled(boolean enabled) {
        ((ImageButton) findViewById(R.id.atflee_crop_cancel)).setEnabled(enabled);
        ((ImageButton) findViewById(R.id.atflee_crop_rotate)).setEnabled(enabled);
        ((ImageButton) findViewById(R.id.atflee_crop_done)).setEnabled(enabled);
    }

    private final TransformImageView.TransformImageListener transformImageListener =
            new TransformImageView.TransformImageListener() {
                @Override
                public void onLoadComplete() {
                    findViewById(R.id.atflee_ucrop).animate().alpha(1f).setDuration(180);
                    setBlocking(false);
                }

                @Override
                public void onLoadFailure(@NonNull Exception error) {
                    setResultError(error);
                    finish();
                }

                @Override
                public void onRotate(float currentAngle) {
                }

                @Override
                public void onScale(float currentScale) {
                }
            };

    private final BitmapCropCallback cropCallback = new BitmapCropCallback() {
        @Override
        public void onBitmapCropped(
                @NonNull Uri resultUri,
                int offsetX,
                int offsetY,
                int imageWidth,
                int imageHeight
        ) {
            setResultUri(resultUri, cropImageView.getTargetAspectRatio(), offsetX, offsetY, imageWidth, imageHeight);
            finish();
        }

        @Override
        public void onCropFailure(@NonNull Throwable error) {
            setResultError(error);
            finish();
        }
    };

    private void setResultUri(
            Uri uri,
            float resultAspectRatio,
            int offsetX,
            int offsetY,
            int imageWidth,
            int imageHeight
    ) {
        setResult(RESULT_OK, new Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY));
    }

    private void setResultError(Throwable error) {
        setResult(UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, error));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class RatioOption {
        final String label;
        final float aspectRatio;
        final boolean freestyle;
        final boolean fitOnly;

        private RatioOption(String label, float aspectRatio, boolean freestyle, boolean fitOnly) {
            this.label = label;
            this.aspectRatio = aspectRatio;
            this.freestyle = freestyle;
            this.fitOnly = fitOnly;
        }

        static RatioOption free(String label) {
            return new RatioOption(label, CropImageView.SOURCE_IMAGE_ASPECT_RATIO, true, false);
        }

        static RatioOption fixed(String label, float aspectRatio) {
            return new RatioOption(label, aspectRatio, false, false);
        }

        static RatioOption fit(String label) {
            return new RatioOption(label, CropImageView.SOURCE_IMAGE_ASPECT_RATIO, false, true);
        }
    }
}
