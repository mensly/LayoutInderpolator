package ly.mens.layoutinderpolator.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import ly.mens.layoutinderpolator.InderpolatorView;

public class InderpolatorActivity extends Activity implements View.OnClickListener, InderpolatorView.OnPageChangeListener {
    private InderpolatorView view;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (InderpolatorView)findViewById(R.id.main_view);
        view.setOnPageChangeListener(this);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(view.getPageCount());
        progressBar.setProgress(1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test:
                view.next(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!view.previous(true)) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageChange(InderpolatorView view, int page) {
        progressBar.setProgress(page + 1);
    }
}