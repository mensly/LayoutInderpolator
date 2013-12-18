package ly.mens.layoutinderpolator.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import ly.mens.layoutinderpolator.InderpolatorView;

public class InderpolatorActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test:
                ((InderpolatorView)findViewById(R.id.main_view)).next(true);
                break;
        }
    }
}