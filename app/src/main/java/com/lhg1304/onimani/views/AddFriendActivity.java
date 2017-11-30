package com.lhg1304.onimani.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.lhg1304.onimani.R;
import com.lhg1304.onimani.views.transitions.FabTransform;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddFriendActivity extends AppCompatActivity {

    @BindView(R.id.username)
    AutoCompleteTextView tvEmail;

    @BindView(R.id.btn_find)
    Button btnFind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_friend);
        ButterKnife.bind(this);

        FabTransform.setup(this, findViewById(R.id.search_container));

        initButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initButton() {
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputString = tvEmail.getText().toString();

                if ( inputString.isEmpty() ) {
                    Snackbar.make(tvEmail, "이메일을 입력해주세요.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("email", inputString);
                setResult(RESULT_OK, intent);
                finishAfterTransition();
            }
        });
    }

    public void dismiss(View view) {
        finishAfterTransition();
    }
}
