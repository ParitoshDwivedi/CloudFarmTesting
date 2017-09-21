package jp.co.cyberagent.stf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;


public class TestActivity extends Activity implements View.OnClickListener {

    Button btnStart, btnStop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnStop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnStart:
                Intent intent = new Intent();
                intent.setAction("jp.co.cyberagent.stf.ACTION_START");
                startService(intent);
                break;
            case R.id.btnStop:
                Intent intent1 = new Intent();
                intent1.setAction("jp.co.cyberagent.stf.ACTION_STOP");
                stopService(intent1);
                break;
            default:
                break;
        }
    }
}
